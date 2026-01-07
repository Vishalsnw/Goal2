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
        
        // Try to parse the complete JSON first
        var daysList = mutableListOf<RoadmapDay>()
        var estimatedDays = 30
        
        try {
            val jsonObject = JsonParser.parseString(content).asJsonObject
            
            // Extract estimated days
            estimatedDays = try {
                jsonObject.get("estimatedDays")?.asInt ?: 30
            } catch (e: Exception) {
                30
            }
            
            // Extract days array
            val daysArray = try {
                jsonObject.getAsJsonArray("days")
            } catch (e: Exception) {
                null
            }
            
            // Parse each day from array
            if (daysArray != null && daysArray.size() > 0) {
                for (i in 0 until daysArray.size()) {
                    try {
                        val dayElement = daysArray.get(i)
                        val dayObj = dayElement.asJsonObject
                        val day = dayObj.get("day")?.asInt ?: daysList.size + 1
                        val title = dayObj.get("title")?.asString ?: "Task ${daysList.size + 1}"
                        val description = dayObj.get("description")?.asString ?: "Complete this step"
                        
                        val tips = try {
                            val tipsArray = dayObj.getAsJsonArray("tips")
                            if (tipsArray != null) {
                                val tipsList = mutableListOf<String>()
                                for (j in 0 until tipsArray.size()) {
                                    tipsList.add(tipsArray.get(j).asString)
                                }
                                tipsList
                            } else {
                                listOf("Keep going")
                            }
                        } catch (e: Exception) {
                            listOf("Keep going")
                        }
                        
                        daysList.add(RoadmapDay(day = day, title = title, description = description, tips = tips))
                    } catch (e: Exception) {
                        continue
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("AIService", "Full JSON parsing failed (likely truncated): ${e.message}")
            // JSON is likely truncated - try to extract days manually with regex
            daysList = extractDaysFromMalformedJson(content)
            
            if (daysList.isEmpty()) {
                android.util.Log.w("AIService", "Regex extraction failed, will use fallback")
            }
        }
        
        // If we got some days, return them
        if (daysList.isNotEmpty()) {
            val roadmap = Roadmap(estimatedDays = estimatedDays, days = daysList)
            android.util.Log.d("AIService", "SUCCESS: Generated roadmap with ${roadmap.days.size} days (parsed from ${if (daysList.size < estimatedDays) "truncated" else "complete"} response)")
            return roadmap
        }
        
        // If we still have no days, return fallback
        android.util.Log.w("AIService", "Could not parse any days from response, using fallback roadmap")
        return createFallbackRoadmap(goal, estimatedDays)
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
                """
                Generate a savage, humorous roast/insult in Hinglish (Hindi + English) for a $age year old who hasn't completed their task: '$taskTitle'. 
                The roast level is $userPreference. 
                Use street-style language like "Bhai tu lukkha hi marega" or "Abey saale". 
                Be extremely funny and insulting but keep it to the task. 
                Max 20 words. Use Devnagari for Hindi parts if appropriate, or just Roman script.
                """.trimIndent()
            else ->
                """
                Generate a savage, brutally honest, and funny roast for a $age year old who hasn't completed their task: '$taskTitle'. 
                The roast level is $userPreference. 
                Be creative, use modern slang, and don't be afraid to be mean in a hilarious way. 
                Make the user feel like a total slacker for missing this. 
                Max 20 words.
                """.trimIndent()
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

    private fun extractDaysFromMalformedJson(content: String): MutableList<RoadmapDay> {
        val daysList = mutableListOf<RoadmapDay>()
        
        // Regex to find day objects: {"day": N, "title": "...", "description": "...", ...}
        val dayPattern = """"day"\s*:\s*(\d+)\s*,\s*"title"\s*:\s*"([^"]*)"(?:\s*,\s*"description"\s*:\s*"([^"]*)")?""".toRegex()
        
        for (match in dayPattern.findAll(content)) {
            try {
                val day = match.groupValues[1].toInt()
                val title = match.groupValues[2]
                val description = match.groupValues.getOrNull(3) ?: "Complete this step"
                
                daysList.add(RoadmapDay(day = day, title = title, description = description, tips = listOf("Keep going")))
            } catch (e: Exception) {
                continue
            }
        }
        
        return daysList
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
