package com.goalguru.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Goal::class, Task::class, UserPreferences::class], version = 1)
abstract class GoalGuruDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun taskDao(): TaskDao
    abstract fun preferencesDao(): PreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: GoalGuruDatabase? = null

        fun getDatabase(context: Context): GoalGuruDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GoalGuruDatabase::class.java,
                    "goalguru_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
