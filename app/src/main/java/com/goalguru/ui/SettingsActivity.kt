package com.goalguru.ui

import android.os.Bundle
import android.widget.Spinner
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.goalguru.R
import com.goalguru.data.GoalGuruDatabase
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var db: GoalGuruDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        db = GoalGuruDatabase.getDatabase(this)

        val roastLevelSpinner = findViewById<Spinner>(R.id.spinner_roast_level)
        val languageSpinner = findViewById<Spinner>(R.id.spinner_language)
        val genderSpinner = findViewById<Spinner>(R.id.spinner_gender)
        val themeSpinner = findViewById<Spinner>(R.id.spinner_theme)
        val notificationTime = findViewById<TimePicker>(R.id.time_picker_notification)
        val saveButton = findViewById<MaterialButton>(R.id.btn_save_settings)

        lifecycleScope.launch {
            val prefs = db.preferencesDao().getPreferencesSync()
            if (prefs != null) {
                // Set UI values based on preferences
            }
        }

        saveButton.setOnClickListener {
            lifecycleScope.launch {
                val prefs = db.preferencesDao().getPreferencesSync()
                if (prefs != null) {
                    val updated = prefs.copy(
                        roastLevel = roastLevelSpinner.selectedItem.toString(),
                        language = languageSpinner.selectedItem.toString(),
                        gender = genderSpinner.selectedItem.toString(),
                        theme = themeSpinner.selectedItem.toString(),
                        notificationTime = "${notificationTime.hour}:${notificationTime.minute}"
                    )
                    db.preferencesDao().update(updated)
                    finish()
                }
            }
        }
    }
}
