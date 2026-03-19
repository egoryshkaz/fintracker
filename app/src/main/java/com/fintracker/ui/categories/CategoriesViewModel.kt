package com.fintracker.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintracker.data.model.Category
import com.fintracker.data.repository.CategoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.fintracker.ui.categories.getIconVector

class CategoriesViewModel(
    private val repository: CategoryRepository = CategoryRepository()
) : ViewModel() {

    private val _typeFilter = MutableStateFlow<String?>(null)
    val typeFilter: StateFlow<String?> = _typeFilter.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _operationResult = MutableSharedFlow<OperationResult>()
    val operationResult: SharedFlow<OperationResult> = _operationResult.asSharedFlow()

    init {
        observeCategories()
    }

    fun setTypeFilter(type: String?) {
        _typeFilter.value = type
    }

    private fun observeCategories() {
        viewModelScope.launch {
            repository.getCategoriesRealtime(_typeFilter.value).collect { result ->
                result.fold(
                    onSuccess = { _categories.value = it },
                    onFailure = {
                        _operationResult.emit(OperationResult.Error(it.message ?: "Ошибка загрузки"))
                    }
                )
            }
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.addCategory(category)
            result.fold(
                onSuccess = {
                    _operationResult.emit(OperationResult.Success("Категория добавлена"))
                },
                onFailure = {
                    _operationResult.emit(OperationResult.Error(it.message ?: "Ошибка добавления"))
                }
            )
            _isLoading.value = false
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateCategory(category)
            result.fold(
                onSuccess = {
                    _operationResult.emit(OperationResult.Success("Категория обновлена"))
                },
                onFailure = {
                    _operationResult.emit(OperationResult.Error(it.message ?: "Ошибка обновления"))
                }
            )
            _isLoading.value = false
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.deleteCategory(categoryId)
            result.fold(
                onSuccess = {
                    _operationResult.emit(OperationResult.Success("Категория удалена"))
                },
                onFailure = {
                    _operationResult.emit(OperationResult.Error(it.message ?: "Ошибка удаления"))
                }
            )
            _isLoading.value = false
        }
    }

    fun clearOperationResult() {
        viewModelScope.launch {
            _operationResult.emit(OperationResult.Idle)
        }
    }

    sealed class OperationResult {
        object Idle : OperationResult()
        data class Success(val message: String) : OperationResult()
        data class Error(val message: String) : OperationResult()
    }
}