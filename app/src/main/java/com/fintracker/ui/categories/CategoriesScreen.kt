package com.fintracker.ui.categories

import android.graphics.Color as AndroidColor
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fintracker.data.model.Category
import com.fintracker.ui.categories.getIconVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoriesViewModel = viewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    // Показывать сообщения об успехе/ошибке
    LaunchedEffect(Unit) {
        viewModel.operationResult.collect { result ->
            when (result) {
                is CategoriesViewModel.OperationResult.Success -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
                is CategoriesViewModel.OperationResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Категории") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Фильтр по типу
                    IconButton(onClick = {
                        viewModel.setTypeFilter(if (typeFilter == null) "expense" else null)
                    }) {
                        Icon(
                            if (typeFilter == "expense") Icons.Default.FilterAlt
                            else Icons.Default.FilterAltOff,
                            contentDescription = "Фильтр"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить категорию")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && categories.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (categories.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (typeFilter != null) "Нет категорий этого типа" else "Нет категорий",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Нажмите +, чтобы добавить",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryCard(
                            category = category,
                            onEdit = { editingCategory = it },
                            onDelete = { viewModel.deleteCategory(it.id) }
                        )
                    }
                }
            }
        }
    }

    // Диалог добавления/редактирования
    if (showAddDialog || editingCategory != null) {
        CategoryDialog(
            category = editingCategory,
            onDismiss = {
                showAddDialog = false
                editingCategory = null
            },
            onSave = { category ->
                if (editingCategory != null) {
                    viewModel.updateCategory(category)
                } else {
                    viewModel.addCategory(category)
                }
                showAddDialog = false
                editingCategory = null
            }
        )
    }
}

@Composable
fun CategoryCard(
    category: Category,
    onEdit: (Category) -> Unit,
    onDelete: (Category) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Цветной кружок
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Color(AndroidColor.parseColor(category.color))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconVector(category.icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (category.type == "expense") "Расход" else "Доход",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (category.budgetLimit != null) {
                    Text(
                        text = "Лимит: ${category.budgetLimit} ₽",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row {
                IconButton(onClick = { onEdit(category) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                }
                IconButton(onClick = { onDelete(category) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var type by remember { mutableStateOf(category?.type ?: "expense") }
    var selectedColor by remember {
        mutableStateOf(
            category?.color?.let { Color(AndroidColor.parseColor(it)) }
                ?: categoryColors.first()
        )
    }
    var selectedIcon by remember {
        mutableStateOf(category?.icon ?: "folder")   // ← дефолтный ключ
    }
    var budgetLimit by remember {
        mutableStateOf(category?.budgetLimit?.toString() ?: "")
    }
    var showIconPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    if (showIconPicker) {
        IconPickerDialog(
            onIconSelected = { selectedIcon = it },   // сохраняем ключ
            onDismiss = { showIconPicker = false }
        )
    }

    if (showColorPicker) {
        ColorPickerDialog(
            onColorSelected = { selectedColor = it },
            onDismiss = { showColorPicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Новая категория" else "Редактировать") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Название
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Тип
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == "expense",
                        onClick = { type = "expense" },
                        label = { Text("Расход") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == "income",
                        onClick = { type = "income" },
                        label = { Text("Доход") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Иконка
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Иконка: ", modifier = Modifier.weight(1f))
                    IconButton(onClick = { showIconPicker = true }) {
                        Icon(Icons.Default.Image, contentDescription = "Выбрать иконку")
                    }
                    // Показываем название иконки (не ключ) – можно заменить на красивый значок
                    Text(categoryIconMap[selectedIcon]?.let { "✓" } ?: "Выбрать")
                }

                // Цвет
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Цвет: ", modifier = Modifier.weight(1f))
                    IconButton(onClick = { showColorPicker = true }) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(selectedColor)
                        )
                    }
                }

                // Бюджет (опционально)
                OutlinedTextField(
                    value = budgetLimit,
                    onValueChange = { budgetLimit = it },
                    label = { Text("Лимит (необязательно)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val limit = budgetLimit.toDoubleOrNull()
                        // Формируем HEX цвета
                        val colorHex = String.format("#%06X", 0xFFFFFF and selectedColor.toArgb())
                        onSave(
                            Category(
                                id = category?.id ?: "",
                                userId = "",     // заполнится в репозитории
                                name = name,
                                type = type,
                                icon = selectedIcon,
                                color = colorHex,
                                budgetLimit = limit,
                                isDefault = false
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}