package com.avinashpatil.app.dishacomputer.viewmodel

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.dishacomputer.api.models.QuestionDto as ApiQuestion
import com.avinashpatil.app.dishacomputer.api.models.QuizDto as Quiz
import com.avinashpatil.app.dishacomputer.data.model.ExamOption
import com.avinashpatil.app.dishacomputer.data.model.ExamQuestion
import com.avinashpatil.app.dishacomputer.data.model.ExamResult
import com.avinashpatil.app.dishacomputer.data.model.Question
import com.avinashpatil.app.dishacomputer.data.model.QuizState
import com.avinashpatil.app.dishacomputer.data.model.QuestionType
import com.avinashpatil.app.dishacomputer.repository.ExamRepository
import com.avinashpatil.app.dishacomputer.repository.QuizRepository
import com.avinashpatil.app.dishacomputer.utils.ErrorUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FinalExamState {
    object Loading : FinalExamState()
    object NotScheduled : FinalExamState()
    object NotEligible : FinalExamState()
    data class Available(val quiz: Quiz) : FinalExamState()
    data class Error(val message: String) : FinalExamState()
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val examRepository: ExamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState

    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    private val _selectedAnswer = MutableStateFlow(-1)
    val selectedAnswer: StateFlow<Int> = _selectedAnswer

    private val _answersByQuestionIndex = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val answersByQuestionIndex: StateFlow<Map<Int, Int>> = _answersByQuestionIndex

    private val _questionTimeLeft = MutableStateFlow(30)
    val questionTimeLeft: StateFlow<Int> = _questionTimeLeft

    private val _totalTimeLeft = MutableStateFlow(1800)
    val totalTimeLeft: StateFlow<Int> = _totalTimeLeft

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused
    
    private val _finalExamState = MutableStateFlow<FinalExamState>(FinalExamState.Loading)
    val finalExamState: StateFlow<FinalExamState> = _finalExamState

    private var questionTimer: CountDownTimer? = null
    private var totalTimer: CountDownTimer? = null
    private var loadedCourseId: String? = null
    private var loadedQuizId: String? = null
    private var loadedQuestions: List<Question> = emptyList()

    fun loadFinalExam() {
        viewModelScope.launch {
            _finalExamState.value = FinalExamState.Loading
            try {
                val result = quizRepository.resolveFinalExam()
                result.fold(
                    onSuccess = { resolved ->
                        val quiz = resolved.quiz
                        val userCourseId = resolved.userCourseId
                        val quizCourseId = resolved.quizCourseId

                        val eligible = !userCourseId.isNullOrBlank() &&
                            !quizCourseId.isNullOrBlank() &&
                            userCourseId == quizCourseId

                        if (quiz == null || quiz.questions.isEmpty()) {
                            _finalExamState.value = FinalExamState.NotScheduled
                            return@fold
                        }

                        Log.d(
                            "FINAL_EXAM_DEBUG",
                            "userCourseId=$userCourseId, quizCourseId=$quizCourseId, eligible=$eligible, quizId=${quiz.quizId}, questions=${quiz.questions.size}"
                        )

                        _finalExamState.value = if (eligible) {
                            FinalExamState.Available(quiz)
                        } else {
                            FinalExamState.NotEligible
                        }
                    },
                    onFailure = {
                        _finalExamState.value = FinalExamState.NotScheduled
                    }
                )
            } catch (e: Exception) {
                _finalExamState.value = FinalExamState.Error(ErrorUtils.getFriendlyErrorMessage(e))
            }
        }
    }

    fun loadQuiz(courseId: String?, limit: Int = 25) {
        _uiState.value = QuizUiState.Loading
        viewModelScope.launch {
            try {
                // If courseId is null, try to load random CCC questions
                val result = if (courseId == null) {
                    quizRepository.getRandomCccQuestions(limit)
                        .map { it.second }
                } else {
                    quizRepository.getCourseTopicsQuestions(courseId)
                }
                handleQuizResult(result, courseId, limit)
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(ErrorUtils.getFriendlyErrorMessage(e))
            }
        }
    }

    fun loadQuizByTopic(topicId: String, limit: Int = 25) {
        _uiState.value = QuizUiState.Loading
        viewModelScope.launch {
            try {
                val result = quizRepository.getQuestionsByTopicId(topicId)
                handleQuizResult(result, null, limit)
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(ErrorUtils.getFriendlyErrorMessage(e))
            }
        }
    }

    private fun handleQuizResult(result: Result<List<ApiQuestion>>, courseId: String?, limit: Int) {
        result.fold(
            onSuccess = { apiQuestions ->
                if (apiQuestions.isEmpty()) {
                    _uiState.value = QuizUiState.Error("Questions are not available.")
                    return@fold
                }

                loadedCourseId = courseId ?: apiQuestions.firstOrNull()?.courseId
                loadedQuizId = null
                val questions = apiQuestions.shuffled().take(limit).map { it.toUiQuestion() }
                loadedQuestions = questions
                _answersByQuestionIndex.value = emptyMap()
                _currentQuestionIndex.value = 0
                _selectedAnswer.value = -1
                _questionTimeLeft.value = 30
                _totalTimeLeft.value = 1800
                totalTimer?.cancel()
                totalTimer = null
                _uiState.value = QuizUiState.Success(questions)
                startTimers()
            },
            onFailure = { error ->
                _uiState.value = QuizUiState.Error(ErrorUtils.getFriendlyErrorMessage(error))
            }
        )
    }

    fun selectAnswer(index: Int) {
        _selectedAnswer.value = index
        _answersByQuestionIndex.value = _answersByQuestionIndex.value.toMutableMap().apply {
            this[_currentQuestionIndex.value] = index
        }
    }

    fun updateQuestionTimer(time: Int) {
        _questionTimeLeft.value = time
    }

    fun updateTotalTimer(time: Int) {
        _totalTimeLeft.value = time
    }

    fun goToNextQuestion() {
        _currentQuestionIndex.value = _currentQuestionIndex.value + 1
        _selectedAnswer.value = _answersByQuestionIndex.value[_currentQuestionIndex.value] ?: -1
        _questionTimeLeft.value = 30
        startTimers()
    }

    fun goToPreviousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value = _currentQuestionIndex.value - 1
            _selectedAnswer.value = _answersByQuestionIndex.value[_currentQuestionIndex.value] ?: -1
            _questionTimeLeft.value = 30
            startTimers()
        }
    }

    fun togglePause() {
        _isPaused.value = !_isPaused.value
        if (_isPaused.value) {
            questionTimer?.cancel()
            totalTimer?.cancel()
        } else {
            startTimers()
        }
    }

    private fun startTimers() {
        questionTimer?.cancel()
        questionTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _questionTimeLeft.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                _questionTimeLeft.value = 0
                goToNextQuestion()
            }
        }.start()

        if (totalTimer == null) {
            totalTimer = object : CountDownTimer((_totalTimeLeft.value * 1000).toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    _totalTimeLeft.value = (millisUntilFinished / 1000).toInt()
                }

                override fun onFinish() {
                    _totalTimeLeft.value = 0
                }
            }.start()
        }
    }

    override fun onCleared() {
        super.onCleared()
        questionTimer?.cancel()
        totalTimer?.cancel()
    }

    fun submitPracticeQuiz(
        durationMinutes: Int = 30,
        onSubmitted: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val courseId = loadedCourseId
        val quizId = loadedQuizId
        if (courseId.isNullOrBlank() || quizId.isNullOrBlank() || loadedQuestions.isEmpty()) {
            onError("Quiz not loaded")
            return
        }

        viewModelScope.launch {
            try {
                val answersByIndex = _answersByQuestionIndex.value
                val solved = mutableListOf<String>()
                val unsolved = mutableListOf<String>()
                val answersPayload = linkedMapOf<String, Int>()

                loadedQuestions.forEachIndexed { idx, q ->
                    val ans = answersByIndex[idx]
                    if (ans == null || ans !in 0..3) {
                        unsolved.add(q.id)
                    } else {
                        solved.add(q.id)
                        answersPayload[q.id] = ans + 1
                    }
                }

                val submitResult = quizRepository.submitQuiz(
                    quizId = quizId,
                    answers = answersPayload,
                    solvedQuestionIds = solved,
                    unsolvedQuestionIds = unsolved
                )
                submitResult.fold(
                    onSuccess = {
                        questionTimer?.cancel()
                        totalTimer?.cancel()
                        val examResult = buildExamResult(
                            questions = loadedQuestions,
                            answersByIndex = answersByIndex,
                            totalTimeSeconds = durationMinutes * 60
                        )
                        val examId = "local_practice_${System.currentTimeMillis()}"
                        examRepository.putLocalResult(examId, examResult)
                        onSubmitted(examId)
                    },
                    onFailure = { e ->
                        onError(e.message ?: "Failed to submit quiz")
                    }
                )
            } catch (e: Exception) {
                onError(e.message ?: "Failed to submit quiz")
            }
        }
    }

    private fun ApiQuestion.toUiQuestion(): Question {
        val correctIndex = correctIndexFromApi(correctOption ?: 0)
        return Question(
            id = questionId ?: "",
            questionEnglish = questionEnglish ?: "",
            questionMarathi = questionMarathi ?: "",
            optionsEnglish = listOf(
                option1English ?: "",
                option2English ?: "",
                (option3English as String?) ?: "",
                (option4English as String?) ?: ""
            ),
            optionsMarathi = listOf(
                option1Marathi ?: "",
                option2Marathi ?: "",
                (option3Marathi as String?) ?: "",
                (option4Marathi as String?) ?: ""
            ),
            correctOptionIndex = correctIndex,
            type = QuestionType.MCQ,
            explanation = null
        )
    }

    private fun correctIndexFromApi(correctOption: Int): Int {
        return if (correctOption in 1..4) correctOption - 1 else correctOption
    }

    private fun buildExamResult(
        questions: List<Question>,
        answersByIndex: Map<Int, Int>,
        totalTimeSeconds: Int
    ): ExamResult {
        val examQuestions = questions.mapIndexed { index, q ->
            val correctIdx = q.correctOptionIndex
            val options = q.optionsEnglish.zip(q.optionsMarathi).mapIndexed { optIndex, (en, mr) ->
                ExamOption(
                    id = optIndex,
                    text = en,
                    textMarathi = mr,
                    isCorrect = optIndex == correctIdx
                )
            }
            ExamQuestion(
                id = index,
                text = q.questionEnglish,
                textMarathi = q.questionMarathi,
                options = options
            )
        }

        val userAnswers = answersByIndex
            .filter { (_, ans) -> ans in 0..3 }
            .mapKeys { (index, _) -> index }

        val correct = examQuestions.count { eq ->
            val user = userAnswers[eq.id]
            val correctOptionId = eq.options.indexOfFirst { it.isCorrect }
            user != null && user == correctOptionId
        }
        val answered = userAnswers.size
        val wrong = answered - correct
        val skipped = questions.size - answered

        return ExamResult(
            totalMarks = questions.size,
            obtainedMarks = correct,
            correctAnswers = correct,
            wrongAnswers = wrong,
            skippedQuestions = skipped,
            questions = examQuestions,
            userAnswers = userAnswers
        )
    }
}

sealed class QuizUiState {
    object Loading : QuizUiState()
    data class Success(val questions: List<Question>) : QuizUiState()
    data class Error(val message: String) : QuizUiState()
}
