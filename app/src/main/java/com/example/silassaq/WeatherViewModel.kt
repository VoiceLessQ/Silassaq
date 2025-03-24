package com.example.silassaq

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.silassaq.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val data: WeatherResponse) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState
    
    private var currentLocation = "Nuuk, Greenland"
    
    // List of available Greenland locations
    val greenlandLocations = listOf(
        "Nuuk, Greenland",
        "Ilulissat, Greenland",
        "Sisimiut, Greenland",
        "Qaqortoq, Greenland",
        "Tasiilaq, Greenland"
    )
    
    init {
        loadWeather()
    }
    
    fun loadWeather(location: String = currentLocation) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            
            // Add a small delay to simulate network request
            delay(1000)
            
            try {
                // Create mock weather data
                val weatherResponse = createMockWeatherData(location)
                _uiState.value = WeatherUiState.Success(weatherResponse)
                currentLocation = location
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("Failed to load weather data: ${e.message}")
            }
        }
    }
    
    private fun createMockWeatherData(location: String): WeatherResponse {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        
        // Extract location name
        val locationName = location.split(",").first().trim()
        
        // Create location data
        val locationData = Location(
            name = location,
            region = "Greenland",
            country = "Greenland",
            localtime = now.format(formatter)
        )
        
        // Create current weather data based on location
        val currentTemp = when (locationName) {
            "Nuuk" -> -5.0f
            "Ilulissat" -> -10.0f
            "Sisimiut" -> -7.0f
            "Qaqortoq" -> -2.0f
            "Tasiilaq" -> -8.0f
            else -> -5.0f
        }
        
        val weatherCondition = when (locationName) {
            "Nuuk" -> "Partly cloudy"
            "Ilulissat" -> "Snow"
            "Sisimiut" -> "Cloudy"
            "Qaqortoq" -> "Light rain"
            "Tasiilaq" -> "Clear"
            else -> "Partly cloudy"
        }
        
        val currentWeather = CurrentWeather(
            last_updated = now.format(formatter),
            temp_c = currentTemp,
            condition = Condition(
                text = weatherCondition,
                icon = getWeatherIconUrl(weatherCondition),
                code = 0
            ),
            wind_kph = 15.0f,
            wind_dir = "N",
            humidity = 70,
            feelslike_c = currentTemp - 2.0f,
            uv = 1.0f
        )
        
        // Create forecast days
        val forecastDays = mutableListOf<ForecastDay>()
        
        // Generate 7 days of forecast
        for (i in 0 until 7) {
            val forecastDate = now.plusDays(i.toLong())
            val dateStr = forecastDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            
            // Vary temperature slightly each day
            val maxTemp = currentTemp + (-2..2).random()
            val minTemp = maxTemp - (2..5).random()
            val avgTemp = (maxTemp + minTemp) / 2
            
            // Vary weather condition
            val dayCondition = when ((i + locationName.hashCode()) % 5) {
                0 -> "Clear"
                1 -> "Partly cloudy"
                2 -> "Cloudy"
                3 -> "Light snow"
                else -> "Snow"
            }
            
            // Create day forecast
            val day = Day(
                maxtemp_c = maxTemp,
                mintemp_c = minTemp,
                avgtemp_c = avgTemp,
                condition = Condition(
                    text = dayCondition,
                    icon = getWeatherIconUrl(dayCondition),
                    code = 0
                ),
                daily_chance_of_rain = (0..30).random()
            )
            
            // Create hourly forecasts
            val hours = mutableListOf<Hour>()
            for (h in 0 until 24) {
                val hourTime = forecastDate.withHour(h)
                val hourTemp = when (h) {
                    in 0..5 -> minTemp - 1.0f
                    in 6..10 -> minTemp + ((maxTemp - minTemp) * (h - 6) / 4)
                    in 11..16 -> maxTemp
                    else -> maxTemp - ((maxTemp - minTemp) * (h - 16) / 8)
                }
                
                val hourCondition = when (h) {
                    in 6..18 -> dayCondition
                    else -> if (dayCondition == "Clear") "Clear" else "Cloudy"
                }
                
                hours.add(
                    Hour(
                        time = hourTime.format(formatter),
                        temp_c = hourTemp,
                        condition = Condition(
                            text = hourCondition,
                            icon = getWeatherIconUrl(hourCondition),
                            code = 0
                        ),
                        chance_of_rain = (0..20).random()
                    )
                )
            }
            
            // Create astro data
            val astro = Astro(
                sunrise = "06:00",
                sunset = "18:00"
            )
            
            // Add forecast day
            forecastDays.add(
                ForecastDay(
                    date = dateStr,
                    day = day,
                    astro = astro,
                    hour = hours
                )
            )
        }
        
        // Create forecast
        val forecast = Forecast(forecastDays)
        
        // Create and return weather response
        return WeatherResponse(currentWeather, locationData, forecast)
    }
    
    private fun getWeatherIconUrl(condition: String): String {
        val iconName = when {
            condition.contains("clear", ignoreCase = true) -> "clearsky_day"
            condition.contains("partly cloudy", ignoreCase = true) -> "partlycloudy_day"
            condition.contains("cloudy", ignoreCase = true) -> "cloudy"
            condition.contains("rain", ignoreCase = true) -> "rain"
            condition.contains("snow", ignoreCase = true) -> "snow"
            condition.contains("sleet", ignoreCase = true) -> "sleet"
            condition.contains("fog", ignoreCase = true) -> "fog"
            else -> "cloudy"
        }
        
        return "https://api.met.no/weatherapi/weathericon/2.0/?symbol=$iconName&content_type=image/png"
    }
} 