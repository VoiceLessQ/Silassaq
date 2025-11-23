package com.example.silassaq.data

import com.example.silassaq.network.MetNoWeatherService
import com.example.silassaq.utils.SunriseSunsetCalculator
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Utility class to map between different weather data formats
 */
object WeatherDataMapper {

    private const val MS_TO_KMH = 3.6
    private const val PRECIPITATION_THRESHOLD_MM = 0.1

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
        val currentTimeseries = metNoResponse.properties.timeseries.firstOrNull()
            ?: throw IllegalStateException("No timeseries data available")

        val details = currentTimeseries.data.instant.details
        val symbolCode = currentTimeseries.data.next_1_hours?.summary?.symbol_code

        val location = Location(
            name = locationData.name,
            region = locationData.region,
            country = "Greenland",
            localtime = currentTimeseries.time
        )

        val current = CurrentWeather(
            temp_c = details.air_temperature?.toFloat() ?: 0f,
            condition = createCondition(symbolCode),
            wind_kph = (details.wind_speed?.times(MS_TO_KMH))?.toFloat() ?: 0f,
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

        val forecastDays = createForecastDays(
            metNoResponse.properties.timeseries,
            locationData.lat,
            locationData.lon
        )
        return WeatherResponse(current, location, Forecast(forecastDays))
    }

    /**
     * Create a Condition object from symbol code
     */
    private fun createCondition(symbolCode: String?): Condition {
        return Condition(
            text = getWeatherConditionText(symbolCode),
            icon = getWeatherIconUrl(symbolCode),
            code = getWeatherConditionCode(symbolCode)
        )
    }
    
    /**
     * Create forecast days from timeseries data
     */
    private fun createForecastDays(
        timeseries: List<Timeseries>,
        latitude: Double,
        longitude: Double
    ): List<ForecastDay> {
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
                createCondition(symbolCode)
            } ?: Condition("Unknown", "", 0)

            // Create hourly forecast
            val hourlyForecasts = dayTimeseries.map { ts ->
                Hour(
                    time = ts.time,
                    temp_c = ts.data.instant.details.air_temperature?.toFloat() ?: 0f,
                    condition = createCondition(ts.data.next_1_hours?.summary?.symbol_code),
                    chance_of_rain = calculatePrecipitationChance(listOf(ts))
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
            
            // Calculate real sunrise/sunset times for this location and date
            val localDate = LocalDate.parse(date)
            val sunTimes = SunriseSunsetCalculator.calculate(latitude, longitude, localDate)
            val astro = Astro(
                sunrise = sunTimes.sunrise?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "No sunrise",
                sunset = sunTimes.sunset?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "No sunset"
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
        if (timeseries.isEmpty()) return 0

        val hoursWithPrecipitation = timeseries.count {
            val amount = it.data.next_1_hours?.details?.precipitation_amount
                ?: it.data.next_6_hours?.details?.precipitation_amount
                ?: 0.0
            amount > PRECIPITATION_THRESHOLD_MM
        }

        return ((hoursWithPrecipitation.toDouble() / timeseries.size) * 100).roundToInt()
    }
    
    /**
     * Get wind direction as a string based on degrees
     */
    private fun getWindDirection(degrees: Double?): String {
        degrees ?: return "N"
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = (((degrees + 22.5) % 360) / 45).toInt()
        return directions.getOrElse(index) { "N" }
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
