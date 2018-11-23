package de.smartsquare.kickdroid.statistics

import com.squareup.moshi.JsonClass

/**
 * @author Ruben Gees
 */
@JsonClass(generateAdapter = true)
data class Player(
    val name: String,
    val totalWins: Int,
    val totalGoals: Int
)
