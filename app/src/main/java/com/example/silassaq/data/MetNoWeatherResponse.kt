package com.example.silassaq.data

/**
 * Data classes for the Met.no API response
 * Documentation: https://api.met.no/weatherapi/locationforecast/2.0/documentation
 */
data class MetNoWeatherResponse(
    val properties: Properties
)

data class Properties(
    val meta: Meta,
    val timeseries: List<Timeseries>
)

data class Meta(
    val updated_at: String,
    val units: Units
)

data class Units(
    val air_pressure_at_sea_level: String,
    val air_temperature: String,
    val cloud_area_fraction: String,
    val precipitation_amount: String,
    val relative_humidity: String,
    val wind_from_direction: String,
    val wind_speed: String
)

data class Timeseries(
    val time: String,
    val data: Data
)

data class Data(
    val instant: Instant,
    val next_1_hours: NextHours? = null,
    val next_6_hours: NextHours? = null,
    val next_12_hours: NextHours? = null
)

data class Instant(
    val details: InstantDetails
)

data class InstantDetails(
    val air_pressure_at_sea_level: Double? = null,
    val air_temperature: Double? = null,
    val cloud_area_fraction: Double? = null,
    val relative_humidity: Double? = null,
    val wind_from_direction: Double? = null,
    val wind_speed: Double? = null
)

data class NextHours(
    val summary: Summary? = null,
    val details: NextHoursDetails? = null
)

data class Summary(
    val symbol_code: String? = null
)

data class NextHoursDetails(
    val precipitation_amount: Double? = null
) 