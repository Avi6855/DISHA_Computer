package com.avinashpatil.app.dishacomputer.utils

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import retrofit2.HttpException

object ErrorUtils {
    fun getFriendlyErrorMessage(t: Throwable): String {
        return when (t) {
            is UnknownHostException -> "Please connect with the stable connection."
            is ConnectException -> "Please connect with the stable connection."
            is SocketTimeoutException -> "Connection timed out. Please try again."
            is TimeoutException -> "Request timed out. Please try again."
            is HttpException -> {
                when (t.code()) {
                    401 -> "Session expired. Please login again."
                    403 -> "You don't have permission to access this resource."
                    404 -> "Resource not found."
                    500, 502, 503, 504 -> "Server error. Please try again later."
                    else -> "Something went wrong. Please try again."
                }
            }
            else -> {
                val message = t.message ?: return "Something went wrong. Please try again."
                if (message.contains("Failed to connect to", ignoreCase = true) || 
                    message.contains("Unable to resolve host", ignoreCase = true) ||
                    message.contains("No address associated with hostname", ignoreCase = true)) {
                    "Please connect with the stable connection."
                } else {
                    "Something went wrong. Please try again."
                }
            }
        }
    }
}
