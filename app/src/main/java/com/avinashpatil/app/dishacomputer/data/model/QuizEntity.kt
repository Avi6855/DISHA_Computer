package com.avinashpatil.app.dishacomputer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.avinashpatil.app.dishacomputer.api.models.QuizDto
import com.avinashpatil.app.dishacomputer.api.models.QuizQuestionDTO
import com.avinashpatil.app.dishacomputer.data.converters.Converters

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey val quizId: String,
    val quizName: String?,
    val durationMinutes: Int?,
    val scheduledTime: String?,
    val scheduledDate: String?,
    val courseId: String?,
    val topicId: String?,
    val sample: Boolean?,
    val attempted: Boolean?,
    // We will use a TypeConverter to store the list of questions as a JSON string
    val questions: List<QuizQuestionDTO> = emptyList()
) {
    fun toDomainModel(): QuizDto {
        return QuizDto(
            quizId = quizId,
            title = quizName,
            durationMinutes = durationMinutes,
            scheduledTime = scheduledTime,
            scheduledDate = scheduledDate,
            courseId = courseId,
            topicId = topicId,
            sample = sample,
            attempted = attempted,
            questions = questions
        )
    }
}

fun QuizDto.toEntity(): QuizEntity {
    return QuizEntity(
        quizId = quizId ?: "",
        quizName = title,
        durationMinutes = durationMinutes,
        scheduledTime = scheduledTime,
        scheduledDate = scheduledDate,
        courseId = courseId,
        topicId = topicId,
        sample = sample,
        attempted = attempted,
        questions = questions
    )
}
