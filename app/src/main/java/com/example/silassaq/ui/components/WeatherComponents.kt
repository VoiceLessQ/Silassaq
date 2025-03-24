package com.example.silassaq.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.silassaq.R
import com.example.silassaq.data.CurrentWeather
import com.example.silassaq.data.ForecastDay
import com.example.silassaq.data.WeatherResponse
import com.example.silassaq.ui.theme.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun WeatherCard(
    weather: WeatherResponse,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit = {}
) {
    val date = LocalDateTime.parse(weather.current.last_updated.replace(" ", "T"))
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.White, DarkBlue),
                        startX = 0f,
                        endX = 1000f
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side - Temperature and location
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "+${weather.current.temp_c.toInt()}°",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )
                    
                    Text(
                        text = weather.current.condition.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                }
                
                // Right side - Location and time
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = weather.location.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    
                    Text(
                        text = "Now in ${weather.location.name} ${date.format(formatter)}, ${date.dayOfWeek.toString().lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherDetails(
    current: CurrentWeather,
    useGreenlandicFormat: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DetailItem(
                icon = Icons.Default.LocationOn,
                value = "${current.humidity}%",
                label = if (useGreenlandicFormat) "Masarsuseq" else "Humidity"
            )
            
            DetailItem(
                icon = Icons.Default.Refresh,
                value = "${current.wind_kph.toInt()} km/h",
                label = if (useGreenlandicFormat) "Anori" else "Wind"
            )
            
            DetailItem(
                icon = Icons.Default.LocationOn,
                value = current.wind_dir,
                label = if (useGreenlandicFormat) "Sammivik" else "Direction"
            )
        }
    }
}

@Composable
fun DetailItem(
    icon: ImageVector,
    value: String, 
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ForecastList(
    forecast: List<ForecastDay>,
    modifier: Modifier = Modifier,
    useGreenlandicFormat: Boolean = false,
    translateCondition: (String) -> String = { it }
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardLight
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = if (useGreenlandicFormat) {
                    "${forecast.size} ullut silaannaat"
                } else {
                    "${forecast.size}-Day Forecast"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MeteoBlue
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            forecast.forEach { day ->
                ForecastDayItem(
                    day = day,
                    useGreenlandicFormat = useGreenlandicFormat,
                    translateCondition = translateCondition
                )
                if (forecast.indexOf(day) < forecast.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color.LightGray.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
fun ForecastDayItem(
    day: ForecastDay,
    useGreenlandicFormat: Boolean = false,
    translateCondition: (String) -> String = { it }
) {
    val date = try {
        LocalDate.parse(day.date)
    } catch (e: Exception) {
        // Use a default date if parsing fails
        LocalDate.now()
    }
    
    val formattedDate = if (useGreenlandicFormat) {
        val dayOfWeek = date.dayOfWeek.value
        val monthValue = date.monthValue
        val dayOfMonth = date.dayOfMonth
        
        val greenlandicDay = when (dayOfWeek) {
            1 -> "ataasinngorneq"
            2 -> "marlunngorneq"
            3 -> "pingasunngorneq"
            4 -> "sisamanngorneq"
            5 -> "tallimanngorneq"
            6 -> "arfininngorneq"
            7 -> "sapaat"
            else -> ""
        }
        
        val greenlandicMonth = when (monthValue) {
            1 -> "januarip"
            2 -> "februarip"
            3 -> "marsip"
            4 -> "aprilip"
            5 -> "majip"
            6 -> "junip"
            7 -> "julip"
            8 -> "augustip"
            9 -> "septemberip"
            10 -> "oktoberip"
            11 -> "novemberip"
            12 -> "decemberip"
            else -> ""
        }
        
        "$greenlandicDay $greenlandicMonth $dayOfMonth-at"
    } else {
        val formatter = DateTimeFormatter.ofPattern("EEE, MMM d")
        date.format(formatter)
    }
    
    // Determine color based on temperature
    val tempColor = when {
        day.day.maxtemp_c > 25 -> HotTemp
        day.day.maxtemp_c > 15 -> WarmTemp
        day.day.maxtemp_c > 5 -> MildTemp
        day.day.maxtemp_c > -5 -> CoolTemp
        day.day.maxtemp_c > -15 -> ColdTemp
        else -> FreezingTemp
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date column
        Column(
            modifier = Modifier.width(100.dp)
        ) {
            Text(
                text = formattedDate.split(",")[0], // Just the day name
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MeteoBlue
            )
            
            Text(
                text = formattedDate.substringAfter(",").trim(), // Just the date
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryLight
            )
        }
        
        // Weather condition
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(120.dp)
        ) {
            WeatherIcon(
                iconUrl = day.day.condition.icon,
                description = day.day.condition.text,
                size = 40.dp
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = if (useGreenlandicFormat) {
                    translateCondition(day.day.condition.text)
                } else {
                    day.day.condition.text
                },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = TextPrimaryLight
            )
        }
        
        // Temperature with min/max
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Max temperature
                Text(
                    text = "${day.day.maxtemp_c.toInt()}°",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = tempColor
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Min temperature
                Text(
                    text = "${day.day.mintemp_c.toInt()}°",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryLight
                )
            }
            
            // Precipitation chance
            if (day.day.daily_chance_of_rain > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn, // Using LocationOn as a substitute for water drop
                        contentDescription = "Chance of rain",
                        tint = RainyColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${day.day.daily_chance_of_rain}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = RainyColor
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherIcon(
    iconUrl: String,
    description: String,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Make sure the URL is valid
    val validUrl = if (iconUrl.startsWith("http")) {
        iconUrl
    } else {
        "https://openweathermap.org/img/wn/10d@2x.png" // Default icon
    }
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(validUrl)
            .crossfade(true)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .build(),
        contentDescription = description,
        modifier = modifier.size(size),
        error = painterResource(id = android.R.drawable.ic_menu_gallery),
        fallback = painterResource(id = android.R.drawable.ic_menu_gallery)
    )
}

@Composable
fun LocationSelector(
    locations: List<String>,
    selectedLocation: String,
    onLocationSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Favorite Cities",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        locations.forEach { location ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = if (location == selectedLocation) AccentYellow else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (location == selectedLocation) FontWeight.Bold else FontWeight.Normal,
                    color = if (location == selectedLocation) AccentBlue else Color.Black,
                    modifier = Modifier.weight(1f)
                )
                
                // Weather icon placeholder
                Icon(
                    painter = painterResource(id = R.drawable.ic_weather_sunny),
                    contentDescription = "Weather",
                    tint = AccentYellow,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "+5°C",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LoadingScreen(isDarkMode: Boolean = true) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp),
                color = if (isDarkMode) AccentBlue else MeteoBlue,
                strokeWidth = 5.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Loading weather data...",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDarkMode) Color.White else MeteoBlue
            )
        }
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit, isDarkMode: Boolean = true) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) DarkNavyCardLight else CardLight
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Error",
                    tint = if (isDarkMode) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Unable to Load Weather",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else TextSecondaryLight
                )
                
                // Add helpful tips based on error message
                if (message.contains("403")) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "This could be due to an invalid API key or rate limiting. Please check your API credentials.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else TextSecondaryLight
                    )
                } else if (message.contains("404")) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "The location you're looking for might not be available. Try a different location.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else TextSecondaryLight
                    )
                } else if (message.contains("network") || message.contains("internet")) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Please check your internet connection and try again.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else TextSecondaryLight
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) AccentBlue else MeteoBlue
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Retry",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HourlyForecast(weather: WeatherResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Hourly Forecast",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                val hourlyData = weather.forecast.forecastday.firstOrNull()?.hour ?: emptyList()
                
                items(hourlyData) { hourData ->
                    val hourTime = LocalDateTime.parse(hourData.time.replace(" ", "T"))
                    val now = LocalDateTime.now()
                    val isNow = hourTime.hour == now.hour && hourTime.dayOfMonth == now.dayOfMonth
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isNow) LightBlue.copy(alpha = 0.3f) else Color.Transparent)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (isNow) "Now" else hourTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Icon(
                            painter = painterResource(
                                id = when {
                                    hourData.condition.text.contains("sunny", ignoreCase = true) -> R.drawable.ic_weather_sunny
                                    hourData.condition.text.contains("cloud", ignoreCase = true) -> R.drawable.ic_weather_cloudy
                                    else -> R.drawable.ic_weather_partly_cloudy
                                }
                            ),
                            contentDescription = hourData.condition.text,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "+${hourData.temp_c.toInt()}°",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherDetailsGrid(weather: WeatherResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Weather Details",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // First column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WeatherDetailItem(
                        icon = R.drawable.ic_weather_sunny,
                        value = "+${weather.current.feelslike_c.toInt()}°",
                        label = "Feels Like"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    WeatherDetailItem(
                        icon = R.drawable.ic_weather_sunny,
                        value = "${weather.current.humidity}%",
                        label = "Humidity"
                    )
                }
                
                // Second column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WeatherDetailItem(
                        icon = R.drawable.ic_weather_sunny,
                        value = "${weather.current.wind_kph} km/h",
                        label = "Wind"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    WeatherDetailItem(
                        icon = R.drawable.ic_weather_sunny,
                        value = "${weather.current.uv}",
                        label = "UV Index"
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherDetailItem(
    icon: Int,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}

@Composable
fun CustomSearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        color = if (isDarkMode) DarkNavyCardLight else Color.LightGray.copy(alpha = 0.2f)
    ) {
        TextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            placeholder = { 
                Text(
                    "Enter city name",
                    color = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Gray
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = if (isDarkMode) Color.White.copy(alpha = 0.7f) else TextSecondaryLight
                )
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = if (isDarkMode) Color.White else TextPrimaryLight,
                unfocusedTextColor = if (isDarkMode) Color.White else TextPrimaryLight,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                cursorColor = if (isDarkMode) AccentBlue else MeteoBlue,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch()
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
} 