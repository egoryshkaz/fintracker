package com.fintracker.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Onboarding : Screen("onboarding")
    object Categories : Screen("categories")
    object More : Screen("more")
    object AddTransaction : Screen("add_transaction")
    object About : Screen("about")
    object History : Screen("history")
}