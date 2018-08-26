package de.smartsquare.kickdroid.base

import de.smartsquare.kickdroid.R
import io.mockk.mockk
import org.amshove.kluent.shouldEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * @author Ruben Gees
 */
class DefaultErrorHandlerTest {

    @ParameterizedTest(name = "error code {arguments} should result in a client error")
    @ValueSource(ints = [400, 404, 499])
    fun `client error`(code: Int) {
        val result = DefaultErrorHandler.handle(HttpException(Response.error<Nothing>(code, mockk())))

        result shouldEqualTo R.string.error_client
    }

    @ParameterizedTest(name = "error code {arguments} should result in a server error")
    @ValueSource(ints = [500, 501, 599])
    fun `server error`(code: Int) {
        val result = DefaultErrorHandler.handle(HttpException(Response.error<Nothing>(code, mockk())))

        result shouldEqualTo R.string.error_server
    }

    @ParameterizedTest(name = "error code {arguments} should result in a unknown error")
    @ValueSource(ints = [600, 601, 700])
    fun `other http error`(code: Int) {
        val result = DefaultErrorHandler.handle(HttpException(Response.error<Nothing>(code, mockk())))

        result shouldEqualTo R.string.error_unknown
    }

    @Test
    fun `timeout error`() {
        val result = DefaultErrorHandler.handle(SocketTimeoutException())

        result shouldEqualTo R.string.error_timeout
    }

    @Test
    fun `io error`() {
        val result = DefaultErrorHandler.handle(IOException())

        result shouldEqualTo R.string.error_io
    }

    @Test
    fun `other error`() {
        val result = DefaultErrorHandler.handle(RuntimeException())

        result shouldEqualTo R.string.error_unknown
    }
}
