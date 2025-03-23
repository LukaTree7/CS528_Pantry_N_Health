package com.example.afinal

import LocalAppState
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExerciseScreen() {
    val appState = LocalAppState.current

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
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.avatar),
                    contentDescription = "Side Image",
                    modifier = Modifier
                        .size(100.dp)
                        .clickable {
                            println("Side image clicked!")
                        }
                        .padding(end = 16.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HealthMetricCard(
                        iconResId = R.drawable.step,
                        value = "10,000",
                        unit = "steps",
                    )

                    HealthMetricCard(
                        iconResId = R.drawable.dist,
                        value = "5.0",
                        unit = "km",
                    )

                    HealthMetricCard(
                        iconResId = R.drawable.flame,
                        value = "500",
                        unit = "kcal",
                    )
                }
            }
        }
    }
}

@Composable
fun HealthMetricCard(iconResId: Int, value: String, unit: String) {
    val appState = LocalAppState.current
    val backgroundColor = if (appState.isDarkMode) Color.Black else Color.White
    val textColor = if (appState.isDarkMode) Color.White else Color.Black

    Box(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.medium
            )
//            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
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
                color = textColor
            )
        }
    }
}