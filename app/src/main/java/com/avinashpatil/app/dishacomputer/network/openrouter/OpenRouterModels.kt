package com.avinashpatil.app.dishacomputer.network.openrouter

data class OpenRouterMessage(
    val role: String,
    val content: String
)

data class OpenRouterChatRequest(
    val model: String,
    val stream: Boolean,
    val messages: List<OpenRouterMessage>
)

