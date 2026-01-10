package com.goalguru.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.goalguru.data.GoalGuruDatabase
import com.goalguru.databinding.ActivityDashboardBinding
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var db: GoalGuruDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = GoalGuruDatabase.getDatabase(this)

        lifecycleScope.launch {
            val latestGoal = db.goalDao().getLatestGoal()
            if (latestGoal != null) {
                val total = db.taskDao().getTotalTaskCount(latestGoal.id)
                val completed = db.taskDao().getCompletedTaskCount(latestGoal.id)
                
                binding.tvTotalTasks.text = "Total: $total"
                binding.tvCompletedTasks.text = "Completed: $completed"
                val percentage = if (total > 0) (completed * 100) / total else 0
                binding.tvCompletionPercentage.text = "Progress: $percentage%"
                
                val prefs = db.preferencesDao().getPreferencesSync()
                if (prefs != null) {
                    binding.tvCurrentStreak.text = "Current Streak: ${prefs.currentStreak}"
                    binding.tvBestStreak.text = "Best Streak: ${prefs.bestStreak}"
                    
                    // Display Guru RPG Stats
                    // Note: Ensure these IDs exist in activity_dashboard.xml
                    binding.tvLevel.text = "Guru Level: ${prefs.level} (${prefs.guruTitle})"
                    binding.tvXp.text = "XP: ${prefs.xp}"
                    binding.tvGuruPoints.text = "Guru Points: ${prefs.guruPoints}"
                }
            } else {
                binding.tvTotalTasks.text = "Total: 0"
                binding.tvCompletedTasks.text = "Completed: 0"
                binding.tvCompletionPercentage.text = "Progress: 0%"
                binding.tvCurrentStreak.text = "Current Streak: 0"
                binding.tvBestStreak.text = "Best Streak: 0"
                binding.tvLevel.text = "Guru Level: 1 (Seeker)"
                binding.tvXp.text = "XP: 0"
                binding.tvGuruPoints.text = "Guru Points: 0"
            }
        }
    }
}
