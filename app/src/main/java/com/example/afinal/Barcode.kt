package com.example.afinal

import LocalAppState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.afinal.data.FoodItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun BarcodeScreen() {
    val appState = LocalAppState.current
    val context = LocalContext.current

    // Initialize ViewModel
    val foodViewModel: FoodViewModel = viewModel(
        factory = FoodViewModelFactory(context.applicationContext as android.app.Application)
    )
    var foodItems by remember { mutableStateOf<List<FoodItem>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    var selectedFoodItem by remember { mutableStateOf<FoodItem?>(null) }

    LaunchedEffect(key1 = true) {
        foodViewModel.allFoodItems.collectLatest { items ->
            foodItems = items
        }
    }

    val scope = rememberCoroutineScope()

    MaterialTheme(
        colorScheme = if (appState.isDarkMode) darkColorScheme() else lightColorScheme()
    ) {
        Box(
            modifier = if (appState.isDarkMode) {
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            } else {
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
            },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Pantry Scanner",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (appState.isDarkMode) Color.White else Color.Black,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { isScanning = true }
                    ) {
                        Text("Scan Food Item")
                    }

                    Button(
                        onClick = {
                            scannedBarcode = null
                            selectedFoodItem = null
                            showAddDialog = true
                        }
                    ) {
                        Text("Add item")
                    }
                }

                if (foodItems.isNotEmpty()) {
                    Text(
                        "Your Pantry Items",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (appState.isDarkMode) Color.White else Color.Black,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        foodItems.forEach { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .weight(1f)
                                    ) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Expires: ${formatDate(item.expiryDate)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Calories: ${item.calories}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        item.barcode?.let {
                                            if (it.isNotEmpty()) {
                                                Text(
                                                    text = "Barcode: $it",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        // Edit button
                                        IconButton(
                                            onClick = {
                                                selectedFoodItem = item
                                                showEditDialog = true
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                selectedFoodItem = item
                                                showDeleteConfirmation = true
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No items in your pantry yet.\nScan a food item or add manually to get started.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (appState.isDarkMode) Color.White else Color.Black,
                        )
                    }
                }
            }
        }

        if (isScanning) {
            AlertDialog(
                onDismissRequest = { isScanning = false },
                title = { Text("Scan Barcode") },
                text = { Text("Simulating barcode scan...") },
                confirmButton = {
                    Button(onClick = {
                        scannedBarcode = "123${(100000..999999).random()}"
                        isScanning = false

                        scope.launch {
                            val existingItem = foodViewModel.getFoodItemByBarcode(scannedBarcode!!)
                            if (existingItem == null) {
                                // Item not in database, show dialog to add details
                                selectedFoodItem = null
                                showAddDialog = true
                            } else {
                                // Item found - show notification
                                selectedFoodItem = existingItem
                                showEditDialog = true
                            }
                        }
                    }) {
                        Text("Simulate Scan")
                    }
                },
                dismissButton = {
                    Button(onClick = { isScanning = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showAddDialog) {
            var itemName by remember { mutableStateOf("") }
            var caloriesText by remember { mutableStateOf("") }
            var expiryDays by remember { mutableStateOf("7") } // Default 7 days

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Food Item") },
                text = {
                    Column {
                        TextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            label = { Text("Food Name") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                        TextField(
                            value = caloriesText,
                            onValueChange = { caloriesText = it },
                            label = { Text("Calories") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                        TextField(
                            value = expiryDays,
                            onValueChange = { expiryDays = it },
                            label = { Text("Expires in (days)") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (itemName.isNotEmpty() && caloriesText.isNotEmpty()) {
                            val calories = caloriesText.toIntOrNull() ?: 0
                            val days = expiryDays.toIntOrNull() ?: 7

                            val expiryDate = Date(System.currentTimeMillis() + days * 24 * 60 * 60 * 1000L)

                            // Add to database
                            foodViewModel.addFoodItem(
                                name = itemName,
                                barcode = scannedBarcode,
                                expiryDate = expiryDate,
                                calories = calories
                            )
                            showAddDialog = false
                        }
                    }) {
                        Text("Add Item")
                    }
                },
                dismissButton = {
                    Button(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showEditDialog && selectedFoodItem != null) {
            var itemName by remember { mutableStateOf(selectedFoodItem!!.name) }
            var caloriesText by remember { mutableStateOf(selectedFoodItem!!.calories.toString()) }
            // Calculate days remaining until expiry
            val daysRemaining = ((selectedFoodItem!!.expiryDate.time - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
            var expiryDays by remember { mutableStateOf(daysRemaining.toString()) }

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Food Item") },
                text = {
                    Column {
                        TextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            label = { Text("Food Name") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                        TextField(
                            value = caloriesText,
                            onValueChange = { caloriesText = it },
                            label = { Text("Calories") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                        TextField(
                            value = expiryDays,
                            onValueChange = { expiryDays = it },
                            label = { Text("Expires in (days)") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (itemName.isNotEmpty() && caloriesText.isNotEmpty()) {
                            val calories = caloriesText.toIntOrNull() ?: 0
                            val days = expiryDays.toIntOrNull() ?: 7

                            val expiryDate = Date(System.currentTimeMillis() + days * 24 * 60 * 60 * 1000L)

                            val updatedItem = selectedFoodItem!!.copy(
                                name = itemName,
                                calories = calories,
                                expiryDate = expiryDate
                            )

                            foodViewModel.updateFoodItem(updatedItem)
                            showEditDialog = false
                        }
                    }) {
                        Text("Update Item")
                    }
                },
                dismissButton = {
                    Button(onClick = { showEditDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showDeleteConfirmation && selectedFoodItem != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Food Item") },
                text = { Text("Are you sure you want to delete ${selectedFoodItem!!.name}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            foodViewModel.deleteFoodItem(selectedFoodItem!!)
                            showDeleteConfirmation = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteConfirmation = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

private fun formatDate(date: Date): String {
    val formatter = java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault())
    return formatter.format(date)
}

class FoodViewModelFactory(private val application: android.app.Application) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}