package com.avinashpatil.app.dishacomputer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.dishacomputer.data.model.ChatMessage
import com.avinashpatil.app.dishacomputer.network.openrouter.OpenRouterMessage
import com.avinashpatil.app.dishacomputer.repository.DishaAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import javax.inject.Inject

data class ModernDishaAIUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isStreaming: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ModernDishaAIViewModel @Inject constructor(
    private val dishaAIRepository: DishaAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModernDishaAIUiState())
    val uiState = _uiState.asStateFlow()

    fun sendMessage(userMessage: String) {
        val trimmed = userMessage.trim()
        if (trimmed.isEmpty()) return

        _uiState.update { state ->
            state.copy(
                messages = state.messages +
                    ChatMessage(text = trimmed, isUser = true) +
                    ChatMessage(text = "", isUser = false, isLoading = true),
                isStreaming = true,
                errorMessage = null
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                // Build conversation with a DISHA-specific system prompt
                val systemPrompt = OpenRouterMessage(
                    role = "system",
                    content = "You are DISHA AI, an intelligent assistant for DISHA Computer — " +
                        "a computer science education app for students. Help students with " +
                        "computer science topics, programming, exam preparation, and related queries. " +
                        "Be friendly, concise, and educational."
                )
                val conversation = listOf(systemPrompt) + _uiState.value.messages
                    .filterNot { !it.isUser && it.isLoading && it.text.isBlank() }
                    .map { msg ->
                        OpenRouterMessage(
                            role = if (msg.isUser) "user" else "assistant",
                            content = msg.text
                        )
                    }

                // openrouter/auto is OpenRouter's own smart router:
                // it automatically picks the best available free model.
                // Fall back to explicit models if auto fails.
                val modelsToTry = listOf(
                    "openrouter/auto",
                    "mistralai/mistral-7b-instruct:free",
                    "meta-llama/llama-3.3-70b-instruct:free",
                    "google/gemini-2.0-flash-exp:free",
                    "qwen/qwen-2.5-coder-32b-instruct:free",
                    "deepseek/deepseek-r1:free"
                )

                var success = false
                var lastError: Throwable? = null

                for (model in modelsToTry) {
                    try {
                        dishaAIRepository.streamChatCompletion(
                            model = model,
                            messages = conversation
                        ).collect { delta ->
                            if (delta.isNotEmpty()) {
                                success = true
                                _uiState.update { state ->
                                    val updated = state.messages.toMutableList()
                                    val lastIndex = updated.indexOfLast { !it.isUser }
                                    if (lastIndex >= 0) {
                                        val last = updated[lastIndex]
                                        updated[lastIndex] = last.copy(
                                            text = last.text + delta,
                                            isLoading = true
                                        )
                                    }
                                    state.copy(messages = updated, isStreaming = true, errorMessage = null)
                                }
                                yield()
                            }
                        }

                        if (success) break
                    } catch (t: Throwable) {
                        lastError = t
                        if (success) break
                        val msg = t.message.orEmpty()
                        val isRetryable = msg.contains("Provider returned error", ignoreCase = true) ||
                            msg.contains("not a valid model", ignoreCase = true) ||
                            msg.contains("No endpoints found", ignoreCase = true) ||
                            msg.contains("429") ||
                            msg.contains("Too Many Requests", ignoreCase = true) ||
                            msg.contains("502") ||
                            msg.contains("503") ||
                            msg.contains("504") ||
                            msg.contains("timeout", ignoreCase = true) ||
                            msg.contains("upstream", ignoreCase = true)
                        if (isRetryable) continue else break
                    }
                }

                // Finalize the UI based on outcome
                _uiState.update { state ->
                    val updated = state.messages.toMutableList()
                    val lastIndex = updated.indexOfLast { !it.isUser }

                    if (success) {
                        if (lastIndex >= 0) {
                            updated[lastIndex] = updated[lastIndex].copy(isLoading = false)
                        }
                    } else {
                        val errorMsg = lastError?.message
                            ?.takeIf { it.isNotBlank() }
                            ?: "AI service unavailable. Please try again later."
                        if (lastIndex >= 0) {
                            if (updated[lastIndex].text.isBlank()) {
                                updated.removeAt(lastIndex)
                            } else {
                                updated[lastIndex] = updated[lastIndex].copy(isLoading = false)
                            }
                        }
                        updated.add(ChatMessage(text = errorMsg, isUser = false))
                    }
                    state.copy(messages = updated, isStreaming = false, errorMessage = null)
                }
            }.onFailure { t ->
                // Safety net: catch any unexpected exception so it never crashes the app
                val friendly = t.message?.takeIf { it.isNotBlank() }
                    ?: "Something went wrong. Please try again."
                _uiState.update { state ->
                    val updated = state.messages.toMutableList()
                    val lastIndex = updated.indexOfLast { !it.isUser }
                    if (lastIndex >= 0) {
                        val last = updated[lastIndex]
                        if (last.text.isBlank()) updated.removeAt(lastIndex)
                        else updated[lastIndex] = last.copy(isLoading = false)
                    }
                    updated.add(ChatMessage(text = friendly, isUser = false))
                    state.copy(messages = updated, isStreaming = false, errorMessage = null)
                }
            }
        }
    }

    fun clearChat() {
        _uiState.value = ModernDishaAIUiState()
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
