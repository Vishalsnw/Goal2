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
            
            IMPORTANT: Respond with ONLY a valid JSON object, no markdown, no extra text.
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
            
            1. Estimate how many days (1-90) it takes to achieve this goal.
            2. Create a daily roadmap for that duration with realistic tasks.
            3. Response MUST be valid JSON only.
        """.trimIndent()

        val request = ChatCompletionRequest(
            messages = listOf(
                ChatMessage("user", prompt)
            )
        )

        try {
            val response = api.generateRoadmap(request, "Bearer $apiKey")
            var content = response.choices[0].message.content.trim()
            
            // Remove markdown code blocks
            content = when {
                content.contains("```json") -> content.substringAfter("```json").substringBeforeLast("```").trim()
                content.contains("```") -> content.substringAfter("```").substringBeforeLast("```").trim()
                else -> content
            }
            
            // Remove "json" text if present
            if (content.startsWith("json")) content = content.substring(4).trim()
            if (content.endsWith("json")) content = content.substring(0, content.length - 4).trim()
            
            // Extract JSON object
            val jsonStart = content.indexOf('{')
            val jsonEnd = content.lastIndexOf('}')
            if (jsonStart < 0 || jsonEnd <= jsonStart) {
                throw Exception("No valid JSON found in response: ${content.take(200)}")
            }
            content = content.substring(jsonStart, jsonEnd + 1).trim()
            
            try {
                val jsonObject = JsonParser.parseString(content).asJsonObject
                val roadmap = gson.fromJson(jsonObject, Roadmap::class.java)
                
                // Validate roadmap
                if (roadmap.days.isEmpty()) {
                    return createFallbackRoadmap(goal, roadmap.estimatedDays.coerceIn(1, 90))
                }
                return roadmap
            } catch (jsonError: Exception) {
                // Fallback to generic roadmap
                val estimatedDays = extractEstimatedDays(content)
                return createFallbackRoadmap(goal, estimatedDays)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Final fallback
            return createFallbackRoadmap(goal, 30)
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

    private fun extractEstimatedDays(content: String): Int {
        return try {
            val regex = """"estimatedDays"\s*:\s*(\d+)""".toRegex()
            val match = regex.find(content)
            match?.groupValues?.get(1)?.toIntOrNull()?.coerceIn(1, 90) ?: 30
        } catch (e: Exception) {
            30
        }
    }

    private fun createFallbackRoadmap(goal: String, days: Int): Roadmap {
        val dayList = mutableListOf<RoadmapDay>()
        repeat(days.coerceIn(1, 90)) { i ->
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
            estimatedDays = days.coerceIn(1, 90),
            days = dayList
        )
    }
}
