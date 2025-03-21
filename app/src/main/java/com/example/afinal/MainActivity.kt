package com.example.afinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.afinal.ui.theme.FinalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinalTheme {
                val navController = rememberNavController()
                MainApp(navController = navController)
            }
        }
    }
}

@Composable
fun MainApp(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(
                    Screen.Exercise.route,
                    Screen.Search.route,
                    Screen.Notifications.route,
                    Screen.Profile.route,
                    Screen.Recipe.route)) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Exercise.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Exercise.route) { ExerciseScreen() }
            composable(Screen.Search.route) { BarcodeScreen() }
            composable(Screen.Notifications.route) { ClassifyScreen() }
            composable(Screen.Recipe.route) { RecipeScreen() }
            composable(Screen.Profile.route) { SettingScreen(navController = navController) }
            composable("login") { LoginRegisterScreen(navController = navController) }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Exercise,
        Screen.Search,
        Screen.Notifications,
        Screen.Recipe,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(imageVector = screen.icon, contentDescription = screen.route) },
                label = { Text(text = screen.route) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

sealed class Screen(val route: String, val icon: ImageVector) {
    object Exercise : Screen("Exercise", Icons.Default.Home)
    object Search : Screen("Pantry", Icons.Default.CheckCircle)
    object Notifications : Screen("Classify", Icons.Default.Face)
    object Recipe : Screen("Recipe", Icons.Default.Menu)
    object Profile : Screen("Settings", Icons.Default.Settings)
}