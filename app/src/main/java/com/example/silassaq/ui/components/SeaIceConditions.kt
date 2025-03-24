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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.silassaq.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Sea Ice Conditions component for Greenland
 * Shows information about sea ice coverage, thickness, and safety recommendations
 */
@Composable
fun SeaIceConditions(
    iceConcentration: Float = 75f, // Percentage of sea ice coverage
    iceThickness: Float = 1.2f, // Ice thickness in meters
    iceEdgeDistance: Float = 5.2f, // Distance to ice edge in km
    safetyLevel: SafetyLevel = SafetyLevel.MODERATE,
    lastUpdated: LocalDate = LocalDate.now().minusDays(1),
    historicalComparison: String = "15% below average for this time of year",
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
                text = "Sea Ice Conditions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else MeteoBlue
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Last updated info
            Text(
                text = "Last updated: ${lastUpdated.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) Color.White.copy(alpha = 0.5f) else TextSecondaryLight
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ice concentration
            IceMetricRow(
                label = "Ice Concentration",
                value = "${iceConcentration.toInt()}%",
                maxValue = 100f,
                currentValue = iceConcentration,
                isDarkMode = isDarkMode,
                valueColor = when {
                    iceConcentration > 80f -> if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF0288D1)
                    iceConcentration > 40f -> if (isDarkMode) Color(0xFF90CAF9) else Color(0xFF1976D2)
                    else -> if (isDarkMode) Color(0xFFBBDEFB) else Color(0xFF2196F3)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ice thickness
            IceMetricRow(
                label = "Ice Thickness",
                value = "$iceThickness m",
                maxValue = 3f,
                currentValue = iceThickness,
                isDarkMode = isDarkMode,
                valueColor = when {
                    iceThickness > 2f -> if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF0288D1)
                    iceThickness > 1f -> if (isDarkMode) Color(0xFF90CAF9) else Color(0xFF1976D2)
                    else -> if (isDarkMode) Color(0xFFBBDEFB) else Color(0xFF2196F3)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ice edge distance
            IceMetricRow(
                label = "Distance to Ice Edge",
                value = "$iceEdgeDistance km",
                maxValue = 20f,
                currentValue = iceEdgeDistance,
                isDarkMode = isDarkMode,
                valueColor = when {
                    iceEdgeDistance > 10f -> if (isDarkMode) Color(0xFF81D4FA) else Color(0xFF0288D1)
                    iceEdgeDistance > 5f -> if (isDarkMode) Color(0xFF90CAF9) else Color(0xFF1976D2)
                    else -> if (isDarkMode) Color(0xFFBBDEFB) else Color(0xFF2196F3)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Historical comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historical Comparison:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else TextSecondaryLight,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = historicalComparison,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkMode) Color.White else TextPrimaryLight
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Safety recommendations
            SafetyRecommendations(safetyLevel, isDarkMode)
        }
    }
}

/**
 * Row displaying an ice metric with a progress bar
 */
@Composable
private fun IceMetricRow(
    label: String,
    value: String,
    maxValue: Float,
    currentValue: Float,
    isDarkMode: Boolean,
    valueColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else TextSecondaryLight
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Progress bar
        val progress = (currentValue / maxValue).coerceIn(0f, 1f)
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .background(
                        color = valueColor,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

/**
 * Safety recommendations based on ice conditions
 */
@Composable
private fun SafetyRecommendations(
    safetyLevel: SafetyLevel,
    isDarkMode: Boolean
) {
    val (backgroundColor, textColor, recommendations) = when (safetyLevel) {
        SafetyLevel.SAFE -> Triple(
            if (isDarkMode) Color(0xFF81C784).copy(alpha = 0.2f) else Color(0xFFC8E6C9),
            if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32),
            listOf(
                "Ice conditions are generally safe for travel.",
                "Always check local conditions before venturing out.",
                "Carry safety equipment including ice picks and rope."
            )
        )
        SafetyLevel.MODERATE -> Triple(
            if (isDarkMode) Color(0xFFFFD54F).copy(alpha = 0.2f) else Color(0xFFFFF9C4),
            if (isDarkMode) Color(0xFFFFD54F) else Color(0xFFF57F17),
            listOf(
                "Exercise caution when traveling on ice.",
                "Avoid areas with visible cracks or thin ice.",
                "Travel with experienced guides if unfamiliar with the area.",
                "Maintain communication devices at all times."
            )
        )
        SafetyLevel.DANGEROUS -> Triple(
            if (isDarkMode) Color(0xFFE57373).copy(alpha = 0.2f) else Color(0xFFFFCDD2),
            if (isDarkMode) Color(0xFFE57373) else Color(0xFFB71C1C),
            listOf(
                "Ice conditions are hazardous - avoid travel if possible.",
                "If travel is necessary, consult with local authorities first.",
                "Never travel alone and inform others of your route and expected return.",
                "Carry emergency equipment and stay on established routes."
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = "Safety Recommendations",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        recommendations.forEach { recommendation ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Canvas(modifier = Modifier
                    .size(8.dp)
                    .padding(top = 6.dp)) {
                    drawCircle(
                        color = textColor,
                        radius = 3.dp.toPx()
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkMode) Color.White else TextPrimaryLight
                )
            }
        }
    }
}

/**
 * Safety levels for sea ice conditions
 */
enum class SafetyLevel {
    SAFE,
    MODERATE,
    DANGEROUS
}
