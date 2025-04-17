package com.example.afinal

import LocalAppState
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@SuppressLint("DefaultLocale")
@Composable
fun ExerciseScreen() {
    val appState = LocalAppState.current
    val context = LocalContext.current

    // Add these state variables
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
                            .size(160.dp)
                            .clickable { showUserInfoDialog = true }
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

                        Text(
                            text = "Walking 8,000 steps daily improves cardiovascular health. Keep up your current activity level.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Walking 8,000 steps daily improves cardiovascular health. Keep up your current activity level.",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = (appState.fontSize - 5).sp),
                        )

                        Button(
                            onClick = { /* Navigate to details */ },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("View Details")
                        }
                    }
                }

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

                    val calendar = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -6)
                    }
                    val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                    val dates = List(7) { i ->
                        if (i > 0) calendar.add(Calendar.DAY_OF_YEAR, 1)
                        dateFormat.format(calendar.time)
                    }

                    val stepData = listOf(8000, 10000, 7500, 9000, 12000, 6000, 11000)
                    val maxSteps = stepData.maxOrNull() ?: 1

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

                                dates.forEachIndexed { index, _ ->
                                    val barHeight = (stepData[index].toFloat() / maxSteps) * size.height

                                    drawRoundRect(
                                        color = barColor,
                                        topLeft = Offset(
                                            x = (index + 0.2f) * (size.width / dates.size),
                                            y = size.height - barHeight
                                        ),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(4f)
                                    )
                                }
                            }
                        }

                        val dateBoxWidth = remember { 1f / (dates.size * 1.8f) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            dates.forEach { date ->
                                Box(
                                    modifier = Modifier
                                        .weight(dateBoxWidth)
                                        .padding(top = 4.dp),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Text(
                                        text = date,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = appState.fontSize.sp),
                                        color = if (appState.isDarkMode) Color.White else Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
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
                                        // Save the information here
                                        showUserInfoDialog = false
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