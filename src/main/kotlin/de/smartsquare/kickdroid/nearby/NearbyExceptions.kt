package de.smartsquare.kickdroid.nearby

open class NearbyException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
class NearbyInvalidMessageException(message: String? = null, cause: Throwable? = null) : NearbyException(message, cause)
