package com.avinashpatil.app.dishacomputer.di

import android.content.Context
import androidx.room.Room
import com.avinashpatil.app.dishacomputer.data.AppDatabase
import com.avinashpatil.app.dishacomputer.data.dao.CourseDao
import com.avinashpatil.app.dishacomputer.data.dao.QuizDao
import com.avinashpatil.app.dishacomputer.data.dao.TopicsDao
import com.avinashpatil.app.dishacomputer.data.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.avinashpatil.app.dishacomputer.data.dao.ExamAnswerDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "disha_computer_database"
        ).fallbackToDestructiveMigration()
         .build()
    }


    @Provides
    @Singleton
    fun provideTopicsDao(database: AppDatabase): TopicsDao {
        return database.topicsDao()
    }

    @Provides
    @Singleton
    fun provideCourseDao(database: AppDatabase): CourseDao {
        return database.courseDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideQuizDao(database: AppDatabase): QuizDao {
        return database.quizDao()
    }

    @Provides
    @Singleton
    fun provideExamAnswerDao(database: AppDatabase): ExamAnswerDao {
        return database.examAnswerDao()
    }
}
