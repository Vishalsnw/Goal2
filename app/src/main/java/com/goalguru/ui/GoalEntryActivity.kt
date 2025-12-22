package com.goalguru.ui

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.goalguru.R
import com.goalguru.data.Goal
import com.goalguru.data.GoalGuruDatabase
import com.goalguru.data.Task
import com.goalguru.service.AIService
import com.google.gson.Gson
import kotlinx.coroutines.launch

class GoalEntryActivity : AppCompatActivity() {
    private lateinit var db: GoalGuruDatabase
    private lateinit var aiService: AIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_entry)

        db = GoalGuruDatabase.getDatabase(this)
        aiService = AIService("dummy-key")

        val goalInput = findViewById<EditText>(R.id.et_goal)
        val submitButton = findViewById<MaterialButton>(R.id.btn_submit_goal)

        submitButton.setOnClickListener {
            val goalText = goalInput.text.toString().trim()
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
        try {
            val roadmap = aiService.generateGoalRoadmap(goalText, 30)
            val roadmapJson = Gson().toJson(roadmap)

            val goal = Goal(title = goalText, description = goalText, roadmap = roadmapJson)
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
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
