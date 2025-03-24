package com.example.silassaq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.silassaq.data.WeatherResponse
import com.example.silassaq.network.MetNoWeatherService
import com.example.silassaq.network.WeatherService
import com.example.silassaq.ui.components.*
import com.example.silassaq.ui.components.SafetyLevel
import com.example.silassaq.ui.theme.AccentOrange
import com.example.silassaq.ui.theme.SilassaqTheme
import com.example.silassaq.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SilassaqTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: WeatherViewModel = viewModel()
                    WeatherApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun WeatherApp(viewModel: WeatherViewModel) {
    val weatherState = viewModel.weatherState
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    var showLocationSelector by remember { mutableStateOf(false) }
    var showApiSourceDialog by remember { mutableStateOf(false) }
    
    when (weatherState) {
        is WeatherViewModel.WeatherState.Loading -> {
            LoadingScreen()
        }
        is WeatherViewModel.WeatherState.Success -> {
            val weatherData = (weatherState as WeatherViewModel.WeatherState.Success).data
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // Offline banner
                if (isOfflineMode) {
                    OfflineBanner(viewModel.getDataAge())
                }
                
                // App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* Open drawer */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                    
                    TextButton(
                        onClick = { showLocationSelector = !showLocationSelector }
                    ) {
                        Text(weatherData.location.name)
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Location"
                        )
                    }
                    
                    Row {
                        // API source toggle button
                        IconButton(onClick = { showApiSourceDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "API Source"
                            )
                        }
                        
                        IconButton(onClick = { /* More options */ }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More"
                            )
                        }
                    }
                }
                
                // API Source Dialog
                if (showApiSourceDialog) {
                    AlertDialog(
                        onDismissRequest = { showApiSourceDialog = false },
                        title = { Text("Select Weather Data Source") },
                        text = { 
                            Column {
                                Text("Choose which weather API to use as the primary data source.")
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Current source: ${if (viewModel.useMetNorway) "Met Norway" else "WeatherAPI.com"}")
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.toggleApiSource()
                                    showApiSourceDialog = false
                                }
                            ) {
                                Text("Switch to ${if (viewModel.useMetNorway) "WeatherAPI.com" else "Met Norway"}")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showApiSourceDialog = false }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }
                
                // Location selector popup
                if (showLocationSelector) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        EnhancedLocationSelector(
                            selectedLocation = weatherData.location.name,
                            onLocationSelected = { location ->
                                coroutineScope.launch {
                                    viewModel.selectLocation(location)
                                }
                                showLocationSelector = false
                            }
                        )
                    }
                }
                
                // Main content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    WeatherCard(weather = weatherData)
                    HourlyForecast(weather = weatherData)
                    WeatherDetailsGrid(weather = weatherData)
                    
                    // Greenland-specific features
                    // Aurora forecast (simulated data for now)
                    AuroraForecast(
                        kpIndex = 4.2f,
                        viewingProbability = 75f,
                        bestViewingTime = LocalDateTime.now().plusHours(4),
                        cloudCover = 15f,
                        isDarkMode = false
                    )
                    
                    // Daylight visualization (simulated data for now)
                    // For Nuuk in March (spring)
                    DaylightVisualization(
                        sunrise = LocalTime.of(6, 45),
                        sunset = LocalTime.of(18, 15),
                        currentTime = LocalTime.now(),
                        date = LocalDate.now(),
                        isDarkMode = false,
                        isPolarDay = false,
                        isPolarNight = false,
                        daylightHours = 11.5f,
                        civilTwilightMorning = LocalTime.of(6, 15),
                        civilTwilightEvening = LocalTime.of(18, 45),
                        nauticalTwilightMorning = LocalTime.of(5, 45),
                        nauticalTwilightEvening = LocalTime.of(19, 15)
                    )
                    
                    // Sea ice conditions (simulated data for now)
                    SeaIceConditions(
                        iceConcentration = 75f,
                        iceThickness = 1.2f,
                        iceEdgeDistance = 5.2f,
                        safetyLevel = SafetyLevel.MODERATE,
                        lastUpdated = LocalDate.now().minusDays(1),
                        historicalComparison = "15% below average for this time of year",
                        isDarkMode = false
                    )
                }
            }
        }
        is WeatherViewModel.WeatherState.Error -> {
            val errorMessage = (weatherState as WeatherViewModel.WeatherState.Error).message
            ErrorScreen(
                message = errorMessage,
                onRetry = {
                    coroutineScope.launch {
                        viewModel.refreshWeather()
                    }
                }
            )
        }
    }
}

@Composable
fun OfflineBanner(dataAge: Int?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        color = AccentOrange
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Offline",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = if (dataAge != null) {
                    "Offline mode - Showing data from $dataAge ${if (dataAge == 1) "minute" else "minutes"} ago"
                } else {
                    "Offline mode - Showing cached data"
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
