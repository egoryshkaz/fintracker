package com.fintracker.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintracker.data.model.Transaction
import com.fintracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val transactionRepo: TransactionRepository = TransactionRepository()
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    private val _monthlyExpenses = MutableStateFlow(0.0)
    val monthlyExpenses: StateFlow<Double> = _monthlyExpenses.asStateFlow()

    private val _monthlyIncome = MutableStateFlow(0.0)
    val monthlyIncome: StateFlow<Double> = _monthlyIncome.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            transactionRepo.getTransactions().collect { result ->
                result.fold(
                    onSuccess = { list ->
                        _transactions.value = list
                        calculateStats(list)
                    },
                    onFailure = { /* ошибка */ }
                )
                _isLoading.value = false
            }
        }
    }

    private fun calculateStats(transactions: List<Transaction>) {
        val currentMonth = java.time.YearMonth.now()
        val startOfMonth = currentMonth.atDay(1).atStartOfDay(java.time.ZoneOffset.UTC).toEpochSecond() * 1000
        val endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59).atZone(java.time.ZoneOffset.UTC).toEpochSecond() * 1000

        val monthly = transactions.filter {
            it.date.toDate().time in startOfMonth..endOfMonth
        }

        val income = monthly.filter { it.type == "income" }.sumOf { it.amount }
        val expense = monthly.filter { it.type == "expense" }.sumOf { it.amount }

        _monthlyIncome.value = income
        _monthlyExpenses.value = expense
        _balance.value = income - expense
    }
}