package com.example.silassaq.network

import com.example.silassaq.data.MetNoWeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Service for fetching weather data from MET Norway API
 * Documentation: https://api.met.no/weatherapi/locationforecast/2.0/documentation
 */
interface MetNoWeatherApi {
    @GET("weatherapi/locationforecast/2.0/compact")
    suspend fun getWeatherForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("altitude") altitude: Int? = null
    ): MetNoWeatherResponse
}

object MetNoWeatherService {
    private const val BASE_URL = "https://api.met.no/"
    private const val USER_AGENT = "SilassaqApp/1.0 (https://github.com/VoiceLessQ/Silassaq; t12kaem@gmail.com)"
    private const val CACHE_DURATION = 3600000L // 1 hour in milliseconds
    
    // Create a custom OkHttpClient with interceptors to properly set headers
    private val okHttpClient by lazy {
        // Create a cache directory
        val cacheDir = File(System.getProperty("java.io.tmpdir"), "met_norway_cache")
        val cacheSize = 10 * 1024 * 1024L // 10 MB cache
        val cache = Cache(cacheDir, cacheSize)
        
        OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(Interceptor { chain ->
                // Create a new request with required headers
                val originalRequest = chain.request()
                val requestWithHeaders = originalRequest.newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .build()
                
                // Proceed with the modified request
                chain.proceed(requestWithHeaders)
            })
            // Add cache control interceptor for responses
            .addNetworkInterceptor(Interceptor { chain ->
                val originalResponse = chain.proceed(chain.request())
                
                // Cache for 1 hour as recommended by MET Norway
                originalResponse.newBuilder()
                    .header("Cache-Control", "public, max-age=3600")
                    .build()
            })
            // Add If-Modified-Since interceptor for requests
            .addInterceptor(Interceptor { chain ->
                val originalRequest = chain.request()
                
                // Check if we have a cached response with a Last-Modified header
                val cachedResponse = cache.get(originalRequest)
                val lastModified = cachedResponse?.header("Last-Modified")
                
                // If we have a Last-Modified header, add If-Modified-Since to the request
                val newRequest = if (lastModified != null) {
                    originalRequest.newBuilder()
                        .header("If-Modified-Since", lastModified)
                        .build()
                } else {
                    originalRequest
                }
                
                chain.proceed(newRequest)
            })
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val weatherApi = retrofit.create(MetNoWeatherApi::class.java)
    
    // Data class for location information
    data class LocationData(
        val name: String,
        val lat: Double,
        val lon: Double,
        val region: String,
        val geonamesId: String
    )
    
    // Expanded Greenland locations with regional information
    val greenlandLocations = mapOf(
        // Original 5 locations
        "Nuuk" to LocationData("Nuuk", 64.1835, -51.7216, "Sermersooq", "3421319"),
        "Ilulissat" to LocationData("Ilulissat", 69.2167, -51.1000, "Qaasuitsup", "3423146"),
        "Sisimiut" to LocationData("Sisimiut", 66.9395, -53.6735, "Qeqqata", "3419842"),
        "Qaqortoq" to LocationData("Qaqortoq", 60.7167, -46.0333, "Kujalleq", "3420846"),
        "Tasiilaq" to LocationData("Tasiilaq", 65.6145, -37.6368, "Sermersooq", "3424607"),
        
        // Additional locations from the PHP code
        "Aasiaat" to LocationData("Aasiaat", 68.7098, -52.8699, "Qaasuitsup", "3424901"),
        "Alluitsup Paa" to LocationData("Alluitsup Paa", 60.4627, -45.5695, "Kujalleq", "3424682"),
        "Ittoqqortoormiit" to LocationData("Ittoqqortoormiit", 70.4846, -21.9622, "Tunu", "3422891"),
        "Kangaatsiaq" to LocationData("Kangaatsiaq", 68.3065, -53.4641, "Qaasuitsup", "3422683"),
        "Kangerlussuaq" to LocationData("Kangerlussuaq", 67.0088, -50.6894, "Qeqqata", "3419714"),
        "Maniitsoq" to LocationData("Maniitsoq", 65.4167, -52.9000, "Qeqqata", "3421982"),
        "Nanortalik" to LocationData("Nanortalik", 60.1432, -45.2372, "Kujalleq", "3421765"),
        "Narsaq" to LocationData("Narsaq", 60.9152, -46.0526, "Kujalleq", "3421719"),
        "Narsarmijit" to LocationData("Narsarmijit", 60.0049, -44.6653, "Kujalleq", "3423771"),
        "Paamiut" to LocationData("Paamiut", 61.9940, -49.6678, "Sermersooq", "3421193"),
        "Qaanaaq" to LocationData("Qaanaaq", 77.4667, -69.2316, "Other", "3831208"),
        "Qasigiannguit" to LocationData("Qasigiannguit", 68.8193, -51.1922, "Qaasuitsup", "3420768"),
        "Qeqertarsuaq" to LocationData("Qeqertarsuaq", 69.2472, -53.5368, "Qaasuitsup", "3420635"),
        "Thule Air Base" to LocationData("Thule Air Base", 76.5311, -68.7017, "Qaasuitsup", "3831683"),
        "Upernavik" to LocationData("Upernavik", 72.7868, -56.1549, "Qaasuitsup", "3418910"),
        "Uummannaq" to LocationData("Uummannaq", 70.6747, -52.1264, "Qaasuitsup", "3426193")
    )
    
    /**
     * Utility function to round coordinates to 4 decimal places
     * as required by MET Norway API to avoid blocking
     */
    private fun roundCoordinate(coordinate: Double): Double {
        return (coordinate * 10000).toInt() / 10000.0
    }
    
    /**
     * Get weather forecast for a location in Greenland using MET Norway API
     * with coordinates rounded to 4 decimal places
     */
    suspend fun getGreenlandWeather(location: String): MetNoWeatherResponse {
        val locationData = greenlandLocations[location] ?: greenlandLocations["Nuuk"]!!
        
        return withContext(Dispatchers.IO) {
            try {
                weatherApi.getWeatherForecast(
                    latitude = roundCoordinate(locationData.lat),
                    longitude = roundCoordinate(locationData.lon)
                )
            } catch (e: Exception) {
                // Log the error and rethrow
                println("Error fetching weather data for $location: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * Get weather forecast for any location using MET Norway API
     * with coordinates rounded to 4 decimal places
     */
    suspend fun getWeatherForLocation(latitude: Double, longitude: Double, altitude: Int? = null): MetNoWeatherResponse {
        val roundedLatitude = roundCoordinate(latitude)
        val roundedLongitude = roundCoordinate(longitude)
        
        return withContext(Dispatchers.IO) {
            try {
                weatherApi.getWeatherForecast(
                    latitude = roundedLatitude,
                    longitude = roundedLongitude,
                    altitude = altitude
                )
            } catch (e: Exception) {
                // Log the error and rethrow
                println("Error fetching weather data for coordinates ($roundedLatitude, $roundedLongitude): ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * Get all available Greenland locations grouped by region
     */
    fun getGreenlandLocationsByRegion(): Map<String, List<String>> {
        return greenlandLocations.entries
            .groupBy { it.value.region }
            .mapValues { entry -> entry.value.map { it.key } }
    }
}
