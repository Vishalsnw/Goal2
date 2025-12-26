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

    suspend fun generateGoalRoadmap(goal: String, userProfile: String? = null): Roadmap {
        val personalityContext = if (userProfile != null) {
            "User Profile: $userProfile. Use this to provide culturally aware, friendly, and slightly sarcastic guidance. Use emojis. Be like an experienced human mentor, not a robot."
        } else ""

        val prompt = """
            $personalityContext
            Goal: "$goal"
            1. Estimate how many days (1-90) it takes to achieve this.
            2. Create a daily roadmap for that duration.
            Output ONLY raw JSON.
            {
                "estimatedDays": 30,
                "days": [
                    {
                        "day": 1,
                        "title": "Title",
                        "description": "Task",
                        "tips": ["Tip"]
                    }
                ]
            }
        """.trimIndent()

        val request = ChatCompletionRequest(
            messages = listOf(
                ChatMessage("user", prompt)
            )
        )

        try {
            val response = api.generateRoadmap(request, "Bearer $apiKey")
            var content = response.choices[0].message.content
            
            // Clean up JSON response with multiple strategies
            if (content.contains("```json")) {
                content = content.substringAfter("```json").substringBeforeLast("```").trim()
            } else if (content.contains("```")) {
                content = content.substringAfter("```").substringBeforeLast("```").trim()
            }
            
            // Remove any leading/trailing markdown or extra text
            content = content.trim()
            if (content.startsWith("json")) {
                content = content.substring(4).trim()
            }
            if (content.endsWith("json")) {
                content = content.substring(0, content.length - 4).trim()
            }
            
            // Extract JSON object if there's surrounding text
            val jsonStart = content.indexOf('{')
            val jsonEnd = content.lastIndexOf('}')
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                content = content.substring(jsonStart, jsonEnd + 1)
            }
            
            try {
                val jsonObject = JsonParser.parseString(content).asJsonObject
                return gson.fromJson(jsonObject, Roadmap::class.java)
            } catch (jsonError: Exception) {
                throw Exception("Failed to parse AI response as JSON. Response: ${content.take(300)}", jsonError)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun generateRoastMessage(
        userPreference: String,
        gender: String,
        age: Int,
        language: String,
        taskTitle: String
    ): String {
        val roastLevel = when {
            age > 40 -> "EXTRA_SPICY"
            age < 18 -> "MILD"
            else -> "SPICY"
        }
        val roastPrompt = when {
            roastLevel == "EXTRA_SPICY" && language == "HINDI" -> 
                "Generate an extra spicy Hindi roast message for a $age year old $gender about not completing the task: '$taskTitle'. Be culturally aware and witty."
            roastLevel == "SPICY" && language == "HINDI" ->
                "Generate a spicy Hindi motivational roast for a $age year old $gender about the incomplete task: '$taskTitle'."
            roastLevel == "MILD" && language == "HINDI" ->
                "Generate a mild Hindi reminder for a young user about the task: '$taskTitle'."
            roastLevel == "EXTRA_SPICY" ->
                "Generate an extra spicy English roast message for a $age year old $gender procrastinating on: '$taskTitle'. Use cultural context."
            roastLevel == "SPICY" ->
                "Generate a spicy English motivational message for a $age year old $gender about: '$taskTitle'."
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
        return Roadmap(
            estimatedDays = days,
            days = dayList
        )
    }
}
