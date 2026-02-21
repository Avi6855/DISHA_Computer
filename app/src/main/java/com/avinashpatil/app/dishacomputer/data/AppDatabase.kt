package com.avinashpatil.app.dishacomputer.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.avinashpatil.app.dishacomputer.data.converters.Converters
import com.avinashpatil.app.dishacomputer.data.dao.CourseDao
import com.avinashpatil.app.dishacomputer.data.dao.QuizDao
import com.avinashpatil.app.dishacomputer.data.dao.TopicsDao
import com.avinashpatil.app.dishacomputer.data.dao.UserDao
import com.avinashpatil.app.dishacomputer.data.model.CourseEntity
import com.avinashpatil.app.dishacomputer.data.model.UserEntity
import com.avinashpatil.app.dishacomputer.data.model.QuizEntity
import com.avinashpatil.app.dishacomputer.data.model.TopicsEntity
import com.avinashpatil.app.dishacomputer.data.dao.ExamAnswerDao
import com.avinashpatil.app.dishacomputer.data.model.ExamAnswerEntity

@Database(
    entities = [TopicsEntity::class, CourseEntity::class, UserEntity::class, QuizEntity::class, ExamAnswerEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicsDao(): TopicsDao
    abstract fun courseDao(): CourseDao
    abstract fun userDao(): UserDao
    abstract fun quizDao(): QuizDao
    abstract fun examAnswerDao(): ExamAnswerDao
}
