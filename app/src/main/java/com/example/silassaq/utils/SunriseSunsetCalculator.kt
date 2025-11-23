package com.example.silassaq.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.*

/**
 * Calculate sunrise, sunset, and twilight times for a given location and date
 * Uses simplified NOAA solar position algorithm
 */
object SunriseSunsetCalculator {

    private const val ZENITH_OFFICIAL = 90.833 // Official sunrise/sunset
    private const val ZENITH_CIVIL = 96.0      // Civil twilight
    private const val ZENITH_NAUTICAL = 102.0  // Nautical twilight
    private const val ZENITH_ASTRONOMICAL = 108.0 // Astronomical twilight

    data class SunTimes(
        val sunrise: LocalTime?,
        val sunset: LocalTime?,
        val civilTwilightMorning: LocalTime?,
        val civilTwilightEvening: LocalTime?,
        val nauticalTwilightMorning: LocalTime?,
        val nauticalTwilightEvening: LocalTime?,
        val isPolarDay: Boolean,
        val isPolarNight: Boolean,
        val daylightHours: Float
    )

    /**
     * Calculate sun times for a given location and date
     */
    fun calculate(latitude: Double, longitude: Double, date: LocalDate): SunTimes {
        val sunrise = calculateSunTime(latitude, longitude, date, ZENITH_OFFICIAL, true)
        val sunset = calculateSunTime(latitude, longitude, date, ZENITH_OFFICIAL, false)

        val civilMorning = calculateSunTime(latitude, longitude, date, ZENITH_CIVIL, true)
        val civilEvening = calculateSunTime(latitude, longitude, date, ZENITH_CIVIL, false)

        val nauticalMorning = calculateSunTime(latitude, longitude, date, ZENITH_NAUTICAL, true)
        val nauticalEvening = calculateSunTime(latitude, longitude, date, ZENITH_NAUTICAL, false)

        val isPolarDay = sunrise == null && sunset == null && isDaytime(latitude, longitude, date)
        val isPolarNight = sunrise == null && sunset == null && !isDaytime(latitude, longitude, date)

        val daylightHours = if (isPolarDay) {
            24f
        } else if (isPolarNight) {
            0f
        } else if (sunrise != null && sunset != null) {
            calculateDuration(sunrise, sunset)
        } else {
            12f // Fallback
        }

        return SunTimes(
            sunrise = sunrise,
            sunset = sunset,
            civilTwilightMorning = civilMorning,
            civilTwilightEvening = civilEvening,
            nauticalTwilightMorning = nauticalMorning,
            nauticalTwilightEvening = nauticalEvening,
            isPolarDay = isPolarDay,
            isPolarNight = isPolarNight,
            daylightHours = daylightHours
        )
    }

    private fun calculateSunTime(
        latitude: Double,
        longitude: Double,
        date: LocalDate,
        zenith: Double,
        isSunrise: Boolean
    ): LocalTime? {
        val dayOfYear = date.dayOfYear

        // Calculate the approximate time
        val t = if (isSunrise) {
            dayOfYear + ((6.0 - (longitude / 15.0)) / 24.0)
        } else {
            dayOfYear + ((18.0 - (longitude / 15.0)) / 24.0)
        }

        // Calculate Sun's mean anomaly
        val M = (0.9856 * t) - 3.289

        // Calculate Sun's true longitude
        val L = M + (1.916 * sin(Math.toRadians(M))) + (0.020 * sin(Math.toRadians(2 * M))) + 282.634
        val Lnorm = normalizeAngle(L)

        // Calculate Sun's right ascension
        val RA = Math.toDegrees(atan(0.91764 * tan(Math.toRadians(Lnorm))))
        val RAnorm = normalizeAngle(RA)

        // Right ascension value needs to be in the same quadrant as L
        val Lquadrant = (floor(Lnorm / 90.0)) * 90.0
        val RAquadrant = (floor(RAnorm / 90.0)) * 90.0
        val RAfinal = RAnorm + (Lquadrant - RAquadrant)

        // Convert right ascension to hours
        val RAhours = RAfinal / 15.0

        // Calculate Sun's declination
        val sinDec = 0.39782 * sin(Math.toRadians(Lnorm))
        val cosDec = cos(asin(sinDec))

        // Calculate Sun's local hour angle
        val cosH = (cos(Math.toRadians(zenith)) - (sinDec * sin(Math.toRadians(latitude)))) /
                   (cosDec * cos(Math.toRadians(latitude)))

        // Check if sun rises/sets
        if (cosH > 1) {
            return null // Sun never rises
        }
        if (cosH < -1) {
            return null // Sun never sets
        }

        // Calculate hour angle
        val H = if (isSunrise) {
            360.0 - Math.toDegrees(acos(cosH))
        } else {
            Math.toDegrees(acos(cosH))
        }

        val Hhours = H / 15.0

        // Calculate local mean time of rising/setting
        val T = Hhours + RAhours - (0.06571 * t) - 6.622

        // Adjust to UTC
        val UT = normalizeTime(T - (longitude / 15.0))

        // Convert to local time (assuming Greenland timezone, UTC-3 to UTC-1)
        // For simplicity, using UTC-2 as average
        val localTime = UT - 2.0

        return timeToLocalTime(normalizeTime(localTime))
    }

    private fun isDaytime(latitude: Double, longitude: Double, date: LocalDate): Boolean {
        val dayOfYear = date.dayOfYear
        val t = dayOfYear + ((12.0 - (longitude / 15.0)) / 24.0)
        val M = (0.9856 * t) - 3.289
        val L = M + (1.916 * sin(Math.toRadians(M))) + (0.020 * sin(Math.toRadians(2 * M))) + 282.634
        val Lnorm = normalizeAngle(L)

        val sinDec = 0.39782 * sin(Math.toRadians(Lnorm))
        val sunAltitude = asin(sinDec * sin(Math.toRadians(latitude)))

        return Math.toDegrees(sunAltitude) > -0.833
    }

    private fun normalizeAngle(angle: Double): Double {
        var result = angle
        while (result < 0) result += 360.0
        while (result >= 360) result -= 360.0
        return result
    }

    private fun normalizeTime(time: Double): Double {
        var result = time
        while (result < 0) result += 24.0
        while (result >= 24) result -= 24.0
        return result
    }

    private fun timeToLocalTime(hours: Double): LocalTime {
        val hour = floor(hours).toInt()
        val minute = ((hours - hour) * 60).toInt()
        val second = (((hours - hour) * 60 - minute) * 60).toInt()
        return LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59), second.coerceIn(0, 59))
    }

    private fun calculateDuration(start: LocalTime, end: LocalTime): Float {
        val startSeconds = start.toSecondOfDay()
        val endSeconds = end.toSecondOfDay()
        return if (endSeconds > startSeconds) {
            (endSeconds - startSeconds) / 3600f
        } else {
            (24 * 3600 - startSeconds + endSeconds) / 3600f
        }
    }
}
