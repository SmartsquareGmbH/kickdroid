package de.smartsquare.kickchain.android.client.nearby

/**
 * @author Ruben Gees
 */
class NearbyException(val type: NearbyExceptionType) : Exception() {

    enum class NearbyExceptionType {
        PERMISSION, API, EXPIRED, UNKNOWN
    }
}
