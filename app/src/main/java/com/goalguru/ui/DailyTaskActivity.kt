package com.goalguru.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.goalguru.data.GoalGuruDatabase
import com.goalguru.databinding.ActivityDailyTaskBinding
import kotlinx.coroutines.launch

class DailyTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDailyTaskBinding
    private lateinit var db: GoalGuruDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = GoalGuruDatabase.getDatabase(this)

        lifecycleScope.launch {
            val task = db.taskDao().getNextIncompleteTask()
            if (task != null) {
                binding.tvTaskTitle.text = task.title
                binding.tvTaskDescription.text = task.description

                binding.btnComplete.setOnClickListener {
                    lifecycleScope.launch {
                        val updatedTask = task.copy(isCompleted = true, completedAt = System.currentTimeMillis())
                        db.taskDao().update(updatedTask)
                        Toast.makeText(this@DailyTaskActivity, "Task completed!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                binding.btnSkip.setOnClickListener {
                    lifecycleScope.launch {
                        val updatedTask = task.copy(isSkipped = true)
                        db.taskDao().update(updatedTask)
                        finish()
                    }
                }
            } else {
                binding.tvTaskTitle.text = "No tasks today!"
                Toast.makeText(this@DailyTaskActivity, "All tasks completed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
