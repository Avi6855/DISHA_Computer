package com.avinashpatil.app.dishacomputer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.avinashpatil.app.dishacomputer.data.model.ExamAnswerEntity

@Dao
interface ExamAnswerDao {
    @Query("SELECT * FROM exam_answers WHERE quizId = :quizId")
    @JvmSuppressWildcards
    suspend fun getAnswersForQuiz(quizId: String): List<ExamAnswerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    suspend fun insertAnswer(answer: ExamAnswerEntity): Long
    
    @Query("DELETE FROM exam_answers WHERE quizId = :quizId")
    @JvmSuppressWildcards
    suspend fun clearAnswersForQuiz(quizId: String): Int
}
