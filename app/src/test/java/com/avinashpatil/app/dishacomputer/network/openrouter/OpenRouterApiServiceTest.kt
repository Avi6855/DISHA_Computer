package com.avinashpatil.app.dishacomputer.network.openrouter

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * DISHA AI streams answers token by token. The parser has to cope with SSE deltas, a non-streaming
 * JSON body, provider errors in the middle of a stream and plain HTTP failures — each one is
 * exercised against a local server so no real API key is needed.
 */
class OpenRouterApiServiceTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun service(apiKey: String = "test-key") = OpenRouterApiService(
        okHttpClient = OkHttpClient(),
        apiKey = apiKey,
        chatCompletionsUrl = server.url("/chat/completions").toString()
    )

    private fun request() = OpenRouterChatRequest(
        model = "openrouter/auto",
        stream = true,
        messages = listOf(OpenRouterMessage(role = "user", content = "hello"))
    )

    private fun sseResponse(body: String) = MockResponse()
        .setHeader("Content-Type", "text/event-stream")
        .setBody(body)

    @Test
    fun `streamed deltas are emitted in order and stop at the done marker`() {
        server.enqueue(
            sseResponse(
                """
                data: {"choices":[{"delta":{"content":"Hello"}}]}

                data: {"choices":[{"delta":{"content":" from DISHA"}}]}

                data: [DONE]

                """.trimIndent()
            )
        )

        val chunks = runBlocking { service().streamChatCompletion(request()).toList() }

        assertEquals(listOf("Hello", " from DISHA"), chunks)
    }

    @Test
    fun `deltas without content are skipped`() {
        server.enqueue(
            sseResponse(
                """
                data: {"choices":[{"delta":{}}]}
                data: {"choices":[{"delta":{"content":"Only this"}}]}
                data: [DONE]
                """.trimIndent()
            )
        )

        val chunks = runBlocking { service().streamChatCompletion(request()).toList() }

        assertEquals(listOf("Only this"), chunks)
    }

    @Test
    fun `a non streaming json answer is delivered as a single chunk`() {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""{"choices":[{"message":{"content":"Complete answer"}}]}""")
        )

        val chunks = runBlocking { service().streamChatCompletion(request()).toList() }

        assertEquals(listOf("Complete answer"), chunks)
    }

    @Test
    fun `provider error inside the stream fails the collector`() {
        server.enqueue(
            sseResponse(
                """
                data: {"error":{"message":"Rate limit exceeded"}}
                """.trimIndent()
            )
        )

        val failure = assertThrows(IllegalStateException::class.java) {
            runBlocking { service().streamChatCompletion(request()).toList() }
        }

        assertEquals("Rate limit exceeded", failure.message)
    }

    @Test
    fun `http error carries the provider message`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"error":{"message":"Too many requests"}}""")
        )

        val failure = assertThrows(IllegalStateException::class.java) {
            runBlocking { service().streamChatCompletion(request()).toList() }
        }

        assertEquals("Too many requests", failure.message)
    }

    @Test
    fun `http error without a body still reports the status`() {
        server.enqueue(MockResponse().setResponseCode(502))

        val failure = assertThrows(IllegalStateException::class.java) {
            runBlocking { service().streamChatCompletion(request()).toList() }
        }

        assertEquals("OpenRouter request failed (502)", failure.message)
    }

    @Test
    fun `a missing api key fails fast instead of calling the api`() {
        val failure = assertThrows(IllegalStateException::class.java) {
            runBlocking { service(apiKey = "").streamChatCompletion(request()).toList() }
        }

        assertEquals("Missing OPENROUTER_API_KEY", failure.message)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `an answer without choices fails instead of emitting nothing`() {
        val emptyBody = """{"choices":[]}"""
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(emptyBody)
        )

        val failure = assertThrows(IllegalStateException::class.java) {
            runBlocking { service().streamChatCompletion(request()).toList() }
        }

        assertEquals(emptyBody, failure.message)
    }

    @Test
    fun `the request is addressed to the chat completions endpoint with bearer auth`() {
        server.enqueue(sseResponse("data: [DONE]\n\n"))

        runBlocking { service().streamChatCompletion(request()).toList() }

        val recorded = server.takeRequest()
        assertEquals("/chat/completions", recorded.path)
        assertEquals("Bearer test-key", recorded.getHeader("Authorization"))
        assertTrue(recorded.getHeader("Content-Type").orEmpty().startsWith("application/json"))
        assertEquals("text/event-stream", recorded.getHeader("Accept"))
    }
}
