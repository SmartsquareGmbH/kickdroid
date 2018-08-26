package de.smartsquare.kickdroid.user

import de.smartsquare.kickdroid.R
import de.smartsquare.kickdroid.base.DefaultErrorHandler
import retrofit2.HttpException

/**
 * @author Ruben Gees
 */
object UserErrorHandler {

    fun handle(error: Throwable): Int {
        return if (error is HttpException && error.code() == 409) {
            R.string.user_error_conflict
        } else {
            DefaultErrorHandler.handle(error)
        }
    }
}
