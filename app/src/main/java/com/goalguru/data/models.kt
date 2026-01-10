package com.goalguru.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val roadmap: String, // JSON string containing the roadmap
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val title: String,
    val description: String,
    val dayNumber: Int,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val isSkipped: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val age: Int = 0,
    val gender: String = "OTHER", // MALE, FEMALE, OTHER
    val country: String = "",
    val language: String = "ENGLISH", // ENGLISH, HINDI
    val roastLevel: String = "SPICY", // MILD, SPICY, EXTRA_SPICY
    val notificationTime: String = "09:00", // HH:mm format
    val theme: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val isOnboardingCompleted: Boolean = false,
    val xp: Int = 0,
    val level: Int = 1,
    val guruTitle: String = "Seeker",
    val guruPoints: Int = 0
)

data class Roadmap(
    val estimatedDays: Int,
    val days: List<RoadmapDay>
)

data class RoadmapDay(
    val day: Int,
    val title: String,
    val description: String,
    val tips: List<String>
)
