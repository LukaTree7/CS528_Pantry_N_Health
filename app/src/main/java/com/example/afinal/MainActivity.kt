package com.example.afinal

import AppState
import LocalAppState
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.afinal.notification.NotificationHelper
import com.example.afinal.ui.theme.FinalTheme
import com.example.afinal.worker.ExpiryCheckWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    // Add permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Optional: handle permission result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Request notification permission for Android 13+
        requestNotificationPermission()

        // Schedule expiry check worker
        scheduleExpiryCheck()

        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val appState = remember { AppState(authViewModel) }
            CompositionLocalProvider(LocalAppState provides appState) {
                FinalTheme {
                    val navController = rememberNavController()
                    MainApp(navController = navController)
                }
            }
        }
    }

    // Method to request notification permission
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Optional: explain why permission is needed
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    // Method to schedule the expiry check worker
    private fun scheduleExpiryCheck() {
        val expiryCheckRequest = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "expiry_check",
            ExistingPeriodicWorkPolicy.KEEP,
            expiryCheckRequest
        )
    }
}

@Composable
fun MainApp(navController: NavHostController) {
    // Existing code remains unchanged
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val appState = LocalAppState.current
    val isDarkMode = appState.isDarkMode

    Scaffold(
        modifier = Modifier.background(if (isDarkMode) Color.Black else Color.White),
        containerColor = if (isDarkMode) Color.Black else Color.White,
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
            composable(Screen.Exercise.route) { ExerciseScreen(navController = navController) }
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
    // Existing code remains unchanged
    val items = listOf(
        Screen.Exercise,
        Screen.Search,
        Screen.Notifications,
        Screen.Recipe,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val appState = LocalAppState.current
    val isDarkMode = appState.isDarkMode
    val fontSize = appState.fontSize

    NavigationBar(
        modifier = Modifier.background(if (isDarkMode) Color.DarkGray else Color.LightGray),
        containerColor = if (isDarkMode) Color.DarkGray else Color.LightGray
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    when {
                        screen.iconVector != null -> {
                            Icon(
                                imageVector = screen.iconVector,
                                contentDescription = screen.route,
                                modifier = Modifier.size(appState.fontSize.dp + 5.dp),
                                tint = if (isDarkMode) Color.White else Color.Black
                            )
                        }
                        screen.iconResId != null -> {
                            Icon(
                                painter = painterResource(id = screen.iconResId),
                                contentDescription = screen.route,
                                modifier = Modifier.size(appState.fontSize.dp + 5.dp),
                                tint = if (isDarkMode) Color.White else Color.Black
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = screen.route,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = (fontSize - 5).sp),
                        color = if (isDarkMode) Color.White else Color.Black
                    )
                },
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

sealed class Screen(val route: String, val iconResId: Int? = null, val iconVector: ImageVector? = null) {
    object Exercise : Screen("Exercise", iconVector = Icons.Default.Home)
    object Search : Screen("Pantry", iconVector = Icons.Rounded.ShoppingCart)
    object Notifications : Screen("Classify", iconResId = R.drawable.flaky)
    object Recipe : Screen("Recipe", iconResId = R.drawable.cook)
    object Profile : Screen("Settings", iconVector = Icons.Default.Settings)
}