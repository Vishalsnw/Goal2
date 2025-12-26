package com.goalguru.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.goalguru.data.GoalGuruDatabase
import com.goalguru.data.UserPreferences
import com.goalguru.Config
import com.goalguru.databinding.ActivityDailyTaskBinding
import com.goalguru.service.AIService
import com.goalguru.service.TaskReminderService
import kotlinx.coroutines.launch

class DailyTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDailyTaskBinding
    private lateinit var db: GoalGuruDatabase
    private lateinit var aiService: AIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = GoalGuruDatabase.getDatabase(this)
        aiService = AIService(Config.DEEPSEEK_API_KEY)

        lifecycleScope.launch {
            val task = db.taskDao().getNextIncompleteTask()
            if (task != null) {
                binding.tvTaskTitle.text = task.title
                binding.tvTaskDescription.text = task.description
                
                roastUser(task.title)

                binding.btnComplete.setOnClickListener {
                    lifecycleScope.launch {
                        val updatedTask = task.copy(isCompleted = true, completedAt = System.currentTimeMillis())
                        db.taskDao().update(updatedTask)
                        updatePreferencesAfterTask()
                        Toast.makeText(this@DailyTaskActivity, "Task completed!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                binding.btnSkip.setOnClickListener {
                    lifecycleScope.launch {
                        val updatedTask = task.copy(isSkipped = true)
                        db.taskDao().update(updatedTask)
                        finish()
                    }
                }
            } else {
                binding.tvTaskTitle.text = "No tasks today!"
                binding.tvTaskDescription.text = "Click 'New Goal' to start a journey."
                binding.btnComplete.isEnabled = false
                binding.btnSkip.isEnabled = false
            }
        }
    }

    private fun updatePreferencesAfterTask() {
        lifecycleScope.launch {
            val latestGoal = db.goalDao().getLatestGoal() ?: return@launch
            val prefs = db.preferencesDao().getPreferencesSync() ?: UserPreferences()
            
            val total = db.taskDao().getTotalTaskCount(latestGoal.id)
            val completed = db.taskDao().getCompletedTaskCount(latestGoal.id)
            
            val updatedPrefs = prefs.copy(
                totalTasks = total,
                completedTasks = completed,
                currentStreak = if (completed > 0) prefs.currentStreak + 1 else prefs.currentStreak
            )
            db.preferencesDao().update(updatedPrefs)
        }
    }

    private fun roastUser(taskTitle: String) {
        lifecycleScope.launch {
            val prefs = db.preferencesDao().getPreferencesSync()
            if (prefs != null) {
                val roast = aiService.generateRoastMessage(
                    userPreference = "Name: ${prefs.name}, Country: ${prefs.country}",
                    gender = prefs.gender,
                    age = prefs.age,
                    language = prefs.language,
                    taskTitle = taskTitle
                )
                binding.tvRoast.text = roast
                binding.tvRoast.visibility = android.view.View.VISIBLE
                
                // Send notification to notification bar
                TaskReminderService.sendRoastNotification(
                    this@DailyTaskActivity,
                    "Time for your task!",
                    roast
                )
            }
        }
    }
}