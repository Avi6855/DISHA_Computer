package com.avinashpatil.app.dishacomputer.repository

import com.avinashpatil.app.dishacomputer.network.openrouter.OpenRouterApiService
import com.avinashpatil.app.dishacomputer.network.openrouter.OpenRouterChatRequest
import com.avinashpatil.app.dishacomputer.network.openrouter.OpenRouterMessage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DishaAIRepository @Inject constructor(
    private val openRouterApiService: OpenRouterApiService
) {
    fun streamChatCompletion(
        model: String,
        messages: List<OpenRouterMessage>
    ): Flow<String> {
        return openRouterApiService.streamChatCompletion(
            OpenRouterChatRequest(
                model = model,
                stream = true,
                messages = messages
            )
        )
    }
}

