package de.smartsquare.kickchain.android.client.nearby

/**
 * @author Ruben Gees
 */
class NearbyException(
    val type: NearbyExceptionType,
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    enum class NearbyExceptionType {
        PERMISSION, API, EXPIRED, UNKNOWN
    }
}
