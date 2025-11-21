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

            // Try to load cached data first if not forcing refresh
            if (!forceRefresh && tryLoadCachedData(location)) {
                return@launch
            }

            try {
                // Add delay to make loading state visible when forcing refresh
                if (forceRefresh) {
                    delay(500)
                }

                // Fetch fresh data from APIs with fallback
                val response = fetchWeatherFromApis(location)

                if (response != null) {
                    cacheWeatherData(location, response)
                    updateWeatherState(response, location, isOffline = false)
                } else {
                    // Both APIs failed, try cached data as last resort
                    if (!tryLoadCachedData(location)) {
                        throw IllegalStateException("Failed to get weather data from all sources")
                    }
                }
            } catch (e: Exception) {
                handleWeatherError(e, location)
            }
        }
    }

    /**
     * Try to load cached weather data
     * @return true if cached data was loaded, false otherwise
     */
    private fun tryLoadCachedData(location: String): Boolean {
        val locationData = MetNoWeatherService.greenlandLocations[location]
        if (locationData != null) {
            val cachedData = WeatherDataCache.getCachedWeather(locationData.geonamesId)
            if (cachedData != null) {
                updateWeatherState(cachedData, location, isOffline = true)
                return true
            }
        }
        return false
    }

    /**
     * Fetch weather from primary and fallback APIs
     * @return WeatherResponse if successful, null if both APIs fail
     */
    private suspend fun fetchWeatherFromApis(location: String): WeatherResponse? {
        return try {
            fetchFromPrimaryApi(location)
        } catch (e: Exception) {
            try {
                fetchFromFallbackApi(location)
            } catch (fallbackError: Exception) {
                null
            }
        }
    }

    /**
     * Fetch from the currently selected primary API
     */
    private suspend fun fetchFromPrimaryApi(location: String): WeatherResponse {
        return if (useMetNorway) {
            fetchFromMetNorway(location)
        } else {
            WeatherService.getGreenlandWeather(location)
        }
    }

    /**
     * Fetch from the fallback API (opposite of primary)
     */
    private suspend fun fetchFromFallbackApi(location: String): WeatherResponse {
        return if (!useMetNorway) {
            fetchFromMetNorway(location)
        } else {
            WeatherService.getGreenlandWeather(location)
        }
    }

    /**
     * Fetch weather data from Met Norway API
     */
    private suspend fun fetchFromMetNorway(location: String): WeatherResponse {
        val locationData = MetNoWeatherService.greenlandLocations[location]
            ?: throw IllegalArgumentException("Unknown location: $location")

        val metNoResponse = fetchMetNoWeatherWithRetry(location)
        return WeatherDataMapper.mapMetNoToAppFormat(metNoResponse, locationData)
    }

    /**
     * Cache weather data for offline use
     */
    private fun cacheWeatherData(location: String, response: WeatherResponse) {
        val locationData = MetNoWeatherService.greenlandLocations[location]
        if (locationData != null) {
            WeatherDataCache.cacheWeatherData(locationData.geonamesId, response)
        }
    }

    /**
     * Update weather state with new data
     */
    private fun updateWeatherState(response: WeatherResponse, location: String, isOffline: Boolean) {
        lastUpdated = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )
        weatherState = WeatherState.Success(response)
        selectedLocation = location
        _isOfflineMode.value = isOffline
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
    
    /**
     * Translate weather conditions based on current language
     */
    fun translateWeatherCondition(condition: String): String {
        return when (languageCode) {
            "kl" -> KALAALLISUT_CONDITIONS[condition.lowercase()] ?: condition
            "da" -> DANISH_CONDITIONS[condition.lowercase()] ?: condition
            else -> condition
        }
    }

    /**
     * Translate UI labels based on current language
     */
    fun translateLabel(label: String): String {
        return when (languageCode) {
            "kl" -> KALAALLISUT_LABELS[label] ?: label
            "da" -> DANISH_LABELS[label] ?: label
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

        repeat(maxRetries) { attempt ->
            try {
                return MetNoWeatherService.getGreenlandWeather(location)
            } catch (e: HttpException) {
                if (e.code() == 403 && attempt < maxRetries - 1) {
                    println("Received 403 error, retrying in ${currentDelay}ms (attempt ${attempt + 1} of $maxRetries)")
                    delay(currentDelay)
                    currentDelay *= 2
                } else {
                    throw e
                }
            }
        }

        throw IllegalStateException("Failed to fetch weather data after $maxRetries retries")
    }
    
    /**
     * Handle weather fetching errors with fallback to cache
     */
    private fun handleWeatherError(e: Exception, location: String) {
        when (e) {
            is CancellationException -> throw e
            is UnknownHostException, is SocketTimeoutException -> {
                if (tryLoadCachedData(location)) {
                    return
                }
                weatherState = WeatherState.Error(
                    if (e is UnknownHostException) {
                        "No internet connection. Please check your network settings and try again."
                    } else {
                        "Connection timed out. Please try again later."
                    }
                )
            }
            is HttpException -> {
                weatherState = WeatherState.Error(getHttpErrorMessage(e.code()))
            }
            else -> {
                weatherState = WeatherState.Error("Error: ${e.message ?: "Unknown error occurred"}")
            }
        }
    }

    /**
     * Get user-friendly error message for HTTP error codes
     */
    private fun getHttpErrorMessage(code: Int): String {
        return when (code) {
            401 -> "API key error. Please check your API key."
            403 -> "Server error (403). The API is refusing the request. This may be due to rate limiting or an invalid API key."
            404 -> "Location not found. Please try a different location."
            429 -> "Too many requests. Please try again later."
            else -> "Server error ($code). Please try again later."
        }
    }
    
    /**
     * Check if we're in offline mode
     */
    fun isOffline(): Boolean = _isOfflineMode.value

    /**
     * Get the age of the current data in minutes
     */
    fun getDataAge(): Int? {
        val locationData = MetNoWeatherService.greenlandLocations[selectedLocation] ?: return null
        val cacheAge = WeatherDataCache.getCacheAge(locationData.geonamesId) ?: return null
        return (cacheAge / 60000).toInt()
    }

    sealed class WeatherState {
        object Loading : WeatherState()
        data class Success(val data: WeatherResponse) : WeatherState()
        data class Error(val message: String) : WeatherState()
    }

    companion object {
        // Translation mappings for weather conditions
        private val KALAALLISUT_CONDITIONS = mapOf(
            "snowy" to "Quliartorluni",
            "snow" to "Quliartorluni",
            "light snow" to "Nittaalaq",
            "partly cloudy" to "Qulisimalluni",
            "cloudy" to "Qulisimalluni",
            "clear" to "Seqinnarpoq",
            "clear sky" to "Seqinnarpoq",
            "sunny" to "Seqinnarpoq",
            "snow shower" to "Nittaalaq",
            "rain" to "Masannartuliorneq",
            "light rain" to "Masannartuliorneq",
            "drizzle" to "Masannartuliorneq",
            "heavy rain" to "Assut masannartuliorneq"
        )

        private val DANISH_CONDITIONS = mapOf(
            "snowy" to "Sne",
            "snow" to "Sne",
            "light snow" to "Let sne",
            "partly cloudy" to "Delvist skyet",
            "cloudy" to "Skyet",
            "clear" to "Solrigt",
            "clear sky" to "Solrigt",
            "sunny" to "Solrigt",
            "snow shower" to "Let sne",
            "rain" to "Regn",
            "light rain" to "Regn",
            "drizzle" to "Støvregn",
            "heavy rain" to "Kraftig regn"
        )

        private val KALAALLISUT_LABELS = mapOf(
            "Weather Details" to "Silap pissusaa pillugu paasissutissat",
            "Feels Like" to "Malugineqarpoq",
            "Humidity" to "Imermik akoqassuseq",
            "Wind" to "Anorlertussusia",
            "Pressure" to "Naqitsineq",
            "UV Index" to "UV uuttortaat",
            "Forecast" to "Silap nalunaarsuutaa",
            "Today" to "Ullumi",
            "Condition" to "Silap qanoq innera",
            "Chance of Rain" to "Siallersinnaanera",
            "Low" to "Appasippoq",
            "Moderate" to "Akunnattumik",
            "High" to "Qaffasippoq",
            "Very High" to "Qaffasissaqaaq",
            "Extreme" to "Qaffasissaqaarujussuaq"
        )

        private val DANISH_LABELS = mapOf(
            "Weather Details" to "Vejrdetaljer",
            "Feels Like" to "Føles som",
            "Humidity" to "Luftfugtighed",
            "Wind" to "Vind",
            "Pressure" to "Tryk",
            "UV Index" to "UV-indeks",
            "Forecast" to "Vejrudsigt",
            "Today" to "I dag",
            "Condition" to "Tilstand",
            "Chance of Rain" to "Chance for regn",
            "Low" to "Lav",
            "Moderate" to "Moderat",
            "High" to "Høj",
            "Very High" to "Meget høj",
            "Extreme" to "Ekstrem"
        )
    }
}
