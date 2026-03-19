package com.fintracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.fintracker.data.prefs.PreferenceManager
import com.fintracker.navigation.NavGraph
import com.fintracker.navigation.Screen
import com.fintracker.ui.theme.FinTrackTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val auth = FirebaseAuth.getInstance()
                    val prefs = PreferenceManager(this)

                    val currentUser = auth.currentUser
                    val startDestination = when {
                        currentUser == null -> Screen.Login.route
                        !prefs.isOnboardingShown(currentUser.uid) -> Screen.Onboarding.route
                        else -> Screen.Main.route
                    }

                    NavGraph(navController = navController, startDestination = startDestination)
                }
            }
        }
    }
}