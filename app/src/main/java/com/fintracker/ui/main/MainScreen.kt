package com.fintracker.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fintracker.data.model.Transaction
import com.fintracker.data.model.CategoryAmount
import com.fintracker.navigation.Screen
import com.fintracker.ui.categories.getIconVector
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val monthlyIncome by viewModel.monthlyIncome.collectAsState()
    val monthlyExpenses by viewModel.monthlyExpenses.collectAsState()
    val dailyIncome by viewModel.dailyIncome.collectAsState()
    val dailyExpenses by viewModel.dailyExpenses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FinTrack") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Categories.route) }) {
                        Icon(Icons.Default.Category, contentDescription = "Категории")
                    }
                    IconButton(onClick = { navController.navigate(Screen.More.route) }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Ещё")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddTransaction.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Баланс
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Текущий баланс",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "%.2f ₽".format(balance),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Итоги за день
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Сегодня", style = MaterialTheme.typography.titleMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Доходы", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = "%.2f ₽".format(dailyIncome),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Расходы", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = "%.2f ₽".format(dailyExpenses),
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Итоги за месяц
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Доходы за месяц", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = "%.2f ₽".format(monthlyIncome),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Card(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Расходы за месяц", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = "%.2f ₽".format(monthlyExpenses),
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Диаграмма доходов
            if (incomeCategories.isNotEmpty()) {
                item {
                    CategoryPieChart(
                        title = "Доходы по категориям",
                        data = incomeCategories,
                        total = monthlyIncome
                    )
                }
            }

            // Диаграмма расходов
            if (expenseCategories.isNotEmpty()) {
                item {
                    CategoryPieChart(
                        title = "Расходы по категориям",
                        data = expenseCategories,
                        total = monthlyExpenses
                    )
                }
            }

            // Заголовок последних операций
            item {
                Text(
                    text = "Последние операции",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Список транзакций
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (transactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Нет операций. Добавьте первую!")
                        }
                    }
                }
            } else {
                items(transactions.take(10)) { transaction ->
                    TransactionItem(transaction = transaction)
                }
            }
        }
    }
}

@Composable
fun CategoryPieChart(
    title: String,
    data: List<CategoryAmount>,
    total: Double,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (total == 0.0) {
                Text("Нет данных за месяц", style = MaterialTheme.typography.bodyMedium)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Круговая диаграмма
                    Canvas(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.CenterVertically)
                    ) {
                        var startAngle = 0f
                        data.forEach { slice ->
                            val sweepAngle = (slice.amount / total * 360).toFloat()
                            drawArc(
                                color = Color(android.graphics.Color.parseColor(slice.color)),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                style = Fill
                            )
                            startAngle += sweepAngle
                        }
                    }

                    // Легенда с иконками
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        data.take(5).forEach { slice ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = getIconVector(slice.iconName),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(android.graphics.Color.parseColor(slice.color))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${slice.categoryName}: ${String.format("%.1f", slice.amount / total * 100)}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        if (data.size > 5) {
                            Text("... и ещё ${data.size - 5}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, showDate: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.categoryName,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = transaction.description.ifEmpty { "Без описания" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (showDate) {
                    Text(
                        text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(transaction.date.toDate()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            Text(
                text = "${if (transaction.type == "expense") "-" else "+"} %.2f ₽".format(transaction.amount),
                color = if (transaction.type == "expense")
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}