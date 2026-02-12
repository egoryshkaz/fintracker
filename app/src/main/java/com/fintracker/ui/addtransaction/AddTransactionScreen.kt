package com.fintracker.ui.addtransaction

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintracker.data.model.Category
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onTransactionAdded: () -> Unit,
    viewModel: AddTransactionViewModel = viewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val context = LocalContext.current

    var amountText by remember { mutableStateOf("0") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var transactionType by remember { mutableStateOf("expense") }

    // Обработка состояния сохранения
    LaunchedEffect(saveState) {
        when (saveState) {
            is AddTransactionViewModel.SaveState.Success -> {
                Toast.makeText(context, "Транзакция добавлена", Toast.LENGTH_SHORT).show()
                onTransactionAdded()
                viewModel.resetSaveState()
            }
            is AddTransactionViewModel.SaveState.Error -> {
                Toast.makeText(context, (saveState as AddTransactionViewModel.SaveState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    // Дата пикер
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Переключатель "Расход / Доход"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                selected = transactionType == "expense",
                onClick = {
                    transactionType = "expense"
                    viewModel.setType("expense")
                    selectedCategory = null
                },
                label = { Text("Расход") },
                modifier = Modifier.padding(end = 8.dp)
            )
            FilterChip(
                selected = transactionType == "income",
                onClick = {
                    transactionType = "income"
                    viewModel.setType("income")
                    selectedCategory = null
                },
                label = { Text("Доход") }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Поле суммы
        Text(
            text = "${amountText} ₽",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Выбор категории
        Text("Категория", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (categories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет категорий. Создайте в настройках.", color = MaterialTheme.colorScheme.error)
            }
        } else {
            LazyColumn(
                modifier = Modifier.height(200.dp)
            ) {
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        isSelected = selectedCategory?.id == category.id,
                        onSelect = { selectedCategory = category }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Дата
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Дата", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { showDatePicker = true }) {
                Text(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedDate))
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.DateRange, contentDescription = "Выбрать дату")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Описание
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание (необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.weight(1f))

        // Кастомная клавиатура
        NumberKeyboard(
            onNumberClick = { digit ->
                if (digit == ".") {
                    if (!amountText.contains(".")) {
                        amountText += digit
                    }
                } else {
                    if (amountText == "0" && digit != ".") {
                        amountText = digit
                    } else {
                        amountText += digit
                    }
                }
            },
            onDeleteClick = {
                amountText = if (amountText.length > 1) {
                    amountText.dropLast(1)
                } else {
                    "0"
                }
            },
            onDoneClick = {
                if (selectedCategory == null) {
                    Toast.makeText(context, "Выберите категорию", Toast.LENGTH_SHORT).show()
                    return@NumberKeyboard
                }
                val amount = amountText.toDoubleOrNull() ?: 0.0
                if (amount <= 0) {
                    Toast.makeText(context, "Введите сумму больше 0", Toast.LENGTH_SHORT).show()
                    return@NumberKeyboard
                }
                viewModel.saveTransaction(
                    amount = amount,
                    categoryId = selectedCategory!!.id,
                    categoryName = selectedCategory!!.name,
                    description = description,
                    date = Timestamp(selectedDate)
                )
            }
        )
    }
}

@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder, // TODO: загружать по имени
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge
            )
            if (category.budgetLimit != null) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Лимит: ${category.budgetLimit} ₽",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun DatePickerDialog(
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    @OptIn(ExperimentalMaterial3Api::class)
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                calendar.set(year, month, day)
                onDateSelected(calendar.time)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    ) {
        // TODO: реализовать полноценный DatePicker через rememberDatePickerState
        // Пока просто статичный диалог
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Выберите дату (реализация с календарём будет позже)")
            // В продакшене используйте rememberDatePickerState
        }
    }
}