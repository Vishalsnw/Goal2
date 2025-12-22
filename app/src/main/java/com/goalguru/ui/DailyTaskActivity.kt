package com.goalguru.ui

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.goalguru.R
import com.goalguru.data.GoalGuruDatabase
import kotlinx.coroutines.launch

class DailyTaskActivity : AppCompatActivity() {
    private lateinit var db: GoalGuruDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_task)

        db = GoalGuruDatabase.getDatabase(this)

        lifecycleScope.launch {
            val task = db.taskDao().getNextIncompleteTask()
            if (task != null) {
                findViewById<TextView>(R.id.tv_task_title).text = task.title
                findViewById<TextView>(R.id.tv_task_description).text = task.description

                findViewById<MaterialButton>(R.id.btn_complete).setOnClickListener {
                    lifecycleScope.launch {
                        val updatedTask = task.copy(isCompleted = true, completedAt = System.currentTimeMillis())
                        db.taskDao().update(updatedTask)
                        Toast.makeText(this@DailyTaskActivity, "Task completed!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                findViewById<MaterialButton>(R.id.btn_skip).setOnClickListener {
                    lifecycleScope.launch {
                        val updatedTask = task.copy(isSkipped = true)
                        db.taskDao().update(updatedTask)
                        finish()
                    }
                }
            } else {
                findViewById<TextView>(R.id.tv_task_title).text = "No tasks today!"
                Toast.makeText(this@DailyTaskActivity, "All tasks completed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
