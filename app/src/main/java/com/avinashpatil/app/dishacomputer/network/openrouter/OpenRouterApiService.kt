package com.avinashpatil.app.dishacomputer.network.openrouter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import com.avinashpatil.app.dishacomputer.di.OpenRouterOkHttp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenRouterApiService @Inject constructor(
    @OpenRouterOkHttp private val okHttpClient: OkHttpClient
) {
    private var apiKeyOverride: String? = null
    private var chatCompletionsUrlOverride: String? = null

    /**
     * Test seam: lets unit tests point the streaming client at a local server with a fixed key,
     * without changing how Hilt builds the production instance.
     */
    internal constructor(
        okHttpClient: OkHttpClient,
        apiKey: String,
        chatCompletionsUrl: String
    ) : this(okHttpClient) {
        this.apiKeyOverride = apiKey
        this.chatCompletionsUrlOverride = chatCompletionsUrl
    }

    private val apiKey: String
        get() = apiKeyOverride ?: OPENROUTER_API_KEY

    private val chatCompletionsUrl: String
        get() = chatCompletionsUrlOverride ?: OPENROUTER_CHAT_COMPLETIONS_URL

    fun streamChatCompletion(request: OpenRouterChatRequest): Flow<String> = callbackFlow {
        if (apiKey.isBlank()) {
            close(IllegalStateException("Missing OPENROUTER_API_KEY"))
            return@callbackFlow
        }

        val bodyJson = JSONObject().apply {
            put("model", request.model)
            put("stream", request.stream)
            put(
                "messages",
                JSONArray().apply {
                    request.messages.forEach { msg ->
                        put(
                            JSONObject().apply {
                                put("role", msg.role)
                                put("content", msg.content)
                            }
                        )
                    }
                }
            )
        }

        val call = okHttpClient.newCall(
            Request.Builder()
                .url(chatCompletionsUrl)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "text/event-stream")
                .addHeader("HTTP-Referer", "app://com.avinashpatil.app.dishacomputer")
                .addHeader("X-Title", "DISHA Computer")
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()
        )

        val job = launch(Dispatchers.IO) {
            try {
                call.execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string().orEmpty()
                        val message = parseErrorMessage(errorBody).ifBlank {
                            "OpenRouter request failed (${response.code})"
                        }
                        close(IllegalStateException(message))
                        return@use
                    }

                    val body = response.body
                        ?: run {
                            close(IllegalStateException("Empty response body"))
                            return@use
                        }

                    val contentType = response.header("Content-Type").orEmpty()
                    val isEventStream = contentType.contains("text/event-stream", ignoreCase = true)
                    if (!isEventStream) {
                        val raw = body.string()
                        val content = extractNonStreamingContent(raw)
                        if (content.isBlank()) {
                            close(IllegalStateException(parseErrorMessage(raw).ifBlank { "Empty AI response" }))
                        } else {
                            trySend(content)
                            close()
                        }
                        return@use
                    }

                    val source = body.source()

                    while (isActive && !source.exhausted()) {
                        val line = source.readUtf8Line() ?: continue
                        if (!line.startsWith("data:")) continue

                        val payload = line.removePrefix("data:").trim()
                        if (payload.isEmpty()) continue
                        if (payload == "[DONE]") break

                        val delta = try {
                            extractDeltaContent(payload)
                        } catch (e: IllegalStateException) {
                            // Stream error from provider — close so the ViewModel retries next model
                            close(e)
                            return@use
                        }

                        if (delta.isNotEmpty()) {
                            trySend(delta)
                        }
                    }
                    // Stream finished — close normally (success or empty)
                    close()
                }
            } catch (t: Throwable) {
                close(t)
            }
        }

        awaitClose {
            call.cancel()
            job.cancel()
        }
    }

    private fun extractDeltaContent(jsonPayload: String): String {
        val root = try {
            JSONObject(jsonPayload)
        } catch (_: Exception) {
            return ""
        }
        
        if (root.has("error")) {
            val errorObj = root.optJSONObject("error")
            val errorMsg = errorObj?.optString("message") ?: root.optString("error")
            throw IllegalStateException(errorMsg.ifBlank { "Stream provider error" })
        }

        val choices = root.optJSONArray("choices") ?: return ""
        if (choices.length() == 0) return ""
        val choice0 = choices.optJSONObject(0) ?: return ""
        val delta = choice0.optJSONObject("delta") ?: return ""
        return delta.optString("content", "")
    }

    private fun parseErrorMessage(raw: String): String {
        return try {
            if (!raw.trim().startsWith("{")) return raw
            val obj = JSONObject(raw)
            val error = obj.optJSONObject("error")
            when {
                error != null -> error.optString("message", raw)
                else -> obj.optString("message", raw)
            }
        } catch (_: Exception) {
            raw
        }
    }

    private fun extractNonStreamingContent(raw: String): String {
        return try {
            val root = JSONObject(raw)
            val choices = root.optJSONArray("choices") ?: return ""
            if (choices.length() == 0) return ""
            val choice0 = choices.optJSONObject(0) ?: return ""
            val message = choice0.optJSONObject("message") ?: return ""
            message.optString("content", "")
        } catch (_: Exception) {
            ""
        }
    }
}
