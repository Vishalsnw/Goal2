package com.goalguru.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.goalguru.data.GoalGuruDatabase
import com.goalguru.databinding.ActivitySplashBinding
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = GoalGuruDatabase.getDatabase(this)

        Handler(Looper.getMainLooper()).postDelayed({
            lifecycleScope.launch {
                val prefs = db.preferencesDao().getPreferencesSync()
                if (prefs == null || !prefs.isOnboardingCompleted) {
                    startActivity(Intent(this@SplashActivity, OnboardingActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                }
                finish()
            }
        }, 2000)
    }
}
