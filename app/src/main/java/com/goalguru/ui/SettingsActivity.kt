package com.goalguru.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.goalguru.data.GoalGuruDatabase
import com.goalguru.databinding.ActivitySettingsBinding
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var db: GoalGuruDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = GoalGuruDatabase.getDatabase(this)

        lifecycleScope.launch {
            val prefs = db.preferencesDao().getPreferencesSync()
            if (prefs != null) {
                // Set UI values based on preferences
            }
        }

        binding.btnSaveSettings.setOnClickListener {
            lifecycleScope.launch {
                val prefs = db.preferencesDao().getPreferencesSync()
                if (prefs != null) {
                    val updated = prefs.copy(
                        roastLevel = binding.spinnerRoastLevel.selectedItem.toString(),
                        language = binding.spinnerLanguage.selectedItem.toString(),
                        gender = binding.spinnerGender.selectedItem.toString(),
                        theme = binding.spinnerTheme.selectedItem.toString(),
                        notificationTime = "${binding.timePickerNotification.hour}:${binding.timePickerNotification.minute}"
                    )
                    db.preferencesDao().update(updated)
                    finish()
                }
            }
        }
    }
}
