package com.avinashpatil.app.dishacomputer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_answers", primaryKeys = ["quizId", "questionId"])
data class ExamAnswerEntity(
    val quizId: String,
    val questionId: String,
    val selectedOption: Int,
    val timestamp: Long
)
