package com.goalguru.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert
    suspend fun insert(goal: Goal): Long

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)

    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoal(goalId: Int): Goal?

    @Query("SELECT * FROM goals WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestGoal(): Goal?
}

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM tasks WHERE goalId = :goalId ORDER BY dayNumber ASC")
    suspend fun getTasksByGoal(goalId: Int): List<Task>

    @Query("SELECT * FROM tasks WHERE goalId = :goalId AND dayNumber = :dayNumber LIMIT 1")
    suspend fun getTaskForDay(goalId: Int, dayNumber: Int): Task?

    @Query("SELECT COUNT(*) FROM tasks WHERE goalId = :goalId AND isCompleted = 1")
    suspend fun getCompletedTaskCount(goalId: Int): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE goalId = :goalId")
    suspend fun getTotalTaskCount(goalId: Int): Int

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY createdAt LIMIT 1")
    suspend fun getNextIncompleteTask(): Task?
}

@Dao
interface PreferencesDao {
    @Insert
    suspend fun insert(preferences: UserPreferences)

    @Update
    suspend fun update(preferences: UserPreferences)

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getPreferences(): Flow<UserPreferences?>

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getPreferencesSync(): UserPreferences?
}
