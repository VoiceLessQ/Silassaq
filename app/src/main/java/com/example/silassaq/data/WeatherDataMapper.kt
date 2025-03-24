package com.example.silassaq.data

import com.example.silassaq.network.MetNoWeatherService
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Utility class to map between different weather data formats
 */
object WeatherDataMapper {
    
    /**
     * Map Met Norway API response to the app's WeatherResponse format
     * 
     * @param metNoResponse The response from Met Norway API
     * @param locationData Location information
     * @return Converted WeatherResponse object
     */
    fun mapMetNoToAppFormat(
        metNoResponse: MetNoWeatherResponse,
        locationData: MetNoWeatherService.LocationData
    ): WeatherResponse {
        // Extract current weather from first timeseries
        val currentTimeseries = metNoResponse.properties.timeseries.firstOrNull()
            ?: throw IllegalStateException("No timeseries data available")
        
        val details = currentTimeseries.data.instant.details
        val next1Hour = currentTimeseries.data.next_1_hours
        val next6Hours = currentTimeseries.data.next_6_hours
        
        // Create location object
        val location = Location(
            name = locationData.name,
            region = locationData.region,
            country = "Greenland",
            localtime = currentTimeseries.time
        )
        
        // Create current weather object
        val current = CurrentWeather(
            temp_c = details.air_temperature?.toFloat() ?: 0f,
            condition = Condition(
                text = getWeatherConditionText(next1Hour?.summary?.symbol_code),
                icon = getWeatherIconUrl(next1Hour?.summary?.symbol_code),
                code = getWeatherConditionCode(next1Hour?.summary?.symbol_code)
            ),
            wind_kph = (details.wind_speed?.times(3.6))?.toFloat() ?: 0f, // Convert m/s to km/h
            wind_dir = getWindDirection(details.wind_from_direction),
            humidity = details.relative_humidity?.toInt() ?: 0,
            feelslike_c = calculateFeelsLikeTemperature(
                details.air_temperature ?: 0.0,
                details.wind_speed ?: 0.0,
                details.relative_humidity ?: 0.0
            ).toFloat(),
            uv = 0f, // Met Norway doesn't provide UV index
            last_updated = currentTimeseries.time
        )
        
        // Create forecast object
        val forecastDays = createForecastDays(metNoResponse.properties.timeseries)
        val forecast = Forecast(forecastDays)
        
        return WeatherResponse(current, location, forecast)
    }
    
    /**
     * Create forecast days from timeseries data
     */
    private fun createForecastDays(timeseries: List<Timeseries>): List<ForecastDay> {
        // Group timeseries by day
        val groupedByDay = timeseries.groupBy { 
            it.time.substring(0, 10) // Extract date part (YYYY-MM-DD)
        }
        
        return groupedByDay.map { (date, dayTimeseries) ->
            // Get min and max temperatures for the day
            val temperatures = dayTimeseries.mapNotNull { 
                it.data.instant.details.air_temperature 
            }
            val maxTemp = temperatures.maxOrNull()?.toFloat() ?: 0f
            val minTemp = temperatures.minOrNull()?.toFloat() ?: 0f
            val avgTemp = temperatures.average().toFloat()
            
            // Get precipitation chance and amount
            val precipitationChance = calculatePrecipitationChance(dayTimeseries)
            
            // Get the most representative condition for the day (noon if available)
            val noonTimeseries = dayTimeseries.find { it.time.contains("T12:00:00") }
                ?: dayTimeseries.firstOrNull()
            
            val condition = noonTimeseries?.let {
                val symbolCode = it.data.next_6_hours?.summary?.symbol_code
                    ?: it.data.next_1_hours?.summary?.symbol_code
                
                Condition(
                    text = getWeatherConditionText(symbolCode),
                    icon = getWeatherIconUrl(symbolCode),
                    code = getWeatherConditionCode(symbolCode)
                )
            } ?: Condition("Unknown", "", 0)
            
            // Create hourly forecast
            val hourlyForecasts = dayTimeseries.map { timeseries ->
                val hourDetails = timeseries.data.instant.details
                val hourNext1Hour = timeseries.data.next_1_hours
                
                Hour(
                    time = timeseries.time,
                    temp_c = hourDetails.air_temperature?.toFloat() ?: 0f,
                    condition = Condition(
                        text = getWeatherConditionText(hourNext1Hour?.summary?.symbol_code),
                        icon = getWeatherIconUrl(hourNext1Hour?.summary?.symbol_code),
                        code = getWeatherConditionCode(hourNext1Hour?.summary?.symbol_code)
                    ),
                    chance_of_rain = calculatePrecipitationChance(listOf(timeseries))
                )
            }
            
            // Create day object
            val day = Day(
                maxtemp_c = maxTemp,
                mintemp_c = minTemp,
                avgtemp_c = avgTemp,
                condition = condition,
                daily_chance_of_rain = precipitationChance
            )
            
            // Create astro object with sunrise/sunset info
            // Note: Met Norway doesn't provide this directly, so we're using placeholder values
            val astro = Astro(
                sunrise = "06:00 AM", // Placeholder
                sunset = "06:00 PM"   // Placeholder
            )
            
            ForecastDay(
                date = date,
                day = day,
                astro = astro,
                hour = hourlyForecasts
            )
        }
    }
    
    /**
     * Calculate chance of precipitation based on timeseries data
     */
    private fun calculatePrecipitationChance(timeseries: List<Timeseries>): Int {
        // Count how many hours have precipitation
        val hoursWithPrecipitation = timeseries.count { 
            val amount = it.data.next_1_hours?.details?.precipitation_amount
                ?: it.data.next_6_hours?.details?.precipitation_amount
                ?: 0.0
            
            amount > 0.1 // More than 0.1mm is considered precipitation
        }
        
        // Calculate percentage
        return if (timeseries.isNotEmpty()) {
            ((hoursWithPrecipitation.toDouble() / timeseries.size) * 100).roundToInt()
        } else {
            0
        }
    }
    
    /**
     * Get wind direction as a string based on degrees
     */
    private fun getWindDirection(degrees: Double?): String {
        if (degrees == null) return "N"
        
        return when (((degrees + 22.5) % 360 / 45).toInt()) {
            0 -> "N"
            1 -> "NE"
            2 -> "E"
            3 -> "SE"
            4 -> "S"
            5 -> "SW"
            6 -> "W"
            7 -> "NW"
            else -> "N"
        }
    }
    
    /**
     * Get weather condition text from symbol code
     */
    private fun getWeatherConditionText(symbolCode: String?): String {
        return when {
            symbolCode == null -> "Unknown"
            symbolCode.contains("clearsky") -> "Clear"
            symbolCode.contains("fair") -> "Fair"
            symbolCode.contains("partlycloudy") -> "Partly cloudy"
            symbolCode.contains("cloudy") -> "Cloudy"
            symbolCode.contains("rain") && symbolCode.contains("thunder") -> "Thunderstorm"
            symbolCode.contains("rain") && symbolCode.contains("heavy") -> "Heavy rain"
            symbolCode.contains("rain") -> "Rain"
            symbolCode.contains("snow") && symbolCode.contains("heavy") -> "Heavy snow"
            symbolCode.contains("snow") -> "Snow"
            symbolCode.contains("sleet") -> "Sleet"
            symbolCode.contains("fog") -> "Fog"
            symbolCode.contains("thunder") -> "Thunder"
            else -> "Unknown"
        }
    }
    
    /**
     * Get weather icon URL from symbol code
     */
    private fun getWeatherIconUrl(symbolCode: String?): String {
        // Met Norway provides weather icons, but we'll use a placeholder for now
        // In a real implementation, we would map to appropriate icon URLs
        return "https://api.met.no/images/weathericons/png/$symbolCode.png"
    }
    
    /**
     * Get weather condition code from symbol code
     */
    private fun getWeatherConditionCode(symbolCode: String?): Int {
        // Map Met Norway symbol codes to our internal condition codes
        // This is a simplified mapping
        return when {
            symbolCode == null -> 0
            symbolCode.contains("clearsky") -> 1000
            symbolCode.contains("fair") -> 1003
            symbolCode.contains("partlycloudy") -> 1003
            symbolCode.contains("cloudy") -> 1006
            symbolCode.contains("rain") && symbolCode.contains("thunder") -> 1087
            symbolCode.contains("rain") && symbolCode.contains("heavy") -> 1195
            symbolCode.contains("rain") -> 1063
            symbolCode.contains("snow") && symbolCode.contains("heavy") -> 1225
            symbolCode.contains("snow") -> 1066
            symbolCode.contains("sleet") -> 1069
            symbolCode.contains("fog") -> 1135
            symbolCode.contains("thunder") -> 1087
            else -> 0
        }
    }
    
    /**
     * Calculate "feels like" temperature using wind chill and heat index
     */
    private fun calculateFeelsLikeTemperature(
        tempC: Double,
        windSpeedMs: Double,
        humidity: Double
    ): Double {
        // Convert wind speed from m/s to km/h for the formula
        val windSpeedKmh = windSpeedMs * 3.6
        
        return when {
            // Wind chill for cold temperatures
            tempC <= 10 && windSpeedKmh > 4.8 -> {
                13.12 + 0.6215 * tempC - 11.37 * windSpeedKmh.pow(0.16) + 0.3965 * tempC * windSpeedKmh.pow(0.16)
            }
            // Heat index for warm temperatures
            tempC >= 27 && humidity >= 40 -> {
                val c1 = -8.78469475556
                val c2 = 1.61139411
                val c3 = 2.33854883889
                val c4 = -0.14611605
                val c5 = -0.012308094
                val c6 = -0.0164248277778
                val c7 = 0.002211732
                val c8 = 0.00072546
                val c9 = -0.000003582
                
                c1 + c2*tempC + c3*humidity + c4*tempC*humidity + c5*tempC*tempC +
                c6*humidity*humidity + c7*tempC*tempC*humidity + c8*tempC*humidity*humidity +
                c9*tempC*tempC*humidity*humidity
            }
            // For moderate conditions, feels like is the same as actual temperature
            else -> tempC
        }
    }
}
