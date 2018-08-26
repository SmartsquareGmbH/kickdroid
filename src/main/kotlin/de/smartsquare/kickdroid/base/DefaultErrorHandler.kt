package de.smartsquare.kickdroid.base

import androidx.annotation.StringRes
import de.smartsquare.kickdroid.R
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * @author Ruben Gees
 */
object DefaultErrorHandler {

    @StringRes
    fun handle(error: Throwable): Int {
        return when (error) {
            is HttpException -> when {
                error.code() in 400..499 -> R.string.error_client
                error.code() in 500..599 -> R.string.error_server
                else -> R.string.error_unknown
            }
            is SocketTimeoutException -> R.string.error_timeout
            is IOException -> R.string.error_io
            else -> R.string.error_unknown
        }
    }
}
