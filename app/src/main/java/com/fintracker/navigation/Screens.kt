package com.fintracker.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object AddTransaction : Screen("add_transaction")
}