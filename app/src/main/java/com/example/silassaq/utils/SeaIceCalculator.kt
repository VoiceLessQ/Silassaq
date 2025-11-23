package com.example.silassaq.utils

import com.example.silassaq.ui.components.SafetyLevel
import java.time.LocalDate
import java.time.Month
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Calculate realistic sea ice conditions for Greenland locations
 * Based on historical patterns and seasonal variations
 */
object SeaIceCalculator {

    data class SeaIceData(
        val iceConcentration: Float,      // Percentage 0-100
        val iceThickness: Float,           // Meters
        val iceEdgeDistance: Float,        // Kilometers
        val safetyLevel: SafetyLevel,
        val historicalComparison: String
    )

    /**
     * Calculate sea ice conditions for a given location and date
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @param date Current date
     * @return SeaIceData with realistic conditions
     */
    fun calculate(latitude: Double, longitude: Double, date: LocalDate = LocalDate.now()): SeaIceData {
        // Get seasonal factor (0-1, where 1 is peak winter ice)
        val seasonalFactor = getSeasonalFactor(date)

        // Base ice concentration on latitude and season
        val baseConcentration = when {
            latitude >= 75 -> 70 + (seasonalFactor * 25)  // High Arctic
            latitude >= 68 -> 50 + (seasonalFactor * 35)  // Mid Arctic
            latitude >= 60 -> 20 + (seasonalFactor * 40)  // South Greenland
            else -> 10 + (seasonalFactor * 30)            // Very south
        }

        // Adjust for longitude (west coast vs east coast)
        val longitudeFactor = if (longitude < -45) {
            0.9  // West coast - warmer West Greenland Current
        } else {
            1.1  // East coast - colder East Greenland Current
        }

        val iceConcentration = (baseConcentration * longitudeFactor).toFloat().coerceIn(0f, 100f)

        // Calculate ice thickness (thicker in winter, thicker at higher latitudes)
        val baseThickness = when {
            latitude >= 75 -> 1.5 + (seasonalFactor * 0.8)
            latitude >= 68 -> 1.0 + (seasonalFactor * 0.7)
            latitude >= 60 -> 0.5 + (seasonalFactor * 0.6)
            else -> 0.3 + (seasonalFactor * 0.4)
        }

        val iceThickness = (baseThickness * longitudeFactor).toFloat()

        // Calculate distance to ice edge
        val iceEdgeDistance = when {
            iceConcentration > 80 -> (5f + (Math.random() * 10).toFloat())
            iceConcentration > 50 -> (15f + (Math.random() * 20).toFloat())
            iceConcentration > 20 -> (35f + (Math.random() * 30).toFloat())
            else -> (50f + (Math.random() * 50).toFloat())
        }

        // Determine safety level
        val safetyLevel = when {
            iceConcentration > 80 && iceThickness > 1.5 -> SafetyLevel.DANGEROUS
            iceConcentration > 60 && iceThickness > 1.0 -> SafetyLevel.MODERATE
            iceConcentration > 30 -> SafetyLevel.CAUTION
            else -> SafetyLevel.SAFE
        }

        // Generate historical comparison
        val comparison = getHistoricalComparison(iceConcentration, date)

        return SeaIceData(
            iceConcentration = iceConcentration,
            iceThickness = iceThickness,
            iceEdgeDistance = iceEdgeDistance,
            safetyLevel = safetyLevel,
            historicalComparison = comparison
        )
    }

    /**
     * Get seasonal ice factor (0-1, where 1 is peak winter)
     * Peak in March, minimum in September
     */
    private fun getSeasonalFactor(date: LocalDate): Double {
        val dayOfYear = date.dayOfYear
        // Peak ice in March (day ~75), minimum in September (day ~260)
        // Using cosine wave shifted to match Greenland ice patterns
        val phase = (dayOfYear - 75) * (2 * Math.PI / 365)
        return (1 + cos(phase)) / 2  // Range 0-1
    }

    /**
     * Generate historical comparison text
     */
    private fun getHistoricalComparison(concentration: Float, date: LocalDate): String {
        // Simulate climate change trend (generally less ice over time)
        val yearFactor = (date.year - 2024) * 0.5  // 0.5% decrease per year
        val deviation = -yearFactor + (Math.random() * 10 - 5)  // Add some randomness

        return when {
            deviation > 10 -> "${deviation.toInt()}% above average for this time of year"
            deviation > 2 -> "Slightly above average for this time of year"
            deviation > -2 -> "Near average for this time of year"
            deviation > -10 -> "Slightly below average for this time of year"
            else -> "${abs(deviation).toInt()}% below average for this time of year"
        }
    }

    /**
     * Get descriptive text for ice conditions
     */
    fun getIceDescription(concentration: Float): String {
        return when {
            concentration > 90 -> "Very close pack ice"
            concentration > 70 -> "Close pack ice"
            concentration > 50 -> "Open pack ice"
            concentration > 20 -> "Very open pack ice"
            concentration > 10 -> "Open water with ice floes"
            else -> "Ice-free water"
        }
    }

    /**
     * Get safety recommendation
     */
    fun getSafetyRecommendation(safetyLevel: SafetyLevel): String {
        return when (safetyLevel) {
            SafetyLevel.SAFE -> "Safe for navigation with standard precautions"
            SafetyLevel.CAUTION -> "Exercise caution - ice present in area"
            SafetyLevel.MODERATE -> "Moderate risk - experienced ice navigation required"
            SafetyLevel.DANGEROUS -> "Dangerous conditions - avoid or use icebreaker support"
        }
    }
}
