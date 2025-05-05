package com.example.afinal

import AppState
import LocalAppState
import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@SuppressLint("DefaultLocale")
@Composable
fun ExerciseScreen(
    navController: NavHostController,
    geoviewModel: GeofenceViewModel = viewModel(factory = GeofenceViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context.applicationContext as Application)
    )
    val currentAccount by authViewModel.currentAccount.collectAsStateWithLifecycle()

    val appState = remember { AppState(authViewModel) }
    val application = context.applicationContext as Application

    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(LocalContext.current.applicationContext as Application)
    )

    val stepsViewModel: StepsViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    )

    val visitPC by geoviewModel.visitPC.collectAsState()

    var showUserInfoDialog by remember { mutableStateOf(false) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    // Calculate derived metrics
    val distanceKm = remember(appState.stepCount) {
        String.format("%.1f", appState.stepCount * 0.000762)
    }

    val caloriesBurned = remember(appState.stepCount) {
        String.format("%.1f", appState.stepCount * 0.04)
    }

    val bmi = remember(height, weight) {
        if (height.isNotBlank() && weight.isNotBlank()) {
            val h = height.toFloatOrNull()
            val w = weight.toFloatOrNull()
            if (h != null && w != null && h > 0 && w > 0) {
                String.format("%.1f", w / ((h / 100) * (h / 100)))
            } else {
                ""
            }
        } else {
            ""
        }
    }

    val ageInt = age.toIntOrNull()

    val recommendedSteps = remember(bmi, ageInt) {
        if (bmi != null && ageInt != null && ageInt > 0) {
            getStepRecommendation(bmi.toFloat(), ageInt)
        } else null
    }

    // Add Geofence
    LaunchedEffect(Unit) {
        addGeofences(
            context,
            visitPC
        )
    }

    DisposableEffect(Unit) {
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.getStringExtra("location")) {
                    "PriceChopper" -> geoviewModel.incrementVisitPC()
                }
            }
        }

        val filter = IntentFilter("geofence_transition")
        ContextCompat.registerReceiver(
            context,
            broadcastReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(broadcastReceiver)
        }
    }

    LaunchedEffect(currentAccount) {
        currentAccount?.let { account ->
            appState.username = account.username
            account.height?.let { height = it.toString() }
            account.weight?.let { weight = it.toString() }
            account.age?.let { age = it.toString() }
        }
    }

    LaunchedEffect(currentAccount, appState.stepCount) {
        while (true) {
            delay(60_000L)

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val currentDate = LocalDate.now().format(formatter)

            currentAccount?.let {
                stepsViewModel.saveSteps(
                    username = it.username,
                    date = currentDate,
                    steps = appState.stepCount
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        val workRequest = PeriodicWorkRequestBuilder<ResetStepWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelayToSixAM(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "ResetStepWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

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
            Column (
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Spacer(modifier = Modifier.width(16.dp))

                    Image(
                        painter = painterResource(id = R.drawable.avatar),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(140.dp)
                            .clickable {
                                if (currentAccount != null) {
                                    showUserInfoDialog = true
                                }
                            }
                            .padding(end = 16.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        HealthMetricCard(
                            iconResId = R.drawable.step,
                            value = "${ appState.stepCount }",
                            unit = "steps",
                        )
                        HealthMetricCard(
                            iconResId = R.drawable.dist,
                            value = distanceKm,
                            unit = "km",
                        )
                        HealthMetricCard(
                            iconResId = R.drawable.flame,
                            value = caloriesBurned,
                            unit = "kcal",
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Daily Health Tip",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )

                        if( currentAccount != null ) {
                            if( appState.height.isNotEmpty() && appState.weight.isNotEmpty() && appState.age.isNotEmpty()) {
                                Text(
                                    text = "Every step you take brings you closer to your goal. Aim for $recommendedSteps steps today — it's not just about walking, it's about creating a healthier, stronger you. Keep moving, stay motivated, and remember that progress, no matter how small, is still progress!",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Button(
                                    onClick = {  },
                                    modifier = Modifier.align(Alignment.End),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Fighting!")
                                }
                            } else {
                                Text(
                                    text = "You haven't filled personal information yet. Please fill the following table first to create your personal exercise plan.",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Button(
                                    onClick = { showUserInfoDialog = true },
                                    modifier = Modifier.align(Alignment.End),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Fill")
                                }
                            }
                        } else {
                            Text(
                                text = "You haven't logged in yet. Please login first to create your personal exercise plan.",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Button(
                                onClick = { navController.navigate("login") },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Login")
                            }
                        }
                    }
                }

                WeeklyStepsChart(
                    stepsViewModel = viewModel(),
                    username = appState.username,
                    appState = appState
                )

            }

            if (showUserInfoDialog) {
                Dialog(
                    onDismissRequest = { showUserInfoDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Edit Profile Information",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = appState.fontSize.sp),
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = height,
                                onValueChange = { height = it },
                                label = { Text("Height (cm)") },
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = weight,
                                onValueChange = { weight = it },
                                label = { Text("Weight (kg)") },
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it },
                                label = { Text("Age") },
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { showUserInfoDialog = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Text("Cancel")
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        viewModel.updateProfile(
                                            height = height.toIntOrNull(),
                                            weight = weight.toIntOrNull(),
                                            age = age.toIntOrNull()
                                        )

                                        showUserInfoDialog = false

                                        appState.height = height
                                        appState.weight = weight
                                        appState.age = age
                                    }
                                ) {
                                    Text("OK")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val stepSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) }

    if (stepSensor == null) {
        Toast.makeText(context, "Step counter sensor not available!", Toast.LENGTH_SHORT).show()
    }

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                        appState.stepCount = it.values[0].toInt()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(sensorManager, stepSensor) {
        stepSensor?.let {
            sensorManager.registerListener(
                sensorListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }
}

fun getStepRecommendation(bmi: Float, age: Int): Int {
    return when {
        age < 18 -> 8000
        age < 40 -> {
            when {
                bmi < 18.5 -> 7000
                bmi < 25 -> 9000
                bmi < 30 -> 11000
                else -> 13000
            }
        }
        age < 60 -> {
            when {
                bmi < 18.5 -> 6000
                bmi < 25 -> 8000
                bmi < 30 -> 10000
                else -> 12000
            }
        }
        else -> {
            when {
                bmi < 18.5 -> 5000
                bmi < 25 -> 7000
                bmi < 30 -> 9000
                else -> 11000
            }
        }
    }
}

@Composable
fun HealthMetricCard(iconResId: Int, value: String, unit: String) {
    val appState = LocalAppState.current
    val backgroundColor = if (appState.isDarkMode) Color.Black else Color.White
    val textColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box {
                Row (
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineLarge,
                        color = textColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyStepsChart(
    stepsViewModel: StepsViewModel,
    username: String,
    appState: AppState
) {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val labelFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

    val dates = remember {
        (0..6).map {
            calendar.apply { time = Date(); add(Calendar.DAY_OF_YEAR, it - 6) }
            dateFormat.format(calendar.time)
        }
    }

    val labels = dates.map {
        val parsed = dateFormat.parse(it)
        labelFormat.format(parsed ?: Date())
    }

    val stepsList by stepsViewModel.getAllSteps(username).collectAsState(initial = emptyList())

    val stepData = dates.map { date ->
        stepsList.find { it.date == date }?.steps ?: 0
    }

    val maxSteps = stepData.maxOrNull() ?: 1
    val maxIndex = stepData.indexOf(maxSteps)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(16.dp)
    ) {
        val barColor = MaterialTheme.colorScheme.primaryContainer

        Text(
            text = "Weekly Activity",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = appState.fontSize.sp),
            modifier = Modifier.align(Alignment.TopCenter),
            color = if (appState.isDarkMode) Color.White else Color.Black
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 32.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barWidth = size.width / (dates.size * 1.8f)

                    stepData.forEachIndexed { index, steps ->
                        val barHeight = (steps.toFloat() / maxSteps) * size.height
                        val barX = (index + 0.2f) * (size.width / dates.size)
                        val barY = size.height - barHeight

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(barX, barY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4f)
                        )

                        if (index == maxIndex) {
                            drawContext.canvas.nativeCanvas.drawText(
                                "$steps",
                                barX + barWidth / 2,
                                barY - 8,
                                android.graphics.Paint().apply {
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    textSize = 32f
                                    color = android.graphics.Color.BLACK
                                    isFakeBoldText = true
                                }
                            )
                        }
                    }
                }
            }

            val dateBoxWidth = remember { 1f / (labels.size * 1.8f) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Box(
                        modifier = Modifier
                            .weight(dateBoxWidth)
                            .padding(top = 4.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = (appState.fontSize-2).sp),
                            color = if (appState.isDarkMode) Color.White else Color.Black
                        )
                    }
                }
            }
        }
    }
}

class ResetStepWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val sharedPref = applicationContext.getSharedPreferences("StepData", Context.MODE_PRIVATE)
        sharedPref.edit().putInt("steps", 0).apply()
        return Result.success()
    }
}

fun calculateInitialDelayToSixAM(): Long {
    val now = LocalDateTime.now()
    val targetTime = now.withHour(6).withMinute(0).withSecond(0).withNano(0)
    val delay = if (now.isAfter(targetTime)) {
        Duration.between(now, targetTime.plusDays(1))
    } else {
        Duration.between(now, targetTime)
    }
    return delay.toMillis()
}

@SuppressLint("MissingPermission")
private fun addGeofences(
    context: Context,
    visitPC: Float
) {
    val geofencingClient = LocationServices.getGeofencingClient(context)

    val geofenceList = ArrayList<Geofence>()

    // Price Chopper Geofence
    val priceChopper = LatLng(42.27087, -71.81515)
    geofenceList.add(
        Geofence.Builder()
            .setRequestId("PriceChopper")
            .setCircularRegion(
                priceChopper.latitude,
                priceChopper.longitude,
                50f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(5000)
            .build()
    )

    val geofencingRequest = GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_DWELL)
        .addGeofences(geofenceList)
        .build()

    val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
        addOnSuccessListener {
            Toast.makeText(context, "Geofences added", Toast.LENGTH_SHORT).show()
        }
        addOnFailureListener {
            Toast.makeText(context, "Failed to add geofences", Toast.LENGTH_SHORT).show()
        }
    }
}

class GeofenceViewModel(context: Context) : ViewModel() {
    private val sharedPreferences = context.getSharedPreferences("GeofencePrefs", Context.MODE_PRIVATE)

    private val _visitPC = MutableStateFlow(sharedPreferences.getFloat("visitPC", 0f))
    val visitPC: StateFlow<Float> = _visitPC

    fun incrementVisitPC() {
        viewModelScope.launch {
            _visitPC.value += 1f
            sharedPreferences.edit().putFloat("visitPC", _visitPC.value).apply()
        }
    }
}

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
                Log.e("Geofence", errorMessage)
                return
            }
        }

        val geofenceTransition = geofencingEvent?.geofenceTransition

        Log.d("Geofence", "Broadcast received with transition: $geofenceTransition")

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER, Geofence.GEOFENCE_TRANSITION_DWELL -> {
                val triggeringGeofences = geofencingEvent.triggeringGeofences
                triggeringGeofences?.forEach { geofence ->
                    when (geofence.requestId) {
                        "PriceChopper" -> {
                            Toast.makeText(
                                context,
                                "You are approaching Price Chopper, it's time to get some fresh!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }
}

class GeofenceViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeofenceViewModel::class.java)) {
            return GeofenceViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}