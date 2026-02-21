package com.avinashpatil.app.dishacomputer.network

import com.avinashpatil.app.dishacomputer.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class OpenRouterChatRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    val stream: Boolean = false
)

@Serializable
data class OpenRouterMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenRouterChatResponse(
    val choices: List<OpenRouterChoice>? = null,
    val error: OpenRouterError? = null
)

@Serializable
data class OpenRouterChoice(
    val message: OpenRouterChoiceMessage? = null
)

@Serializable
data class OpenRouterChoiceMessage(
    val content: String? = null
)

@Serializable
data class OpenRouterError(
    val message: String? = null
)

class GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://openrouter.ai/api/v1/chat/completions"
    private val model = "openai/gpt-oss-120b:free"

    suspend fun generateResponse(prompt: String, maxRetries: Int = 10): String {
        val apiKey = BuildConfig.OPENROUTER_API_KEY
        if (apiKey.isBlank()) {
            return "Error: OpenRouter API key is missing."
        }

        var lastException: Exception? = null

        for (attempt in 1..maxRetries) {
            try {
                return withContext(Dispatchers.IO) {
                    val request = OpenRouterChatRequest(
                        model = model,
                        messages = listOf(
                            OpenRouterMessage(role = "user", content = prompt)
                        )
                    )

                    val requestBody = json.encodeToString(request)
                        .toRequestBody("application/json".toMediaType())

                    val httpRequest = Request.Builder()
                        .url(baseUrl)
                        .addHeader("Authorization", "Bearer $apiKey")
                        .post(requestBody)
                        .build()

                    val response = client.newCall(httpRequest).execute()
                    val responseBody = response.body?.string() ?: throw Exception("Empty response")

                    if (!response.isSuccessful) {
                        val parsed = runCatching { json.decodeFromString<OpenRouterChatResponse>(responseBody) }.getOrNull()
                        val msg = parsed?.error?.message
                        if (!msg.isNullOrBlank()) {
                            throw Exception("API request failed: ${response.code}. $msg")
                        }
                        throw Exception("API request failed: ${response.code}")
                    }

                    val openRouterResponse = json.decodeFromString<OpenRouterChatResponse>(responseBody)
                    openRouterResponse.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content
                        ?.takeIf { it.isNotBlank() }
                        ?: "Sorry, I couldn't generate a response."
                }
            } catch (e: Exception) {
                lastException = e
                if (attempt == maxRetries) {
                    return "Error: Network request failed after $maxRetries attempts. ${e.message}"
                } else {
                    // Exponential backoff delay
                    val delayMs = (1000L * (1 shl (attempt - 1))).coerceAtMost(10000L)
                    kotlinx.coroutines.delay(delayMs)
                }
            }
        }

        return "Error: Unexpected error occurred. ${lastException?.message}"
    }
}
