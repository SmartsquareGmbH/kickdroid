package de.smartsquare.kickchain.android.client.findmatch

/**
 * @author Ruben Gees
 */
data class Player(
    val name: String,
    val wins: Int?,
    val losses: Int?,
    val isTeamMate: Boolean = false,
    val isOpponent: Boolean = false
)
