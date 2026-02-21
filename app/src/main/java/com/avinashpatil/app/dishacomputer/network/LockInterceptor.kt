package com.avinashpatil.app.dishacomputer.network

import com.avinashpatil.app.dishacomputer.data.store.UserPreferences
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class LockInterceptor @Inject constructor(
    private val userPreferences: UserPreferences
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        try {
            // Peek at the body without consuming it to check for lock status
            // Limit to 4KB to avoid performance issues with large responses
            val body = response.peekBody(4096)
            val content = body.string()
            
            // Check for the specific error message indicating a locked account
            if (content.contains("Account is locked", ignoreCase = true)) {
                runBlocking {
                    userPreferences.setUserLockStatus(true)
                }
            }
        } catch (e: Exception) {
            // Silently fail if body cannot be read, to avoid disrupting the flow
        }

        return response
    }
}
