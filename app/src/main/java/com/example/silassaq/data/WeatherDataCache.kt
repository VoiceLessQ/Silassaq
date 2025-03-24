package com.example.silassaq.data

import java.util.concurrent.ConcurrentHashMap

/**
 * Cache system for weather data to minimize API calls and provide offline functionality.
 * Implements a 1-hour cache duration as recommended by Met Norway's terms of service.
 */
object WeatherDataCache {
    private const val DEFAULT_CACHE_DURATION = 3600000L // 1 hour in milliseconds
    
    // Thread-safe map to store cached weather data
    private val cache = ConcurrentHashMap<String, CachedWeatherData>()
    
    /**
     * Data class to store cached weather data with timestamp and expiry information
     */
    data class CachedWeatherData(
        val data: WeatherResponse,
        val timestamp: Long,
        val expiryTime: Long
    )
    
    /**
     * Get cached weather data for a location if available and not expired
     * 
     * @param locationId The unique identifier for the location (e.g., GeoNames ID)
     * @return The cached WeatherResponse or null if not available or expired
     */
    fun getCachedWeather(locationId: String): WeatherResponse? {
        val cachedData = cache[locationId] ?: return null
        
        // Check if cache is still valid
        if (System.currentTimeMillis() < cachedData.expiryTime) {
            return cachedData.data
        }
        
        // Cache expired, remove it
        cache.remove(locationId)
        return null
    }
    
    /**
     * Store weather data in the cache
     * 
     * @param locationId The unique identifier for the location (e.g., GeoNames ID)
     * @param data The weather data to cache
     * @param cacheDuration How long to cache the data in milliseconds (defaults to 1 hour)
     */
    fun cacheWeatherData(
        locationId: String, 
        data: WeatherResponse, 
        cacheDuration: Long = DEFAULT_CACHE_DURATION
    ) {
        val now = System.currentTimeMillis()
        cache[locationId] = CachedWeatherData(
            data = data,
            timestamp = now,
            expiryTime = now + cacheDuration
        )
    }
    
    /**
     * Clear all cached data
     */
    fun clearCache() {
        cache.clear()
    }
    
    /**
     * Get information about the cache contents
     * 
     * @return Map of location IDs to pairs of (timestamp, expiryTime)
     */
    fun getCacheInfo(): Map<String, Pair<Long, Long>> {
        return cache.mapValues { (_, cachedData) ->
            Pair(cachedData.timestamp, cachedData.expiryTime)
        }
    }
    
    /**
     * Check if the cache contains valid data for a location
     * 
     * @param locationId The unique identifier for the location
     * @return True if valid cached data exists, false otherwise
     */
    fun hasValidCache(locationId: String): Boolean {
        val cachedData = cache[locationId] ?: return false
        return System.currentTimeMillis() < cachedData.expiryTime
    }
    
    /**
     * Get the age of cached data in milliseconds
     * 
     * @param locationId The unique identifier for the location
     * @return Age of the data in milliseconds, or null if no cached data exists
     */
    fun getCacheAge(locationId: String): Long? {
        val cachedData = cache[locationId] ?: return null
        return System.currentTimeMillis() - cachedData.timestamp
    }
}
