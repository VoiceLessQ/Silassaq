package com.example.silassaq.data

data class WeatherResponse(
    val current: CurrentWeather,
    val location: Location,
    val forecast: Forecast
)

data class CurrentWeather(
    val temp_c: Float,
    val condition: Condition,
    val wind_kph: Float,
    val wind_dir: String,
    val humidity: Int,
    val feelslike_c: Float,
    val uv: Float,
    val last_updated: String
)

data class Location(
    val name: String,
    val region: String,
    val country: String,
    val localtime: String
)

data class Condition(
    val text: String,
    val icon: String,
    val code: Int
)

data class Forecast(
    val forecastday: List<ForecastDay>
)

data class ForecastDay(
    val date: String,
    val day: Day,
    val astro: Astro,
    val hour: List<Hour>
)

data class Day(
    val maxtemp_c: Float,
    val mintemp_c: Float,
    val avgtemp_c: Float,
    val condition: Condition,
    val daily_chance_of_rain: Int
)

data class Astro(
    val sunrise: String,
    val sunset: String
)

data class Hour(
    val time: String,
    val temp_c: Float,
    val condition: Condition,
    val chance_of_rain: Int
) 