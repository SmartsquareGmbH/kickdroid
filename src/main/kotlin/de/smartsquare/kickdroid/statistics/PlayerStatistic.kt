package de.smartsquare.kickdroid.statistics

import com.squareup.moshi.JsonClass

/**
 * @author Ruben Gees
 */
@JsonClass(generateAdapter = true)
data class PlayerStatistic(
    val averageGoalsPerGame: Int,
    val winRate: Double,
    val totalCrawls: Int,
    val totalWins: Int,
    val totalLosses: Int
)
