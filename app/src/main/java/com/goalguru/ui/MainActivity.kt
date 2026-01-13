package com.goalguru.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.goalguru.data.GoalGuruDatabase
import com.goalguru.data.UserPreferences
import com.goalguru.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

import android.view.animation.AnimationUtils
import com.goalguru.R

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: GoalGuruDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = GoalGuruDatabase.getDatabase(this)

        // Apply animations
        val fadeSlideUp = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up)
        binding.heroCard.startAnimation(fadeSlideUp)
        
        val popIn = AnimationUtils.loadAnimation(this, R.anim.pop_in)
        binding.btnNewGoal.startAnimation(popIn)
        binding.btnDailyTask.startAnimation(popIn)
        binding.btnDashboard.startAnimation(popIn)
        binding.btnSettings.startAnimation(popIn)

        requestNotificationPermission()

        binding.btnNewGoal.setOnClickListener {
            startActivity(Intent(this, GoalEntryActivity::class.java))
        }

        binding.btnDailyTask.setOnClickListener {
            startActivity(Intent(this, DailyTaskActivity::class.java))
        }

        binding.btnDashboard.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }
}
