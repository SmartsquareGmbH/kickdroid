package de.smartsquare.kickdroid.domain

data class Lobby(
    val owner: String,
    val leftTeam: List<String>,
    val scoreLeftTeam: Int,
    val rightTeam: List<String>,
    val scoreRightTeam: Int
)
