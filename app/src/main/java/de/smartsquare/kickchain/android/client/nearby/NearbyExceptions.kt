package de.smartsquare.kickchain.android.client.nearby

open class NearbyException constructor(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)

class NearbyUnknownMessageException(
    message: String? = null,
    cause: Throwable? = null
) : NearbyException(message, cause)

class NearbyInvalidMessageException(
    message: String? = null,
    cause: Throwable? = null
) : NearbyException(message, cause)
