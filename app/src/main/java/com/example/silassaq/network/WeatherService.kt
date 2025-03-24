package com.example.silassaq.network

import com.example.silassaq.data.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("aqi") aqi: String = "no"
    ): WeatherResponse
    
    @GET("v1/forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int = 7,
        @Query("aqi") aqi: String = "no",
        @Query("alerts") alerts: String = "no"
    ): WeatherResponse
}

object WeatherService {
    // Use the API key from ApiKeys
    private val API_KEY = ApiKeys.WEATHER_API_KEY
    private const val BASE_URL = "https://api.weatherapi.com/"
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val weatherApi = retrofit.create(WeatherApi::class.java)
    
    // List of Greenland locations
    val greenlandLocations = listOf(
        "Nuuk, Greenland",
        "Ilulissat, Greenland",
        "Sisimiut, Greenland",
        "Qaqortoq, Greenland",
        "Tasiilaq, Greenland"
    )
    
    // Get weather for a specific location
    suspend fun getGreenlandWeather(location: String): WeatherResponse {
        return weatherApi.getForecast(API_KEY, location)
    }
}