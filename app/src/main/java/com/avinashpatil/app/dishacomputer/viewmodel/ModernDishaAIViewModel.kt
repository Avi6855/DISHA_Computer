package com.avinashpatil.app.dishacomputer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.dishacomputer.BuildConfig
import com.avinashpatil.app.dishacomputer.data.model.ChatMessage
import com.avinashpatil.app.dishacomputer.utils.ErrorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ModernDishaAIViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun sendMessage(userMessage: String, onTyping: (Boolean) -> Unit) {
        val userChat = ChatMessage(text = userMessage, isUser = true)
        _messages.value = _messages.value + userChat

        onTyping(true)

        viewModelScope.launch(Dispatchers.IO) {
            streamAIResponse(userMessage, onTyping)
        }
    }

    private fun streamAIResponse(
        userMessage: String,
        onTyping: (Boolean) -> Unit
    ) {
        val requestBody = """
        {
          "model": "openai/gpt-oss-120b:free",
          "stream": true,
          "messages": [
            { "role": "user", "content": "$userMessage" }
          ]
        }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .addHeader("Authorization", "Bearer ${BuildConfig.OPENROUTER_API_KEY}")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = client.newCall(request).execute()
            val source = response.body?.source() ?: return

            var aiMessage = ""
            var isFirstChunk = true

            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (!line.startsWith("data:")) continue
                if (line.contains("[DONE]")) break

                try {
                    val content = JSONObject(line.removePrefix("data:"))
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("delta")
                        .optString("content")

                    if (content.isNotEmpty()) {
                        aiMessage += content

                        if (isFirstChunk) {
                             // Add initial AI message
                             val aiChat = ChatMessage(text = aiMessage, isUser = false)
                             _messages.value = _messages.value + aiChat
                             isFirstChunk = false
                        } else {
                            // Update last AI message
                            val updatedList = _messages.value.toMutableList()
                            if (updatedList.isNotEmpty() && !updatedList.last().isUser) {
                                updatedList[updatedList.lastIndex] = updatedList.last().copy(text = aiMessage)
                                _messages.value = updatedList
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Optionally add error message
             val errorChat = ChatMessage(text = ErrorUtils.getFriendlyErrorMessage(e), isUser = false)
             _messages.value = _messages.value + errorChat
        } finally {
            onTyping(false)
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
    }
}
