package com.habitflow.app.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.habitflow.app.data.db.AppDatabase
import com.habitflow.app.data.db.dao.HabitDao
import com.habitflow.app.data.db.dao.HabitEntryDao
import com.habitflow.app.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "habitflow_db"
        )
            .addMigrations(*AppDatabase.ALL_MIGRATIONS)
            .build()
        // NOTE: No fallbackToDestructiveMigration().
        // If a migration is missing, the app crashes at dev time
        // instead of silently deleting user data in production.
    }

    @Provides
    fun provideHabitDao(db: AppDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideHabitEntryDao(db: AppDatabase): HabitEntryDao = db.habitEntryDao()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    }
}
