package com.avinashpatil.app.dishacomputer.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ExamQuestion(
    val id: Int,
    val text: String,
    val textMarathi: String,
    val options: List<ExamOption>,
    val isTrueFalse: Boolean = false
)

@Serializable
data class ExamOption(
    val id: Int,
    val text: String,
    val textMarathi: String,
    val isCorrect: Boolean
)


data class QuizState(
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(),
    val timeLeft: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null,
    val score: Int = 0,
    val isPracticeMode: Boolean = true,
    val isFinished: Boolean = false
)

@Serializable
data class Question(
    val id: String,
    val questionEnglish: String,
    val questionMarathi: String,
    val optionsEnglish: List<String>,
    val optionsMarathi: List<String>,
    val correctOptionIndex: Int,
    val type: QuestionType,
    val explanation: String?
)

@Serializable
data class ExamResult(
    val totalMarks: Int,
    val obtainedMarks: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val skippedQuestions: Int,
    val questions: List<ExamQuestion>,
    val userAnswers: Map<Int, Int>
)

@Serializable
data class ExamQuestionResult(
    val id: Int,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val userAnswerIndex: Int?,
    val isCorrect: Boolean = userAnswerIndex == correctAnswerIndex
)

data class QuizResponse(
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val id: String,
    val text: String,
    val textMarathi: String,
    val options: List<QuizOption>,
    val correctOptionId: Int,
    val explanation: String? = null
)

data class QuizOption(
    val id: Int,
    val text: String,
    val textMarathi: String
)

@Serializable
data class QuestionResult(
    val questionId: String,
    val selectedOptionIndex: Int?,
    val isCorrect: Boolean,
    val timeTaken: Long
)

enum class QuizType {
    PRACTICE,
    FINAL_EXAM
}

enum class QuestionType {
    MCQ,
    TRUE_FALSE
}