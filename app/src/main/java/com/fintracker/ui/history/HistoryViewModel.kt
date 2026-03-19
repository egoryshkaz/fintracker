package com.fintracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintracker.data.model.Transaction
import com.fintracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneOffset

enum class FilterType { ALL, EXPENSE, INCOME }
enum class SortOption { DATE_NEWEST, DATE_OLDEST, AMOUNT_ASC, AMOUNT_DESC }

class HistoryViewModel(
    private val transactionRepo: TransactionRepository = TransactionRepository()
) : ViewModel() {

    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _filterType = MutableStateFlow(FilterType.ALL)
    val filterType: StateFlow<FilterType> = _filterType.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.DATE_NEWEST)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Месячные итоги
    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome.asStateFlow()

    private val _totalExpense = MutableStateFlow(0.0)
    val totalExpense: StateFlow<Double> = _totalExpense.asStateFlow()

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        _allTransactions,
        _filterType,
        _sortOption,
        _searchQuery
    ) { transactions, filterType, sortOption, searchQuery ->
        applyFilters(transactions, filterType, sortOption, searchQuery)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalAmount: StateFlow<Double> = filteredTransactions.map { list ->
        list.sumOf { if (it.type == "expense") -it.amount else it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            transactionRepo.getTransactionsRealtime().collect { result ->
                result.fold(
                    onSuccess = { list ->
                        _allTransactions.value = list
                        calculateMonthlyTotals(list)
                    },
                    onFailure = {
                        // обработка ошибки
                    }
                )
                _isLoading.value = false
            }
        }
    }

    private fun calculateMonthlyTotals(transactions: List<Transaction>) {
        val currentMonth = YearMonth.now()
        val start = currentMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond() * 1000
        val end = currentMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneOffset.UTC).toEpochSecond() * 1000

        val monthly = transactions.filter {
            it.date.toDate().time in start..end
        }
        _totalIncome.value = monthly.filter { it.type == "income" }.sumOf { it.amount }
        _totalExpense.value = monthly.filter { it.type == "expense" }.sumOf { it.amount }
    }

    fun setFilterType(type: FilterType) {
        _filterType.value = type
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    suspend fun refresh() {
        loadTransactions()
    }

    private fun applyFilters(
        transactions: List<Transaction>,
        filterType: FilterType,
        sortOption: SortOption,
        searchQuery: String
    ): List<Transaction> {
        val filteredByType = when (filterType) {
            FilterType.ALL -> transactions
            FilterType.EXPENSE -> transactions.filter { it.type == "expense" }
            FilterType.INCOME -> transactions.filter { it.type == "income" }
        }

        val filteredBySearch = if (searchQuery.isBlank()) {
            filteredByType
        } else {
            filteredByType.filter {
                it.description.contains(searchQuery, ignoreCase = true) ||
                        it.categoryName.contains(searchQuery, ignoreCase = true)
            }
        }

        return when (sortOption) {
            SortOption.DATE_NEWEST -> filteredBySearch.sortedByDescending { it.date.toDate().time }
            SortOption.DATE_OLDEST -> filteredBySearch.sortedBy { it.date.toDate().time }
            SortOption.AMOUNT_ASC -> filteredBySearch.sortedBy { kotlin.math.abs(it.amount) }
            SortOption.AMOUNT_DESC -> filteredBySearch.sortedByDescending { kotlin.math.abs(it.amount) }
        }
    }
}