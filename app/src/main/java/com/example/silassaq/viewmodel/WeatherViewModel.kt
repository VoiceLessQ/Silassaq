package com.example.silassaq.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.silassaq.data.WeatherDataCache
import com.example.silassaq.data.WeatherDataMapper
import com.example.silassaq.data.WeatherResponse
import com.example.silassaq.network.WeatherService
import com.example.silassaq.network.MetNoWeatherService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherViewModel : ViewModel() {
    
    var weatherState by mutableStateOf<WeatherState>(WeatherState.Loading)
        private set
    
    var selectedLocation by mutableStateOf(WeatherService.greenlandLocations[0])
        private set
    
    var lastUpdated by mutableStateOf("")
        private set
    
    // Flag to show temperatures in Greenlandic format
    var useGreenlandicFormat by mutableStateOf(true)
        private set
    
    // Language code (en, da, kl)
    var languageCode by mutableStateOf("en")
        private set
    
    // Flag to toggle between WeatherAPI.com and Met Norway
    var useMetNorway by mutableStateOf(false)
        private set
    
    // Offline mode detection
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()
    
    init {
        fetchWeather(forceRefresh = true)
    }
    
    fun fetchWeather(location: String = selectedLocation, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            weatherState = WeatherState.Loading
            
            // Check cache first if not forcing refresh
            if (!forceRefresh) {
                val locationData = MetNoWeatherService.greenlandLocations[location]
                if (locationData != null) {
                    val cachedData = WeatherDataCache.getCachedWeather(locationData.geonamesId)
                    if (cachedData != null) {
                        weatherState = WeatherState.Success(cachedData)
                        selectedLocation = location
                        _isOfflineMode.value = true
                        return@launch
                    }
                }
            }
            
            try {
                // Add a small delay to ensure the loading state is visible
                if (forceRefresh) {
                    delay(500) // Increased delay to make loading state more visible
                }
                
                var response: WeatherResponse? = null
                var error: Exception? = null
                
                // Try the selected API first
                try {
                    response = if (useMetNorway) {
                        // Use Met Norway API with retry mechanism
                        val locationData = MetNoWeatherService.greenlandLocations[location]
                            ?: throw IllegalArgumentException("Unknown location: $location")
                        
                        val metNoResponse = fetchMetNoWeatherWithRetry(location)
                        WeatherDataMapper.mapMetNoToAppFormat(metNoResponse, locationData)
                    } else {
                        // Use WeatherAPI.com
                        WeatherService.getGreenlandWeather(location)
                    }
                    
                    // Cache the successful response
                    val locationData = MetNoWeatherService.greenlandLocations[location]
                    if (locationData != null) {
                        WeatherDataCache.cacheWeatherData(locationData.geonamesId, response)
                    }
                    
                    _isOfflineMode.value = false
                } catch (e: Exception) {
                    error = e
                    
                    // If the selected API fails, try the other one as fallback
                    try {
                        response = if (!useMetNorway) {
                            // Fallback to Met Norway API with retry mechanism
                            val locationData = MetNoWeatherService.greenlandLocations[location]
                                ?: throw IllegalArgumentException("Unknown location: $location")
                            
                            val metNoResponse = fetchMetNoWeatherWithRetry(location)
                            WeatherDataMapper.mapMetNoToAppFormat(metNoResponse, locationData)
                        } else {
                            // Fallback to WeatherAPI.com
                            WeatherService.getGreenlandWeather(location)
                        }
                        
                        // Cache the successful response
                        val locationData = MetNoWeatherService.greenlandLocations[location]
                        if (locationData != null) {
                            WeatherDataCache.cacheWeatherData(locationData.geonamesId, response)
                        }
                        
                        _isOfflineMode.value = false
                    } catch (fallbackError: Exception) {
                        // Both APIs failed, check if we have cached data
                        val locationData = MetNoWeatherService.greenlandLocations[location]
                        if (locationData != null) {
                            val cachedData = WeatherDataCache.getCachedWeather(locationData.geonamesId)
                            if (cachedData != null) {
                                response = cachedData
                                _isOfflineMode.value = true
                            } else {
                                // No cached data available, throw the original error
                                throw error
                            }
                        } else {
                            // Unknown location, throw the original error
                            throw error
                        }
                    }
                }
                
                if (response != null) {
                    // Update the last updated time
                    lastUpdated = LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    )
                    
                    weatherState = WeatherState.Success(response)
                    selectedLocation = location
                } else {
                    // This should not happen, but just in case
                    throw IllegalStateException("Failed to get weather data")
                }
            } catch (e: Exception) {
                handleWeatherError(e)
            }
        }
    }
    
    /**
     * Toggle between WeatherAPI.com and Met Norway
     */
    fun toggleApiSource() {
        useMetNorway = !useMetNorway
        refreshWeather()
    }
    
    fun refreshWeather() {
        fetchWeather(forceRefresh = true)
    }
    
    fun selectLocation(location: String) {
        if (location != selectedLocation) {
            fetchWeather(location, forceRefresh = true)
        }
    }
    
    fun toggleLanguage() {
        useGreenlandicFormat = !useGreenlandicFormat
        // Refresh to apply the new language setting
        refreshWeather()
    }
    
    fun setLanguage(langCode: String) {
        if (langCode != languageCode) {
            languageCode = langCode
            useGreenlandicFormat = langCode == "kl"
            // Refresh to apply the new language setting
            refreshWeather()
        }
    }
    
    // Helper function to translate weather conditions
    fun translateWeatherCondition(condition: String): String {
        return when (languageCode) {
            "kl" -> translateToKalaallisut(condition)
            "da" -> translateToDanish(condition)
            else -> condition // Default to English
        }
    }
    
    private fun translateToKalaallisut(condition: String): String {
        return when (condition.lowercase()) {
            "snowy", "snow", "light snow" -> "Quliartorluni"
            "partly cloudy", "cloudy" -> "Qulisimalluni"
            "clear", "clear sky", "sunny" -> "Seqinnarpoq"
            "light snow", "snow shower" -> "Nittaalaq"
            "rain", "light rain", "drizzle" -> "Masannartuliorneq"
            "heavy rain" -> "Assut masannartuliorneq"
            else -> condition
        }
    }
    
    private fun translateToDanish(condition: String): String {
        return when (condition.lowercase()) {
            "snowy", "snow", "light snow" -> "Sne"
            "partly cloudy" -> "Delvist skyet"
            "cloudy" -> "Skyet"
            "clear", "clear sky", "sunny" -> "Solrigt"
            "light snow", "snow shower" -> "Let sne"
            "rain", "light rain" -> "Regn"
            "drizzle" -> "Støvregn"
            "heavy rain" -> "Kraftig regn"
            else -> condition
        }
    }
    
    // Helper function to translate UI labels
    fun translateLabel(label: String): String {
        return when (languageCode) {
            "kl" -> translateLabelToKalaallisut(label)
            "da" -> translateLabelToDanish(label)
            else -> label // Default to English
        }
    }
    
    private fun translateLabelToKalaallisut(label: String): String {
        return when (label) {
            "Weather Details" -> "Silap pissusaa pillugu paasissutissat"
            "Feels Like" -> "Malugineqarpoq"
            "Humidity" -> "Imermik akoqassuseq"
            "Wind" -> "Anorlertussusia"
            "Pressure" -> "Naqitsineq"
            "UV Index" -> "UV uuttortaat"
            "Forecast" -> "Silap nalunaarsuutaa"
            "Today" -> "Ullumi"
            "Condition" -> "Silap qanoq innera"
            "Chance of Rain" -> "Siallersinnaanera"
            "Low" -> "Appasippoq"
            "Moderate" -> "Akunnattumik"
            "High" -> "Qaffasippoq"
            "Very High" -> "Qaffasissaqaaq"
            "Extreme" -> "Qaffasissaqaarujussuaq"
            else -> label
        }
    }
    
    private fun translateLabelToDanish(label: String): String {
        return when (label) {
            "Weather Details" -> "Vejrdetaljer"
            "Feels Like" -> "Føles som"
            "Humidity" -> "Luftfugtighed"
            "Wind" -> "Vind"
            "Pressure" -> "Tryk"
            "UV Index" -> "UV-indeks"
            "Forecast" -> "Vejrudsigt"
            "Today" -> "I dag"
            "Condition" -> "Tilstand"
            "Chance of Rain" -> "Chance for regn"
            "Low" -> "Lav"
            "Moderate" -> "Moderat"
            "High" -> "Høj"
            "Very High" -> "Meget høj"
            "Extreme" -> "Ekstrem"
            else -> label
        }
    }
    
    /**
     * Fetch weather data from Met Norway API with retry mechanism
     * Uses exponential backoff for 403 errors
     */
    private suspend fun fetchMetNoWeatherWithRetry(
        location: String,
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000
    ): MetNoWeatherResponse {
        var currentDelay = initialDelayMs
        var attempt = 0
        
        while (attempt < maxRetries) {
            try {
                return MetNoWeatherService.getGreenlandWeather(location)
            } catch (e: HttpException) {
                if (e.code() == 403) {
                    // 403 Forbidden - likely due to rate limiting or User-Agent issues
                    attempt++
                    if (attempt < maxRetries) {
                        // Log retry attempt
                        println("Received 403 error, retrying in ${currentDelay}ms (attempt $attempt of $maxRetries)")
                        // Exponential backoff
                        delay(currentDelay)
                        currentDelay *= 2
                    } else {
                        throw e
                    }
                } else {
                    throw e
                }
            }
        }
        
        throw IllegalStateException("Failed to fetch weather data after $maxRetries retries")
    }
    
    private fun handleWeatherError(e: Exception) {
        when (e) {
            is UnknownHostException -> {
                // No internet connection, try to use cached data
                val locationData = MetNoWeatherService.greenlandLocations[selectedLocation]
                if (locationData != null) {
                    val cachedData = WeatherDataCache.getCachedWeather(locationData.geonamesId)
                    if (cachedData != null) {
                        weatherState = WeatherState.Success(cachedData)
                        _isOfflineMode.value = true
                        return
                    }
                }
                
                weatherState = WeatherState.Error("No internet connection. Please check your network settings and try again.")
            }
            is SocketTimeoutException -> {
                // Connection timed out, try to use cached data
                val locationData = MetNoWeatherService.greenlandLocations[selectedLocation]
                if (locationData != null) {
                    val cachedData = WeatherDataCache.getCachedWeather(locationData.geonamesId)
                    if (cachedData != null) {
                        weatherState = WeatherState.Success(cachedData)
                        _isOfflineMode.value = true
                        return
                    }
                }
                
                weatherState = WeatherState.Error("Connection timed out. Please try again later.")
            }
            is HttpException -> {
                val errorMsg = when (e.code()) {
                    401 -> "API key error. Please check your API key."
                    403 -> "Server error (403). The API is refusing the request. This may be due to rate limiting or an invalid API key. Retries were attempted but failed."
                    404 -> "Location not found. Please try a different location."
                    429 -> "Too many requests. Please try again later."
                    else -> "Server error (${e.code()}). Please try again later."
                }
                weatherState = WeatherState.Error(errorMsg)
            }
            is CancellationException -> {
                // Don't handle cancellation exceptions
                throw e
            }
            else -> {
                weatherState = WeatherState.Error("Error: ${e.message ?: "Unknown error occurred"}")
            }
        }
    }
    
    /**
     * Check if we're in offline mode
     */
    fun isOffline(): Boolean {
        return _isOfflineMode.value
    }
    
    /**
     * Get the age of the current data in minutes
     */
    fun getDataAge(): Int? {
        val locationData = MetNoWeatherService.greenlandLocations[selectedLocation] ?: return null
        val cacheAge = WeatherDataCache.getCacheAge(locationData.geonamesId) ?: return null
        return (cacheAge / 60000).toInt() // Convert milliseconds to minutes
    }
    
    sealed class WeatherState {
        object Loading : WeatherState()
        data class Success(val data: WeatherResponse) : WeatherState()
        data class Error(val message: String) : WeatherState()
    }
}
