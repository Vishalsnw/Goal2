package com.goalguru.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.goalguru.R
import com.goalguru.data.GoalGuruDatabase
import com.goalguru.data.UserPreferences
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var db: GoalGuruDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = GoalGuruDatabase.getDatabase(this)

        lifecycleScope.launch {
            val prefs = db.preferencesDao().getPreferencesSync()
            if (prefs == null) {
                db.preferencesDao().insert(UserPreferences())
            }
        }

        findViewById<MaterialButton>(R.id.btn_new_goal).setOnClickListener {
            startActivity(Intent(this, GoalEntryActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btn_daily_task).setOnClickListener {
            startActivity(Intent(this, DailyTaskActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btn_dashboard).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btn_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
