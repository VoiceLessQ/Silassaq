package com.example.silassaq.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.silassaq.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

/**
 * Aurora forecast component for Greenland
 */
@Composable
fun AuroraForecast(
    kpIndex: Float = 3.5f, // KP index (0-9 scale)
    viewingProbability: Float = 65f, // Probability percentage
    bestViewingTime: LocalDateTime = LocalDateTime.now().plusHours(3), // Best time to view
    cloudCover: Float = 25f, // Cloud cover percentage
    isDarkMode: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) DarkNavyCardLight else CardLight
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Aurora Forecast",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else MeteoBlue
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // KP Index visualization
            KpIndexDisplay(kpIndex = kpIndex, isDarkMode = isDarkMode)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Viewing probability
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Viewing Probability:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else TextSecondaryLight,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "${viewingProbability.toInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        viewingProbability > 70f -> if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32)
                        viewingProbability > 40f -> if (isDarkMode) Color(0xFFFFD54F) else Color(0xFFF57F17)
                        else -> if (isDarkMode) Color(0xFFE57373) else Color(0xFFB71C1C)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Best viewing time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Best Viewing Time:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else TextSecondaryLight,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = bestViewingTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) AccentBlue else MeteoBlue
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Cloud cover
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cloud Cover:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else TextSecondaryLight,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "${cloudCover.toInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        cloudCover < 30f -> if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32)
                        cloudCover < 70f -> if (isDarkMode) Color(0xFFFFD54F) else Color(0xFFF57F17)
                        else -> if (isDarkMode) Color(0xFFE57373) else Color(0xFFB71C1C)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Viewing recommendation
            val recommendation = when {
                viewingProbability > 70f && cloudCover < 30f -> "Excellent viewing conditions tonight!"
                viewingProbability > 50f && cloudCover < 50f -> "Good chance of aurora visibility tonight."
                viewingProbability > 30f && cloudCover < 70f -> "Moderate chance of seeing auroras."
                else -> "Poor viewing conditions tonight."
            }
            
            Text(
                text = recommendation,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isDarkMode) Color.White else TextPrimaryLight,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * KP Index display with scale visualization
 */
@Composable
private fun KpIndexDisplay(
    kpIndex: Float,
    isDarkMode: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "KP-Index",
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else TextSecondaryLight
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = kpIndex.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = getKpIndexColor(kpIndex, isDarkMode)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // KP Index scale
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF81C784), // Green
                            Color(0xFFFFD54F), // Yellow
                            Color(0xFFFF8A65), // Orange
                            Color(0xFFE57373)  // Red
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            // KP Index marker
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
            ) {
                val progress = min(kpIndex / 9f, 1f)
                val x = size.width * progress
                
                // Draw marker line
                drawLine(
                    color = Color.White,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
            
            // Scale labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "3",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "6",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "9",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // KP Index interpretation
        Text(
            text = getKpIndexInterpretation(kpIndex),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDarkMode) Color.White else TextPrimaryLight,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Get color based on KP index value
 */
private fun getKpIndexColor(kpIndex: Float, isDarkMode: Boolean): Color {
    return when {
        kpIndex >= 7f -> if (isDarkMode) Color(0xFFE57373) else Color(0xFFB71C1C) // Red
        kpIndex >= 5f -> if (isDarkMode) Color(0xFFFF8A65) else Color(0xFFE64A19) // Orange
        kpIndex >= 3f -> if (isDarkMode) Color(0xFFFFD54F) else Color(0xFFF57F17) // Yellow
        else -> if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32) // Green
    }
}

/**
 * Get interpretation text based on KP index value
 */
private fun getKpIndexInterpretation(kpIndex: Float): String {
    return when {
        kpIndex >= 7f -> "Strong geomagnetic storm, auroras visible far south"
        kpIndex >= 5f -> "Moderate geomagnetic storm, good aurora activity"
        kpIndex >= 3f -> "Minor geomagnetic activity, auroras likely visible"
        else -> "Quiet geomagnetic conditions, limited aurora activity"
    }
}
