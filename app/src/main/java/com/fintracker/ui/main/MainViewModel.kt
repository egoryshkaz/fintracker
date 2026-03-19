package com.fintracker.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintracker.data.model.Transaction
import com.fintracker.data.model.Category
import com.fintracker.data.model.CategoryAmount
import com.fintracker.data.repository.TransactionRepository
import com.fintracker.data.repository.CategoryRepository
import com.fintracker.data.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.*

class MainViewModel(
    private val transactionRepo: TransactionRepository = TransactionRepository(),
    private val categoryRepo: CategoryRepository = CategoryRepository()
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    // Месячные итоги
    private val _monthlyExpenses = MutableStateFlow(0.0)
    val monthlyExpenses: StateFlow<Double> = _monthlyExpenses.asStateFlow()

    private val _monthlyIncome = MutableStateFlow(0.0)
    val monthlyIncome: StateFlow<Double> = _monthlyIncome.asStateFlow()

    // Дневные итоги
    private val _dailyExpenses = MutableStateFlow(0.0)
    val dailyExpenses: StateFlow<Double> = _dailyExpenses.asStateFlow()

    private val _dailyIncome = MutableStateFlow(0.0)
    val dailyIncome: StateFlow<Double> = _dailyIncome.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Категории
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    private val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    // Данные для диаграмм
    private val _incomeCategories = MutableStateFlow<List<CategoryAmount>>(emptyList())
    val incomeCategories: StateFlow<List<CategoryAmount>> = _incomeCategories.asStateFlow()

    private val _expenseCategories = MutableStateFlow<List<CategoryAmount>>(emptyList())
    val expenseCategories: StateFlow<List<CategoryAmount>> = _expenseCategories.asStateFlow()

    init {
        observeCategories()
        observeTransactions()
        ensureDefaultCategories()
    }

    private fun observeCategories() {
        viewModelScope.launch {
            categoryRepo.getCategoriesRealtime().collect { result ->
                result.fold(
                    onSuccess = { categories ->
                        _categories.value = categories
                    },
                    onFailure = {
                        // обработка ошибки
                    }
                )
            }
        }
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            transactionRepo.getTransactionsRealtime().collect { result ->
                result.fold(
                    onSuccess = { list ->
                        _transactions.value = list
                        calculateStats(list)
                        calculateCategoryBreakdown(list)
                    },
                    onFailure = {
                        // обработка ошибки
                    }
                )
            }
        }
    }

    private fun calculateStats(transactions: List<Transaction>) {
        // Получаем начало и конец сегодняшнего дня в UTC
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val todayEnd = calendar.timeInMillis

        val currentMonth = YearMonth.now()
        val monthStart = currentMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond() * 1000
        val monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneOffset.UTC).toEpochSecond() * 1000

        var dailyIncome = 0.0
        var dailyExpense = 0.0
        var monthlyIncome = 0.0
        var monthlyExpense = 0.0
        var balance = 0.0

        transactions.forEach { transaction ->
            val dateMillis = transaction.date.toDate().time
            val amount = transaction.amount
            when (transaction.type) {
                "income" -> {
                    balance += amount
                    if (dateMillis in todayStart..todayEnd) dailyIncome += amount
                    if (dateMillis in monthStart..monthEnd) monthlyIncome += amount
                }
                "expense" -> {
                    balance -= amount
                    if (dateMillis in todayStart..todayEnd) dailyExpense += amount
                    if (dateMillis in monthStart..monthEnd) monthlyExpense += amount
                }
            }
        }

        _dailyIncome.value = dailyIncome
        _dailyExpenses.value = dailyExpense
        _monthlyIncome.value = monthlyIncome
        _monthlyExpenses.value = monthlyExpense
        _balance.value = balance
    }

    private fun calculateCategoryBreakdown(transactions: List<Transaction>) {
        val currentMonth = YearMonth.now()
        val start = currentMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond() * 1000
        val end = currentMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneOffset.UTC).toEpochSecond() * 1000

        val monthly = transactions.filter {
            it.date.toDate().time in start..end
        }

        // Создаём мапу категорий для быстрого доступа
        val categoryMap = categories.value.associateBy { it.name }

        // Доходы по категориям
        val incomeMap = monthly
            .filter { it.type == "income" }
            .groupBy { it.categoryName }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .map { (name, amount) ->
                val cat = categoryMap[name]
                CategoryAmount(
                    categoryName = name,
                    amount = amount,
                    color = cat?.color ?: "#4CAF50",
                    iconName = cat?.icon ?: "work"
                )
            }
            .sortedByDescending { it.amount }

        // Расходы по категориям
        val expenseMap = monthly
            .filter { it.type == "expense" }
            .groupBy { it.categoryName }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .map { (name, amount) ->
                val cat = categoryMap[name]
                CategoryAmount(
                    categoryName = name,
                    amount = amount,
                    color = cat?.color ?: "#F44336",
                    iconName = cat?.icon ?: "folder"
                )
            }
            .sortedByDescending { it.amount }

        _incomeCategories.value = incomeMap
        _expenseCategories.value = expenseMap
    }

    private fun ensureDefaultCategories() {
        viewModelScope.launch {
            val authRepo = AuthRepository()
            authRepo.ensureDefaultCategories()
        }
    }
}