
package com.example.afinal

import LocalAppState
import android.util.Log
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.afinal.data.FoodItem
import com.example.afinal.worker.ExpiryCheckWorker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.afinal.data.RetrofitInstance


@Composable
fun EnsureCameraPermission(onGranted: @Composable () -> Unit) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            permissionGranted = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (permissionGranted) {
        onGranted()
    }
}



@Composable
fun BarcodeScreen() {
    val appState = LocalAppState.current
    val context = LocalContext.current

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
                Button(
                    onClick = {
                        WorkManager.getInstance(context)
                            .enqueueUniqueWork(
                                "manual_expiry_check",
                                ExistingWorkPolicy.REPLACE,
                                OneTimeWorkRequestBuilder<ExpiryCheckWorker>().build()
                            )
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Check Expiring Items")
                }
                Button(
                    onClick = {
                        val notification = NotificationCompat.Builder(context, "expiry_notification_channel")
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle("Test Notification")
                            .setContentText("This is a test notification")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true)
                            .build()

                        with(NotificationManagerCompat.from(context)) {
                            try {
                                notify(100, notification)
                            } catch (e: SecurityException) {
                                Log.e("Notification", "Permission denied", e)
                                // Handle permission not granted
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Test Notification")
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
                text = {
                    RealBarcodeScanner(
                        onDetected = { barcode ->
                            scannedBarcode = barcode
                            isScanning = false

                            scope.launch {
                                try {
                                    val response = RetrofitInstance.api.getProduct(barcode)
                                    val product = response.product

                                    val name = product?.product_name ?: ""
                                    val calories = product?.nutriments?.energy_kcal?.toInt() ?: 0

                                    selectedFoodItem = FoodItem(
                                        name = name,
                                        barcode = barcode,
                                        expiryDate = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L),
                                        calories = calories
                                    )

                                    showAddDialog = true

                                } catch (e: Exception) {
                                    // Product not found or network error
                                    selectedFoodItem = FoodItem(
                                        name = "",
                                        barcode = barcode,
                                        expiryDate = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L),
                                        calories = 0
                                    )
                                    showAddDialog = true
                                }
                            }
                        }




                    )
                },
                confirmButton = {},
                dismissButton = {
                    Button(onClick = { isScanning = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showAddDialog) {
            var itemName by remember { mutableStateOf(selectedFoodItem?.name ?: "") }
            var caloriesText by remember { mutableStateOf(selectedFoodItem?.calories?.toString() ?: "") }
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

@Composable
fun RealBarcodeScanner(onDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val preview = remember { Preview.Builder().build() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val analyzer = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply {
                setAnalyzer(ContextCompat.getMainExecutor(context), BarcodeAnalyzer(onDetected))
            }
    }

    LaunchedEffect(Unit) {
        val cameraProvider = cameraProviderFuture.get()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner, cameraSelector, preview, analyzer
        )
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { ctx ->
            PreviewView(ctx).apply {
                preview.setSurfaceProvider(this.surfaceProvider)
            }
        }
    )
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


class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        // Convert ImageProxy to NV21 byte array (common format for ML Kit)
        val nv21Data = imageProxy.toNV21ByteArray()

        // Create InputImage from the byte array
        val image = InputImage.fromByteArray(
            nv21Data,
            imageProxy.width,
            imageProxy.height,
            imageProxy.imageInfo.rotationDegrees,
            InputImage.IMAGE_FORMAT_NV21
        )

        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let {
                        onBarcodeDetected(it)
                        imageProxy.close()
                        return@addOnSuccessListener
                    }
                }
                imageProxy.close()
            }
            .addOnFailureListener {
                imageProxy.close()
            }
    }

    private fun ImageProxy.toNV21ByteArray(): ByteArray {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Copy Y channel
        yBuffer.get(nv21, 0, ySize)

        // Copy VU channels (NV21 format expects VU, not UV)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        return nv21
    }
}