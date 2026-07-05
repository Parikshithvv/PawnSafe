package com.pawnsafe.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

data class GeminiRequest(
    val contents: List<GeminiContent>
)
data class GeminiContent(
    val parts: List<GeminiPart>
)
data class GeminiPart(
    val text: String
)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)
data class GeminiCandidate(
    val content: GeminiContent?
)

interface GeminiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generate(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private val retrofit = retrofit2.Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()

    val service: GeminiService = retrofit.create(GeminiService::class.java)
}