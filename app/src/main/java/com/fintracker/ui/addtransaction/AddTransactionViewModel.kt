package com.fintracker.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintracker.data.model.Category
import com.fintracker.data.model.Transaction
import com.fintracker.data.repository.TransactionRepository
import com.fintracker.data.repository.CategoryRepository   // <-- ИМПОРТ
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val transactionRepository: TransactionRepository = TransactionRepository(),
    private val categoryRepository: CategoryRepository = CategoryRepository()   // <-- ДОБАВЛЕНО
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private var currentType = "expense"

    init {
        loadCategories()
    }

    fun setType(type: String) {
        currentType = type
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            // Используем categoryRepository с реальным временем
            categoryRepository.getCategoriesRealtime(currentType).collect { result ->
                result.fold(
                    onSuccess = { _categories.value = it },
                    onFailure = { /* обработка ошибки */ }
                )
            }
        }
    }

    fun saveTransaction(
        amount: Double,
        type: String,
        categoryId: String,
        categoryName: String,
        description: String,
        date: Timestamp
    ) {
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            val transaction = Transaction(
                amount = amount,
                type = type,
                categoryId = categoryId,
                categoryName = categoryName,
                description = description,
                date = date
            )
            val result = transactionRepository.addTransaction(transaction)
            _saveState.value = result.fold(
                onSuccess = { SaveState.Success },
                onFailure = { SaveState.Error(it.message ?: "Ошибка сохранения") }
            )
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    sealed class SaveState {
        object Idle : SaveState()
        object Loading : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }
}