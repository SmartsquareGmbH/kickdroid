package de.smartsquare.kickdroid.user

import de.smartsquare.kickdroid.R
import io.mockk.mockk
import org.amshove.kluent.shouldEqualTo
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 * @author Ruben Gees
 */
class UserErrorHandlerTest {

    @Test
    fun conflict() {
        val result = UserErrorHandler.handle(HttpException(Response.error<Nothing>(409, mockk())))

        result shouldEqualTo R.string.user_error_conflict
    }

    @Test
    fun other() {
        val result = UserErrorHandler.handle(SocketTimeoutException())

        result shouldEqualTo R.string.error_timeout
    }
}
