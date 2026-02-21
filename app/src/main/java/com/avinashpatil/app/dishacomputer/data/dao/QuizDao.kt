package com.avinashpatil.app.dishacomputer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.avinashpatil.app.dishacomputer.data.model.QuizEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuizDao {
    @Query("SELECT * FROM quizzes WHERE courseId = :courseId")
    abstract fun getQuizzesByCourseId(courseId: String): Flow<List<QuizEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    abstract suspend fun insertAll(quizzes: List<QuizEntity>): List<Long>

    @Query("DELETE FROM quizzes WHERE courseId = :courseId")
    @JvmSuppressWildcards
    abstract suspend fun deleteByCourseId(courseId: String): Int
    
    @Query("DELETE FROM quizzes")
    @JvmSuppressWildcards
    abstract suspend fun clearAll(): Int
}
