package com.goalguru.service

import com.goalguru.api.ChatCompletionRequest
import com.goalguru.api.ChatMessage
import com.goalguru.api.OpenRouterClient
import com.goalguru.data.Roadmap
import com.goalguru.data.RoadmapDay
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import java.io.StringReader

class AIService(private val apiKey: String) {
    private val api = OpenRouterClient.create(apiKey)
    private val gson = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .create()

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

        val response = api.generateRoadmap(request, "Bearer $apiKey")
        var content = response.choices[0].message.content.trim()
        
        android.util.Log.d("AIService", "Raw API Response: $content")
        
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
            android.util.Log.e("AIService", "ERROR: No valid JSON found in response. Content: ${content.take(500)}")
            throw Exception("ERROR: No valid JSON found in response: ${content.take(500)}")
        }
        content = content.substring(jsonStart, jsonEnd + 1).trim()
        
        android.util.Log.d("AIService", "Cleaned JSON: $content")
        
        try {
            val jsonObject = JsonParser.parseString(content).asJsonObject
            
            // Extract estimated days
            val estimatedDays = try {
                jsonObject.get("estimatedDays")?.asInt ?: 30
            } catch (e: Exception) {
                android.util.Log.w("AIService", "Could not parse estimatedDays, using default 30")
                30
            }
            
            // Extract days array
            val daysArray = try {
                jsonObject.getAsJsonArray("days") ?: return createFallbackRoadmap(goal, estimatedDays)
            } catch (e: Exception) {
                android.util.Log.w("AIService", "No 'days' array found in JSON: ${e.message}")
                return createFallbackRoadmap(goal, estimatedDays)
            }
            
            // Parse each day
            val daysList = mutableListOf<RoadmapDay>()
            for (dayElement in daysArray) {
                try {
                    val dayObj = dayElement.asJsonObject
                    val day = dayObj.get("day")?.asInt ?: daysList.size + 1
                    val title = dayObj.get("title")?.asString ?: "Task ${daysList.size + 1}"
                    val description = dayObj.get("description")?.asString ?: "Complete this step"
                    
                    val tips = try {
                        dayObj.getAsJsonArray("tips")?.map { it.asString } ?: listOf("Keep going")
                    } catch (e: Exception) {
                        listOf("Keep going")
                    }
                    
                    daysList.add(RoadmapDay(day = day, title = title, description = description, tips = tips))
                } catch (e: Exception) {
                    android.util.Log.w("AIService", "Could not parse day element: $dayElement\nError: ${e.message}")
                    continue
                }
            }
            
            if (daysList.isEmpty()) {
                android.util.Log.e("AIService", "ERROR: No days parsed from array with ${daysArray.size()} elements")
                throw Exception("ERROR: API returned 0 parseable days from $daysArray")
            }
            
            val roadmap = Roadmap(estimatedDays = estimatedDays, days = daysList)
            android.util.Log.d("AIService", "SUCCESS: Generated roadmap with ${roadmap.days.size} days")
            return roadmap
        } catch (jsonError: Exception) {
            android.util.Log.e("AIService", "ERROR: JSON parsing failed: ${jsonError.message}\nContent: $content", jsonError)
            throw Exception("ERROR: Failed to parse roadmap JSON:\n${jsonError.message}\n\nRaw response:\n${content.take(300)}")
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
