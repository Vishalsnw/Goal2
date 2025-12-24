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
            val prefs = db.preferencesDao().getPreferencesSync()
            if (prefs != null) {
                binding.tvTotalTasks.text = "Total: ${prefs.totalTasks}"
                binding.tvCompletedTasks.text = "Completed: ${prefs.completedTasks}"
                val percentage = if (prefs.totalTasks > 0) (prefs.completedTasks * 100) / prefs.totalTasks else 0
                binding.tvCompletionPercentage.text = "Progress: $percentage%"
                binding.tvCurrentStreak.text = "Current Streak: ${prefs.currentStreak}"
                binding.tvBestStreak.text = "Best Streak: ${prefs.bestStreak}"
            }
        }
    }
}
