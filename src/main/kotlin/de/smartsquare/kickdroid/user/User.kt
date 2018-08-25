package de.smartsquare.kickdroid.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @author Ruben Gees
 */
@JsonClass(generateAdapter = true)
data class User(@Json(name = "deviceId") val id: String, val name: String)
