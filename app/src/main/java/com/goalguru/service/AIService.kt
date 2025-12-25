package com.goalguru.service

import com.goalguru.api.ChatCompletionRequest
import com.goalguru.api.ChatMessage
import com.goalguru.api.OpenRouterClient
import com.goalguru.data.Roadmap
import com.goalguru.data.RoadmapDay
import com.google.gson.Gson
import com.google.gson.JsonParser

class AIService(private val apiKey: String) {
    private val api = OpenRouterClient.create(apiKey)
    private val gson = Gson()

    suspend fun generateGoalRoadmap(goal: String, duration: Int = 30): Roadmap {
        val prompt = """
            Generate a structured $duration-day roadmap for the goal: "$goal"
            
            Return ONLY valid JSON (no markdown, no extra text) in this format:
            {
                "days": [
                    {
                        "day": 1,
                        "title": "Day title",
                        "description": "What to do today",
                        "tips": ["tip1", "tip2"]
                    }
                ]
            }
            
            Make it practical, achievable, and motivating.
        """.trimIndent()

        val request = ChatCompletionRequest(
            messages = listOf(
                ChatMessage("user", prompt)
            )
        )

        try {
            val response = api.generateRoadmap(request, "Bearer $apiKey")
            var content = response.choices[0].message.content
            
            // Clean up JSON response if AI included markdown blocks
            if (content.contains("```json")) {
                content = content.substringAfter("```json").substringBeforeLast("```")
            } else if (content.contains("```")) {
                content = content.substringAfter("```").substringBeforeLast("```")
            }
            content = content.trim()
            
            val jsonObject = JsonParser.parseString(content).asJsonObject
            return gson.fromJson(jsonObject, Roadmap::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Rethrow to let the UI handle the error instead of falling back
        }
    }

    suspend fun generateRoastMessage(
        userPreference: String,
        roastLevel: String,
        language: String,
        taskTitle: String
    ): String {
        val roastPrompt = when {
            roastLevel == "EXTRA_SPICY" && language == "HINDI" -> 
                "Generate an extra spicy Hindi roast message about not completing the task: '$taskTitle'. Be funny and motivating."
            roastLevel == "SPICY" && language == "HINDI" ->
                "Generate a spicy Hindi motivational roast about the incomplete task: '$taskTitle'."
            roastLevel == "MILD" && language == "HINDI" ->
                "Generate a mild Hindi reminder to complete the task: '$taskTitle'."
            roastLevel == "EXTRA_SPICY" ->
                "Generate an extra spicy English roast message about procrastinating on: '$taskTitle'. Be funny!"
            roastLevel == "SPICY" ->
                "Generate a spicy English motivational message about: '$taskTitle'."
            else ->
                "Remind the user to complete: '$taskTitle'."
        }

        val request = ChatCompletionRequest(
            messages = listOf(
                ChatMessage("user", roastPrompt)
            ),
            max_tokens = 200
        )

        return try {
            val response = api.generateRoadmap(request, "Bearer $apiKey")
            response.choices[0].message.content
        } catch (e: Exception) {
            "Hey! Don't forget to complete: $taskTitle"
        }
    }

    private fun createFallbackRoadmap(goal: String, days: Int): Roadmap {
        val dayList = mutableListOf<RoadmapDay>()
        repeat(days) { i ->
            dayList.add(
                RoadmapDay(
                    day = i + 1,
                    title = "Day ${i + 1}: Progress Towards Your Goal",
                    description = "Focus on completing one small step towards: $goal",
                    tips = listOf("Stay consistent", "Don't give up", "Celebrate small wins")
                )
            )
        }
        return Roadmap(dayList)
    }
}
