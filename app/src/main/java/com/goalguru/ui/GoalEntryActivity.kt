package com.goalguru.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.goalguru.Config
import com.goalguru.data.Goal
import com.goalguru.data.GoalGuruDatabase
import com.goalguru.data.Task
import com.goalguru.databinding.ActivityGoalEntryBinding
import com.goalguru.service.AIService
import com.google.gson.Gson
import kotlinx.coroutines.launch

class GoalEntryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGoalEntryBinding
    private lateinit var db: GoalGuruDatabase
    private lateinit var aiService: AIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = GoalGuruDatabase.getDatabase(this)
        aiService = AIService(Config.DEEPSEEK_API_KEY)

        binding.btnSubmitGoal.setOnClickListener {
            val goalText = binding.etGoal.text.toString().trim()
            if (goalText.isEmpty()) {
                Toast.makeText(this, "Please enter a goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                generateRoadmapAndSaveGoal(goalText)
            }
        }
    }

    private suspend fun generateRoadmapAndSaveGoal(goalText: String) {
        setLoading(true)
        try {
            val prefs = db.preferencesDao().getPreferencesSync()
            val userProfile = prefs?.let { 
                "Name: ${it.name}, Age: ${it.age}, Gender: ${it.gender}, Country: ${it.country}, Language: ${it.language}" 
            }
            
            val roadmap = aiService.generateGoalRoadmap(goalText, userProfile)
            val roadmapJson = Gson().toJson(roadmap)

            val goal = Goal(
                title = goalText, 
                description = "Estimated completion: ${roadmap.estimatedDays} days", 
                roadmap = roadmapJson
            )
            val goalId = db.goalDao().insert(goal).toInt()

            roadmap.days.forEach { day ->
                val task = Task(
                    goalId = goalId,
                    title = day.title,
                    description = day.description,
                    dayNumber = day.day
                )
                db.taskDao().insert(task)
            }

            Toast.makeText(this, "Goal created successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            setLoading(false)
            e.printStackTrace()
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true -> "Connection timed out. Please try again."
                e.message?.contains("401") == true -> "Invalid API Key. Please check your configuration."
                else -> "Failed to generate roadmap: ${e.message}"
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingLayout.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnSubmitGoal.isEnabled = !isLoading
        binding.etGoal.isEnabled = !isLoading
    }
}
