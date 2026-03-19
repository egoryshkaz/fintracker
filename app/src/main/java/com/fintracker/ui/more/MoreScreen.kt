package com.fintracker.ui.more

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fintracker.data.repository.AuthRepository
import com.fintracker.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    navController: NavController,
    authRepository: AuthRepository = AuthRepository()
) {
    val scope = rememberCoroutineScope()
    var vibrationEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ещё") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // История операций
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate(Screen.History.route)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("История операций")
                        Icon(Icons.Default.History, contentDescription = null)
                    }
                }
            }

            // Смена аккаунта (выход)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            authRepository.signOut()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Сменить аккаунт (Выход)")
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                    }
                }
            }

            // О разработчиках
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate(Screen.About.route)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("О разработчиках")
                        Icon(Icons.Default.Info, contentDescription = null)
                    }
                }
            }

            // Вибрация (переключатель)
            item {
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
                        Text("Вибрация")
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { vibrationEnabled = it }
                        )
                    }
                }
            }

            // Премиум
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        // Заглушка для будущего
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Премиум")
                        Icon(Icons.Default.Star, contentDescription = null)
                    }
                }
            }
        }
    }
}