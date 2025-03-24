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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.silassaq.ui.theme.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Daylight visualization component for Greenland
 * Shows daylight hours, twilight periods, and sun position
 */
@Composable
fun DaylightVisualization(
    sunrise: LocalTime = LocalTime.of(6, 0),
    sunset: LocalTime = LocalTime.of(18, 0),
    currentTime: LocalTime = LocalTime.now(),
    date: LocalDate = LocalDate.now(),
    isDarkMode: Boolean = false,
    isPolarDay: Boolean = false,
    isPolarNight: Boolean = false,
    daylightHours: Float = 12f,
    civilTwilightMorning: LocalTime = LocalTime.of(5, 30),
    civilTwilightEvening: LocalTime = LocalTime.of(18, 30),
    nauticalTwilightMorning: LocalTime = LocalTime.of(5, 0),
    nauticalTwilightEvening: LocalTime = LocalTime.of(19, 0)
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
                text = "Daylight Hours",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else MeteoBlue
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Special cases for polar day/night
            if (isPolarDay || isPolarNight) {
                Text(
                    text = if (isPolarDay) "Midnight Sun Period" else "Polar Night Period",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPolarDay) SunnyColor else Color(0xFF3F51B5),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (isPolarDay) 
                        "The sun does not set below the horizon today." 
                    else 
                        "The sun does not rise above the horizon today.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = if (isDarkMode) Color.White else TextPrimaryLight,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Sun clock visualization
            SunClock(
                sunrise = sunrise,
                sunset = sunset,
                currentTime = currentTime,
                civilTwilightMorning = civilTwilightMorning,
                civilTwilightEvening = civilTwilightEvening,
                nauticalTwilightMorning = nauticalTwilightMorning,
                nauticalTwilightEvening = nauticalTwilightEvening,
                isPolarDay = isPolarDay,
                isPolarNight = isPolarNight,
                isDarkMode = isDarkMode
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Daylight information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sunrise
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Sunrise",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else TextSecondaryLight
                    )
                    
                    Text(
                        text = if (isPolarNight) "---" else if (isPolarDay) "Midnight Sun" else sunrise.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) SunnyColor else SunnyColor.copy(alpha = 0.8f)
                    )
                }
                
                // Daylight hours
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Daylight",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else TextSecondaryLight
                    )
                    
                    Text(
                        text = if (isPolarDay) "24h" else if (isPolarNight) "0h" else "${daylightHours.toInt()}h ${((daylightHours - daylightHours.toInt()) * 60).toInt()}m",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) AccentBlue else MeteoBlue
                    )
                }
                
                // Sunset
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Sunset",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else TextSecondaryLight
                    )
                    
                    Text(
                        text = if (isPolarNight) "---" else if (isPolarDay) "Midnight Sun" else sunset.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color(0xFFFF9800) else Color(0xFFE65100)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Twilight periods
            if (!isPolarDay && !isPolarNight) {
                Text(
                    text = "Twilight Periods",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else MeteoBlue,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Civil twilight
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Civil Twilight",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else TextSecondaryLight
                        )
                        
                        Text(
                            text = "${civilTwilightMorning.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${civilTwilightEvening.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkMode) Color.White else TextPrimaryLight
                        )
                    }
                    
                    // Nautical twilight
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Nautical Twilight",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else TextSecondaryLight
                        )
                        
                        Text(
                            text = "${nauticalTwilightMorning.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${nauticalTwilightEvening.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkMode) Color.White else TextPrimaryLight
                        )
                    }
                }
            }
            
            // Special information for Greenland
            Spacer(modifier = Modifier.height(16.dp))
            
            val specialInfo = when {
                isPolarDay -> "Greenland experiences the midnight sun phenomenon where the sun remains visible for 24 hours during summer months."
                isPolarNight -> "During polar night in Greenland, the sun stays below the horizon for 24 hours, creating extended darkness."
                daylightHours > 16 -> "Daylight hours are rapidly increasing as we approach the summer solstice."
                daylightHours < 8 -> "Daylight hours are rapidly decreasing as we approach the winter solstice."
                else -> "Greenland experiences extreme variations in daylight hours throughout the year."
            }
            
            Text(
                text = specialInfo,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else TextSecondaryLight,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Sun clock visualization showing the sun's position throughout the day
 */
@Composable
private fun SunClock(
    sunrise: LocalTime,
    sunset: LocalTime,
    currentTime: LocalTime,
    civilTwilightMorning: LocalTime,
    civilTwilightEvening: LocalTime,
    nauticalTwilightMorning: LocalTime,
    nauticalTwilightEvening: LocalTime,
    isPolarDay: Boolean,
    isPolarNight: Boolean,
    isDarkMode: Boolean
) {
    val sunriseMinutes = sunrise.hour * 60 + sunrise.minute
    val sunsetMinutes = sunset.hour * 60 + sunset.minute
    val currentMinutes = currentTime.hour * 60 + currentTime.minute
    
    val civilTwilightMorningMinutes = civilTwilightMorning.hour * 60 + civilTwilightMorning.minute
    val civilTwilightEveningMinutes = civilTwilightEvening.hour * 60 + civilTwilightEvening.minute
    val nauticalTwilightMorningMinutes = nauticalTwilightMorning.hour * 60 + nauticalTwilightMorning.minute
    val nauticalTwilightEveningMinutes = nauticalTwilightEvening.hour * 60 + nauticalTwilightEvening.minute
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height)
            val radius = size.width / 2
            
            // Draw night background
            if (!isPolarDay) {
                drawArc(
                    color = if (isDarkMode) Color(0xFF1A237E).copy(alpha = 0.5f) else Color(0xFF9FA8DA).copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height * 2)
                )
            }
            
            // Draw twilight arcs
            if (!isPolarDay && !isPolarNight) {
                // Nautical twilight
                val nauticalStartAngle = minutesToAngle(nauticalTwilightMorningMinutes)
                val nauticalEndAngle = minutesToAngle(nauticalTwilightEveningMinutes)
                drawArc(
                    color = if (isDarkMode) Color(0xFF5C6BC0).copy(alpha = 0.5f) else Color(0xFF9FA8DA).copy(alpha = 0.5f),
                    startAngle = nauticalStartAngle,
                    sweepAngle = nauticalEndAngle - nauticalStartAngle,
                    useCenter = true,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height * 2)
                )
                
                // Civil twilight
                val civilStartAngle = minutesToAngle(civilTwilightMorningMinutes)
                val civilEndAngle = minutesToAngle(civilTwilightEveningMinutes)
                drawArc(
                    color = if (isDarkMode) Color(0xFF7986CB).copy(alpha = 0.5f) else Color(0xFFC5CAE9).copy(alpha = 0.5f),
                    startAngle = civilStartAngle,
                    sweepAngle = civilEndAngle - civilStartAngle,
                    useCenter = true,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height * 2)
                )
            }
            
            // Draw daylight arc
            if (!isPolarNight) {
                val dayStartAngle = if (isPolarDay) 0f else minutesToAngle(sunriseMinutes)
                val dayEndAngle = if (isPolarDay) 180f else minutesToAngle(sunsetMinutes)
                drawArc(
                    color = if (isDarkMode) Color(0xFFFFD54F).copy(alpha = 0.3f) else Color(0xFFFFECB3).copy(alpha = 0.5f),
                    startAngle = dayStartAngle,
                    sweepAngle = dayEndAngle - dayStartAngle,
                    useCenter = true,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height * 2)
                )
            }
            
            // Draw horizon line
            drawLine(
                color = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 2f
            )
            
            // Draw hour markers
            for (hour in 0..24 step 2) {
                val angle = (hour / 24f) * 180f
                val radians = (angle * PI / 180f).toFloat()
                val x = center.x + radius * sin(radians)
                val y = center.y - radius * cos(radians)
                
                drawLine(
                    color = if (isDarkMode) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f),
                    start = Offset(x, size.height),
                    end = Offset(x, size.height - 10.dp.toPx()),
                    strokeWidth = 1f
                )
                
                // Draw hour markers without using nativeCanvas
                // We'll just draw small circles instead of text for simplicity
                drawCircle(
                    color = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                    radius = 2.dp.toPx(),
                    center = Offset(x, size.height - 15.dp.toPx())
                )
            }
            
            // Draw sun position
            if (!isPolarNight) {
                val sunAngle = if (isPolarDay) {
                    // For polar day, position sun based on current time
                    (currentMinutes / 1440f) * 180f
                } else if (currentMinutes < sunriseMinutes || currentMinutes > sunsetMinutes) {
                    // Sun is below horizon
                    if (currentMinutes < sunriseMinutes) {
                        minutesToAngle(0) // Position at left edge
                    } else {
                        minutesToAngle(1440) // Position at right edge
                    }
                } else {
                    // Normal sun position during day
                    minutesToAngle(currentMinutes)
                }
                
                val sunRadians = (sunAngle * PI / 180f).toFloat()
                val sunPathRadius = radius * 0.9f
                val sunX = center.x + sunPathRadius * sin(sunRadians)
                val sunY = if (isPolarDay || (currentMinutes >= sunriseMinutes && currentMinutes <= sunsetMinutes)) {
                    // Sun above horizon
                    center.y - sunPathRadius * cos(sunRadians)
                } else {
                    // Sun below horizon
                    center.y + 20.dp.toPx() // Just below horizon line
                }
                
                // Draw sun path arc
                if (!isPolarDay && !isPolarNight) {
                    drawArc(
                        color = if (isDarkMode) Color(0xFFFFD54F).copy(alpha = 0.5f) else Color(0xFFFFB300).copy(alpha = 0.3f),
                        startAngle = minutesToAngle(sunriseMinutes),
                        sweepAngle = minutesToAngle(sunsetMinutes) - minutesToAngle(sunriseMinutes),
                        useCenter = false,
                        style = Stroke(width = 2.dp.toPx()),
                        topLeft = Offset(center.x - sunPathRadius, center.y - sunPathRadius),
                        size = Size(sunPathRadius * 2, sunPathRadius * 2)
                    )
                }
                
                // Draw sun
                if (isPolarDay || (currentMinutes >= sunriseMinutes && currentMinutes <= sunsetMinutes)) {
                    drawCircle(
                        color = SunnyColor,
                        radius = 10.dp.toPx(),
                        center = Offset(sunX, sunY)
                    )
                    
                    // Draw sun rays
                    val rayLength = 15.dp.toPx()
                    for (i in 0 until 8) {
                        val rayAngle = i * 45f
                        val rayRadians = (rayAngle * PI / 180f).toFloat()
                        val startX = sunX + 10.dp.toPx() * cos(rayRadians)
                        val startY = sunY + 10.dp.toPx() * sin(rayRadians)
                        val endX = sunX + (10.dp.toPx() + rayLength) * cos(rayRadians)
                        val endY = sunY + (10.dp.toPx() + rayLength) * sin(rayRadians)
                        
                        drawLine(
                            color = SunnyColor,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

/**
 * Convert minutes since midnight to angle on the sun clock (0-180 degrees)
 */
private fun minutesToAngle(minutes: Int): Float {
    return (minutes / 1440f) * 180f
}
