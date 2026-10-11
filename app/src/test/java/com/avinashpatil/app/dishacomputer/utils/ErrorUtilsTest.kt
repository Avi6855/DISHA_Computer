package com.avinashpatil.app.dishacomputer.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/**
 * ErrorUtils is the single place where every network failure becomes a message the student reads.
 * These tests pin the mapping so a refactor cannot silently start showing raw exceptions in exams.
 */
class ErrorUtilsTest {

    private fun httpException(code: Int): HttpException = HttpException(
        Response.error<Any>(code, "{}".toResponseBody("application/json".toMediaType()))
    )

    @Test
    fun `no internet exceptions ask for a stable connection`() {
        val expected = "Please connect with the stable connection."
        assertEquals(expected, ErrorUtils.getFriendlyErrorMessage(UnknownHostException("api.disha")))
        assertEquals(expected, ErrorUtils.getFriendlyErrorMessage(ConnectException("refused")))
    }

    @Test
    fun `timeouts ask the student to retry`() {
        assertEquals(
            "Connection timed out. Please try again.",
            ErrorUtils.getFriendlyErrorMessage(SocketTimeoutException("timeout"))
        )
        assertEquals(
            "Request timed out. Please try again.",
            ErrorUtils.getFriendlyErrorMessage(TimeoutException("timeout"))
        )
    }

    @Test
    fun `http status codes map to the right message`() {
        assertEquals("Session expired. Please login again.", ErrorUtils.getFriendlyErrorMessage(httpException(401)))
        assertEquals(
            "You don't have permission to access this resource.",
            ErrorUtils.getFriendlyErrorMessage(httpException(403))
        )
        assertEquals("Resource not found.", ErrorUtils.getFriendlyErrorMessage(httpException(404)))
        listOf(500, 502, 503, 504).forEach { code ->
            assertEquals(
                "Server error. Please try again later.",
                ErrorUtils.getFriendlyErrorMessage(httpException(code))
            )
        }
        assertEquals("Something went wrong. Please try again.", ErrorUtils.getFriendlyErrorMessage(httpException(418)))
    }

    @Test
    fun `raw okhttp connect failures are still mapped to the connection message`() {
        val expected = "Please connect with the stable connection."
        assertEquals(
            expected,
            ErrorUtils.getFriendlyErrorMessage(RuntimeException("Failed to connect to /203.0.113.1:9090"))
        )
        assertEquals(
            expected,
            ErrorUtils.getFriendlyErrorMessage(RuntimeException("Unable to resolve host \"api.disha\": No address"))
        )
        assertEquals(
            expected,
            ErrorUtils.getFriendlyErrorMessage(RuntimeException("No address associated with hostname"))
        )
    }

    @Test
    fun `unknown failures never leak the raw exception text`() {
        assertEquals(
            "Something went wrong. Please try again.",
            ErrorUtils.getFriendlyErrorMessage(RuntimeException("kotlinx.serialization internal failure"))
        )
        assertEquals(
            "Something went wrong. Please try again.",
            ErrorUtils.getFriendlyErrorMessage(RuntimeException())
        )
    }
}
