package com.example.silassaq.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

/**
 * NOAA Space Weather Prediction Center API for Aurora forecasting
 * Data source: https://services.swpc.noaa.gov/
 */
data class AuroraData(
    val kpIndex: Float,
    val viewingProbability: Float,
    val cloudCover: Float
)

interface NoaaSpaceWeatherApi {
    @GET("products/noaa-planetary-k-index.json")
    suspend fun getKpIndex(): List<List<String>>
}

object AuroraForecastService {
    private const val BASE_URL = "https://services.swpc.noaa.gov/"

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(NoaaSpaceWeatherApi::class.java)

    /**
     * Get Aurora forecast data for a given latitude
     * @param latitude Location latitude (Greenland: 59-83°N)
     * @param cloudCover Current cloud cover percentage (0-100)
     * @return AuroraData with Kp index and viewing probability
     */
    suspend fun getAuroraForecast(latitude: Double, cloudCover: Float = 0f): AuroraData {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch current Kp index from NOAA
                val kpData = api.getKpIndex()

                // Parse the latest Kp index (last entry in the data)
                val latestKp = if (kpData.size > 1) {
                    // Skip header row, get last data row
                    val lastEntry = kpData.last()
                    if (lastEntry.size > 1) {
                        lastEntry[1].toFloatOrNull() ?: 3.0f
                    } else {
                        3.0f
                    }
                } else {
                    3.0f // Default moderate value
                }

                // Calculate viewing probability based on Kp index and latitude
                val viewingProb = calculateViewingProbability(latestKp, latitude, cloudCover)

                AuroraData(
                    kpIndex = latestKp,
                    viewingProbability = viewingProb,
                    cloudCover = cloudCover
                )
            } catch (e: Exception) {
                // Fallback to moderate values if API fails
                println("Aurora forecast fetch failed: ${e.message}")
                AuroraData(
                    kpIndex = 3.0f,
                    viewingProbability = 50f,
                    cloudCover = cloudCover
                )
            }
        }
    }

    /**
     * Calculate aurora viewing probability based on Kp index and location
     * Higher Kp = more likely to see aurora at lower latitudes
     * Greenland latitudes: 59-83°N are excellent for aurora viewing
     */
    private fun calculateViewingProbability(
        kpIndex: Float,
        latitude: Double,
        cloudCover: Float
    ): Float {
        // Base probability based on Kp index and latitude
        val baseProbability = when {
            latitude >= 75 -> { // High Arctic (Qaanaaq, Thule)
                when {
                    kpIndex >= 5 -> 95f
                    kpIndex >= 4 -> 85f
                    kpIndex >= 3 -> 75f
                    kpIndex >= 2 -> 60f
                    else -> 40f
                }
            }
            latitude >= 68 -> { // Mid-Arctic (Ilulissat, Upernavik)
                when {
                    kpIndex >= 5 -> 90f
                    kpIndex >= 4 -> 80f
                    kpIndex >= 3 -> 70f
                    kpIndex >= 2 -> 50f
                    else -> 30f
                }
            }
            latitude >= 60 -> { // Southern Greenland (Nuuk, Qaqortoq)
                when {
                    kpIndex >= 5 -> 85f
                    kpIndex >= 4 -> 70f
                    kpIndex >= 3 -> 55f
                    kpIndex >= 2 -> 35f
                    else -> 20f
                }
            }
            else -> { // Below typical Greenland latitudes
                when {
                    kpIndex >= 5 -> 70f
                    kpIndex >= 4 -> 50f
                    kpIndex >= 3 -> 30f
                    else -> 10f
                }
            }
        }

        // Reduce probability based on cloud cover
        val cloudReduction = cloudCover / 100f * 0.8f // 80% reduction at 100% clouds
        val adjustedProbability = baseProbability * (1 - cloudReduction)

        return adjustedProbability.coerceIn(0f, 100f)
    }

    /**
     * Get Kp index description
     */
    fun getKpDescription(kpIndex: Float): String {
        return when {
            kpIndex >= 7 -> "Strong geomagnetic storm"
            kpIndex >= 5 -> "Minor geomagnetic storm"
            kpIndex >= 4 -> "Active conditions"
            kpIndex >= 2 -> "Unsettled conditions"
            else -> "Quiet conditions"
        }
    }
}
