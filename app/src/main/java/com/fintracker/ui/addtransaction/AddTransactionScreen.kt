package com.fintracker.ui.addtransaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.fintracker.ui.categories.getIconVector
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
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
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var amountText by remember { mutableStateOf("0") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var transactionType by remember { mutableStateOf("expense") }

    LaunchedEffect(saveState) {
        when (saveState) {
            is AddTransactionViewModel.SaveState.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Транзакция добавлена")
                }
                onTransactionAdded()
            }
            is AddTransactionViewModel.SaveState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = (saveState as AddTransactionViewModel.SaveState.Error).message,
                        duration = SnackbarDuration.Long
                    )
                }
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Переключатель
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

            Spacer(modifier = Modifier.height(16.dp))

            // Сумма
            Text(
                text = "${amountText} ₽",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Категории (занимают доступное пространство)
            Text("Категория", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)   // <-- занимает всё доступное место
                    .fillMaxWidth()
            ) {
                if (categories.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Нет категорий. Создайте в настройках.", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
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
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Дата и описание
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

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание (необязательно)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Клавиатура
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
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Выберите категорию")
                        }
                        return@NumberKeyboard
                    }
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount <= 0) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Введите сумму больше 0")
                        }
                        return@NumberKeyboard
                    }
                    viewModel.saveTransaction(
                        amount = amount,
                        type = transactionType,
                        categoryId = selectedCategory!!.id,
                        categoryName = selectedCategory!!.name,
                        description = description,
                        date = Timestamp(selectedDate)
                    )
                }
            )
        }
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
                imageVector = getIconVector(category.icon),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

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
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Выберите дату (реализация с календарём будет позже)")
        }
    }
}