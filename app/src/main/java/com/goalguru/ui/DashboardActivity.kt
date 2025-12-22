package com.goalguru.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.goalguru.R
import com.goalguru.data.GoalGuruDatabase
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var db: GoalGuruDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        db = GoalGuruDatabase.getDatabase(this)

        lifecycleScope.launch {
            val prefs = db.preferencesDao().getPreferencesSync()
            if (prefs != null) {
                findViewById<TextView>(R.id.tv_total_tasks).text = "Total: ${prefs.totalTasks}"
                findViewById<TextView>(R.id.tv_completed_tasks).text = "Completed: ${prefs.completedTasks}"
                val percentage = if (prefs.totalTasks > 0) (prefs.completedTasks * 100) / prefs.totalTasks else 0
                findViewById<TextView>(R.id.tv_completion_percentage).text = "Progress: $percentage%"
                findViewById<TextView>(R.id.tv_current_streak).text = "Current Streak: ${prefs.currentStreak}"
                findViewById<TextView>(R.id.tv_best_streak).text = "Best Streak: ${prefs.bestStreak}"
            }
        }
    }
}
