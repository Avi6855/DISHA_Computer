package com.avinashpatil.app.dishacomputer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.dishacomputer.api.models.FullQuizDto
import com.avinashpatil.app.dishacomputer.data.model.ExamResult
import com.avinashpatil.app.dishacomputer.repository.ExamRepository
import com.avinashpatil.app.dishacomputer.repository.QuizRepository
import com.avinashpatil.app.dishacomputer.utils.ErrorUtils
import com.avinashpatil.app.dishacomputer.screens.result.List_Option
import com.avinashpatil.app.dishacomputer.screens.result.ReviewQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

sealed class CertificationResultUiState {
    object Loading : CertificationResultUiState()
    data class Success(
        val score: Int,
        val totalScore: Int,
        val correctAnswers: Int,
        val wrongAnswers: Int,
        val skippedQuestions: Int,
        val accuracy: String,
        val accuracyVal: Int,
        val examTitleMarathi: String,
        val examTitleEnglish: String,
        val duration: String,
        val date: String,
        val time: String,
        val questions: List<ReviewQuestion>
    ) : CertificationResultUiState()
    data class Error(val message: String) : CertificationResultUiState()
}

@HiltViewModel
class CertificationExamResultViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val examRepository: ExamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CertificationResultUiState>(CertificationResultUiState.Loading)
    val uiState: StateFlow<CertificationResultUiState> = _uiState.asStateFlow()

    fun loadData(quizId: String, examId: String) {
        viewModelScope.launch {
            _uiState.value = CertificationResultUiState.Loading
            try {
                // Fetch Quiz Details
                val quizResult = quizRepository.getFullQuizById(quizId)
                
                // Fetch Exam Result (Local)
                val examResultRes = examRepository.getExamResult(examId)

                if (quizResult.isSuccess && examResultRes.isSuccess) {
                    val quiz = quizResult.getOrThrow()
                    val result = examResultRes.getOrThrow()
                    
                    processResult(quiz, result)
                } else {
                    _uiState.value = CertificationResultUiState.Error("Failed to load data")
                }
            } catch (e: Exception) {
                _uiState.value = CertificationResultUiState.Error(ErrorUtils.getFriendlyErrorMessage(e))
            }
        }
    }

    private fun processResult(quiz: FullQuizDto, result: ExamResult) {
        val totalScore = result.totalMarks
        val score = result.obtainedMarks
        val correct = result.correctAnswers
        val wrong = result.wrongAnswers
        val skipped = result.skippedQuestions

        val accuracyVal = if (totalScore > 0) {
            (correct.toFloat() / totalScore.toFloat() * 100).roundToInt()
        } else {
            0
        }
        val accuracy = "$accuracyVal%"

        // Use title for both as API only provides one title field
        val quizTitle = quiz.title ?: ""

        // Limit to 100 questions to match FinalExamViewModel logic
        val questionsToReview = quiz.questions.take(100)

        val reviewQuestions = questionsToReview.mapIndexed { index, q ->
            // Use index as ID to match FinalExamViewModel's logic
            val userSelectedOptionIndex = result.userAnswers[index] // 0-based index
            
            val options = listOf(
                List_Option("A", q.option1Marathi ?: "", q.option1English ?: ""),
                List_Option("B", q.option2Marathi ?: "", q.option2English ?: ""),
                List_Option("C", (q.option3Marathi as String?) ?: "", (q.option3English as String?) ?: ""),
                List_Option("D", (q.option4Marathi as String?) ?: "", (q.option4English as String?) ?: "")
            ).filter { it.textEnglish.isNotBlank() || it.textMarathi.isNotBlank() }

            val correctOptionIndex = (q.correctOption ?: 1) - 1
            
            val correctOptionId = when (correctOptionIndex) {
                0 -> "A"
                1 -> "B"
                2 -> "C"
                3 -> "D"
                else -> ""
            }

            val userIdx = result.userAnswers[index]
            val selectedOptionId = if (userIdx != null) {
                when(userIdx) {
                    0 -> "A"
                    1 -> "B"
                    2 -> "C"
                    3 -> "D"
                    else -> null
                }
            } else null

            val isCorrect = selectedOptionId == correctOptionId
            val marks = if (isCorrect) (q.marks ?: 1) else 0

            ReviewQuestion(
                id = index + 1, // Display ID 1-based
                questionMarathi = q.questionMarathi ?: "",
                questionEnglish = q.questionEnglish ?: "",
                options = options,
                correctOptionId = correctOptionId,
                selectedOptionId = selectedOptionId,
                marks = marks
            )
        }

        _uiState.value = CertificationResultUiState.Success(
            score = score,
            totalScore = totalScore,
            correctAnswers = correct,
            wrongAnswers = wrong,
            skippedQuestions = skipped,
            accuracy = accuracy,
            accuracyVal = accuracyVal,
            examTitleMarathi = quizTitle,
            examTitleEnglish = quizTitle,
            duration = "60 min", // Fixed duration for certification
            date = "", // TODO: Format date
            time = "", // TODO: Format time
            questions = reviewQuestions
        )
    }
}
