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
            "User Profile: $userProfile. Be culturally aware and provide friendly, expert guidance."
        } else ""

        val prompt = """
            $personalityContext
            Goal: "$goal"
            
            Create a step-by-step daily roadmap to achieve this goal.
            Respond ONLY with this exact JSON structure (no markdown, no extra text):
            
            {
                "estimatedDays": 30,
                "days": [
                    {"day": 1, "title": "Task Title", "description": "Specific action to take", "tips": ["Tip 1", "Tip 2"]},
                    {"day": 2, "title": "Task Title", "description": "Specific action to take", "tips": ["Tip 1", "Tip 2"]}
                ]
            }
            
            Requirements:
            1. Estimate days needed: 5-90 days
            2. Create realistic, actionable daily tasks
            3. Each day must have a specific title and description related to achieving the goal
            4. Include 1-2 practical tips per day
            5. Return ONLY valid JSON, nothing else
            6. Do NOT use markdown code blocks
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
        val roastPrompt = when {
            language == "HINDI" -> 
                "Generate a motivational message in Hindi for a $age year old to complete this specific task: '$taskTitle'. Make it encouraging and specific to the task. Max 100 words."
            else ->
                "Generate a short motivational message for a $age year old to complete this specific task: '$taskTitle'. Be specific about why this task matters. Max 100 words."
        }

        val request = ChatCompletionRequest(
            messages = listOf(
                ChatMessage("user", roastPrompt)
            ),
            max_tokens = 150
        )

        return try {
            val response = api.generateRoadmap(request, "Bearer $apiKey")
            val message = response.choices[0].message.content.trim()
            if (message.isEmpty()) "Time to complete: $taskTitle" else message
        } catch (e: Exception) {
            "Time to complete: $taskTitle"
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
