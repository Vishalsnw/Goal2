package com.goalguru.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.goalguru.data.GoalGuruDatabase
import com.goalguru.data.UserPreferences
import com.goalguru.databinding.ActivityOnboardingBinding
import kotlinx.coroutines.launch

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var db: GoalGuruDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = GoalGuruDatabase.getDatabase(this)

        binding.btnFinishOnboarding.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val ageStr = binding.etAge.text.toString().trim()
            val country = binding.etCountry.text.toString().trim()
            val language = if (binding.rgLanguage.checkedRadioButtonId == binding.rbHindi.id) "HINDI" else "ENGLISH"
            
            if (name.isEmpty() || ageStr.isEmpty() || country.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val age = ageStr.toIntOrNull() ?: 0
            val gender = when (binding.rgGender.checkedRadioButtonId) {
                binding.rbMale.id -> "MALE"
                binding.rbFemale.id -> "FEMALE"
                else -> "OTHER"
            }

            lifecycleScope.launch {
                val prefs = UserPreferences(
                    name = name,
                    age = age,
                    gender = gender,
                    country = country,
                    language = language,
                    isOnboardingCompleted = true
                )
                db.preferencesDao().insert(prefs)
                
                startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}