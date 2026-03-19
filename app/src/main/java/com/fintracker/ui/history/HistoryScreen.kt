package com.fintracker.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fintracker.ui.main.TransactionItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = viewModel()
) {
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История операций") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Сортировка")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Сначала новые") },
                            onClick = {
                                viewModel.setSortOption(SortOption.DATE_NEWEST)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Сначала старые") },
                            onClick = {
                                viewModel.setSortOption(SortOption.DATE_OLDEST)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("По сумме (возрастание)") },
                            onClick = {
                                viewModel.setSortOption(SortOption.AMOUNT_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("По сумме (убывание)") },
                            onClick = {
                                viewModel.setSortOption(SortOption.AMOUNT_DESC)
                                showSortMenu = false
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            FilterTypeChips(
                filterType = filterType,
                onFilterTypeChange = { viewModel.setFilterType(it) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Карточка итогов месяца
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Итоги месяца", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Доходы: ${String.format("%.2f", totalIncome)} ₽")
                        Text("Расходы: ${String.format("%.2f", totalExpense)} ₽")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val net = totalIncome - totalExpense
                    Text(
                        text = "Чистая экономия: ${String.format("%.2f", net)} ₽",
                        color = if (net >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Итоговая сумма по отфильтрованным операциям
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Итого: ${String.format("%.2f", totalAmount)} ₽",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Список транзакций
            if (isLoading && filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Нет операций по заданным фильтрам")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTransactions) { transaction ->
                        TransactionItem(transaction = transaction, showDate = true)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Поиск по категории или описанию...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Очистить")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
fun FilterTypeChips(
    filterType: FilterType,
    onFilterTypeChange: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = filterType == FilterType.ALL,
            onClick = { onFilterTypeChange(FilterType.ALL) },
            label = { Text("Все") }
        )
        FilterChip(
            selected = filterType == FilterType.EXPENSE,
            onClick = { onFilterTypeChange(FilterType.EXPENSE) },
            label = { Text("Расходы") }
        )
        FilterChip(
            selected = filterType == FilterType.INCOME,
            onClick = { onFilterTypeChange(FilterType.INCOME) },
            label = { Text("Доходы") }
        )
    }
}