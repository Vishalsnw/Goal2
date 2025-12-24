package com.goalguru.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterAPI {
    @POST("chat/completions")
    suspend fun generateRoadmap(
        @Body request: ChatCompletionRequest,
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String = "https://goalguru.app",
        @Header("X-Title") title: String = "GoalGuru"
    ): ChatCompletionResponse
}

data class ChatCompletionRequest(
    val model: String = "deepseek-chat",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 2000
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatCompletionResponse(
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val message: ChatMessage,
    val finish_reason: String
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

object OpenRouterClient {
    private const val BASE_URL = "https://api.deepseek.com/chat/completions"

    fun create(apiKey: String): OpenRouterAPI {
        val client = OkHttpClient()
        return Retrofit.Builder()
            .baseUrl("https://api.deepseek.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(OpenRouterAPI::class.java)
    }
}
