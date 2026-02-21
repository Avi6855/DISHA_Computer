package com.avinashpatil.app.dishacomputer.network

import com.avinashpatil.app.dishacomputer.api.models.LoginResponse
import com.avinashpatil.app.dishacomputer.api.models.RefreshTokenRequest
import com.avinashpatil.app.dishacomputer.data.store.TokenManager
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val baseUrl: String
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Authorization").isNullOrEmpty()) {
            return null
        }

        val refreshToken = runBlocking { tokenManager.getRefreshToken() }
        if (refreshToken.isNullOrBlank()) {
            return null
        }

        val client = OkHttpClient.Builder().build()
        val json = Json { ignoreUnknownKeys = true }
        val body = json.encodeToString(RefreshTokenRequest.serializer(), RefreshTokenRequest(refreshToken))
            .toRequestBody("application/json".toMediaType())
        val req = Request.Builder()
            .url(baseUrl + "api/auth/regenerate-token")
            .post(body)
            .build()
        val resp = client.newCall(req).execute()
        if (!resp.isSuccessful) {
            resp.close()
            return null
        }
        val respBody = resp.body?.string()
        resp.close()
        if (respBody.isNullOrBlank()) {
            return null
        }
        val parsed = try {
            json.decodeFromString(LoginResponse.serializer(), respBody)
        } catch (_: Exception) {
            return null
        }
        val newAccess = parsed.token
        val newRefresh = parsed.refreshToken?.token
        if (newAccess.isNullOrBlank() || newRefresh.isNullOrBlank()) {
            return null
        }
        runBlocking { tokenManager.saveTokens(newAccess, newRefresh) }
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccess")
            .build()
    }
}
