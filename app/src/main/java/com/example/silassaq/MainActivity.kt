package com.example.silassaq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.content.Intent
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
import kotlinx.coroutines.CoroutineScope
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
import com.example.silassaq.ui.screens.SettingsScreen
import com.example.silassaq.ui.theme.AccentOrange
import com.example.silassaq.ui.theme.SilassaqTheme
import com.example.silassaq.utils.SunriseSunsetCalculator
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherApp(viewModel: WeatherViewModel) {
    val weatherState = viewModel.weatherState
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var showLocationSelector by remember { mutableStateOf(false) }
    var showApiSourceDialog by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(
            viewModel = viewModel,
            onNavigateBack = { showSettings = false }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerContent(
                    onSettingsClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            showSettings = true
                        }
                    },
                    onCloseDrawer = {
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        WeatherContent(
            viewModel = viewModel,
            weatherState = weatherState,
            isOfflineMode = isOfflineMode,
            drawerState = drawerState,
            coroutineScope = coroutineScope,
            showLocationSelector = showLocationSelector,
            onShowLocationSelector = { showLocationSelector = it },
            showApiSourceDialog = showApiSourceDialog,
            onShowApiSourceDialog = { showApiSourceDialog = it },
            showMoreMenu = showMoreMenu,
            onShowMoreMenu = { showMoreMenu = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherContent(
    viewModel: WeatherViewModel,
    weatherState: WeatherViewModel.WeatherState,
    isOfflineMode: Boolean,
    drawerState: DrawerState,
    coroutineScope: CoroutineScope,
    showLocationSelector: Boolean,
    onShowLocationSelector: (Boolean) -> Unit,
    showApiSourceDialog: Boolean,
    onShowApiSourceDialog: (Boolean) -> Unit,
    showMoreMenu: Boolean,
    onShowMoreMenu: (Boolean) -> Unit
) {
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
                    IconButton(onClick = {
                        coroutineScope.launch { drawerState.open() }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }

                    TextButton(
                        onClick = { onShowLocationSelector(!showLocationSelector) }
                    ) {
                        Text(weatherData.location.name)
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Location"
                        )
                    }

                    Row {
                        // API source toggle button
                        IconButton(onClick = { onShowApiSourceDialog(true) }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "API Source"
                            )
                        }

                        IconButton(onClick = { onShowMoreMenu(true) }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More"
                            )
                        }
                    }
                }
                
                // More Options Menu
                if (showMoreMenu) {
                    MoreOptionsMenu(
                        viewModel = viewModel,
                        onDismiss = { onShowMoreMenu(false) }
                    )
                }

                // API Source Dialog
                if (showApiSourceDialog) {
                    ApiSourceDialog(
                        useMetNorway = viewModel.useMetNorway,
                        onConfirm = {
                            viewModel.toggleApiSource()
                            onShowApiSourceDialog(false)
                        },
                        onDismiss = { onShowApiSourceDialog(false) }
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
                                onShowLocationSelector(false)
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
                    
                    // Daylight visualization with real sunrise/sunset data
                    val locationData = MetNoWeatherService.greenlandLocations[weatherData.location.name]
                    if (locationData != null) {
                        val sunTimes = SunriseSunsetCalculator.calculate(
                            locationData.lat,
                            locationData.lon,
                            LocalDate.now()
                        )
                        DaylightVisualization(
                            sunrise = sunTimes.sunrise,
                            sunset = sunTimes.sunset,
                            currentTime = LocalTime.now(),
                            date = LocalDate.now(),
                            isDarkMode = false,
                            isPolarDay = sunTimes.isPolarDay,
                            isPolarNight = sunTimes.isPolarNight,
                            daylightHours = sunTimes.daylightHours,
                            civilTwilightMorning = sunTimes.civilTwilightMorning,
                            civilTwilightEvening = sunTimes.civilTwilightEvening,
                            nauticalTwilightMorning = sunTimes.nauticalTwilightMorning,
                            nauticalTwilightEvening = sunTimes.nauticalTwilightEvening
                        )
                    }
                    
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
            ErrorScreen(
                message = weatherState.message,
                onRetry = { viewModel.refreshWeather() }
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

@Composable
fun ApiSourceDialog(
    useMetNorway: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Weather Data Source") },
        text = {
            Column {
                Text("Choose which weather API to use as the primary data source.")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Current source: ${if (useMetNorway) "Met Norway" else "WeatherAPI.com"}")
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Switch to ${if (useMetNorway) "WeatherAPI.com" else "Met Norway"}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NavigationDrawerContent(
    onSettingsClick: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Text(
            text = "Silassaq",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Divider()

        NavigationDrawerItem(
            label = { Text("Settings") },
            selected = false,
            onClick = onSettingsClick,
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            modifier = Modifier.padding(vertical = 8.dp)
        )

        NavigationDrawerItem(
            label = { Text("About") },
            selected = false,
            onClick = { /* TODO: Navigate to About */ },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun MoreOptionsMenu(
    viewModel: WeatherViewModel,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("Refresh") },
            onClick = {
                viewModel.refreshWeather()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
        )

        DropdownMenuItem(
            text = { Text("Share Weather") },
            onClick = {
                // TODO: Implement share functionality
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
        )

        Divider()

        DropdownMenuItem(
            text = { Text("Report Issue") },
            onClick = {
                // TODO: Navigate to issue reporting
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) }
        )
    }
}
