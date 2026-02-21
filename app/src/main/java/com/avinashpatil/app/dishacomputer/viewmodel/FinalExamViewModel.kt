package com.avinashpatil.app.dishacomputer.viewmodel

import android.os.CountDownTimer
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.dishacomputer.api.models.QuestionDto as ApiQuestion
import com.avinashpatil.app.dishacomputer.api.models.QuizDto as ApiQuiz
import com.avinashpatil.app.dishacomputer.api.models.FullQuizDto
import com.avinashpatil.app.dishacomputer.data.model.ExamOption
import com.avinashpatil.app.dishacomputer.data.model.ExamQuestion
import com.avinashpatil.app.dishacomputer.data.model.ExamResult
import com.avinashpatil.app.dishacomputer.utils.ErrorUtils
import com.avinashpatil.app.dishacomputer.repository.ExamRepository
import com.avinashpatil.app.dishacomputer.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.Duration

@HiltViewModel
class FinalExamViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val examRepository: ExamRepository,
    private val apiService: com.avinashpatil.app.dishacomputer.api.ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExamUiState>(ExamUiState.Loading)
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

    private val _currentQuestionIndex = mutableStateOf(0)
    val currentQuestionIndex: State<Int> = _currentQuestionIndex

    private val _questionTimeLeft = mutableStateOf(30)
    val questionTimeLeft: State<Int> = _questionTimeLeft

    private val _totalTimeLeft = mutableStateOf(0) // Seconds remaining (Countdown)
    val totalTimeLeft: State<Int> = _totalTimeLeft

    private val _effectiveTotalTime = MutableStateFlow<Long>(0) // Static effective duration (Duration - Late)
    val effectiveTotalTime: StateFlow<Long> = _effectiveTotalTime.asStateFlow()

    private val _examEndTime = MutableStateFlow<Long>(0)
    val examEndTime: StateFlow<Long> = _examEndTime.asStateFlow()

    private val _userAnswers = mutableMapOf<Int, Int>()
    private val _userAnswersState = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val userAnswersState: StateFlow<Map<Int, Int>> = _userAnswersState.asStateFlow()

    private var questionTimer: CountDownTimer? = null
    private var totalTimer: CountDownTimer? = null

    private val _questions = mutableListOf<ExamQuestion>()
    private var backendQuestionIds: List<String> = emptyList()
    private var loadedCourseId: String? = null
    private var loadedQuizId: String? = null
    private var loadedDurationMinutes: Int? = null
    private var lastLoadKey: String? = null
    private var examEndTimeMillis: Long = 0

    private val _isPaused = mutableStateOf(false)
    val isPaused: State<Boolean> = _isPaused

    // Force Submit Counters
    private var backgroundSwitchCount = 0
    private var screenLockCount = 0
    private var backPressCount = 0
    private var homePressCount = 0
    
    // UI Warnings
    private val _warningMessage = MutableStateFlow<String?>(null)
    val warningMessage: StateFlow<String?> = _warningMessage.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _navigateToResult = MutableSharedFlow<String>()
    val navigateToResult = _navigateToResult.asSharedFlow()

    fun loadExam(quizId: String?) {
        val key = quizId?.takeIf { it.isNotBlank() } ?: "ccc_default"
        if (lastLoadKey == key && _uiState.value !is ExamUiState.Loading) return
        lastLoadKey = key

        viewModelScope.launch {
            _uiState.value = ExamUiState.Loading

            if (quizId.isNullOrBlank() || quizId == "random") {
                // ... (Existing random logic if needed, but for Final Exam we focus on strict rules)
                // Assuming Random is practice, but user wants FinalExamScreen to be strict.
                // Keeping existing random logic but adapting timers later.
                val result = quizRepository.getRandomCccQuestions(limit = 100)
                // ... (Existing fold logic)
                result.fold(
                    onSuccess = { (courseId, apiQuestions) ->
                        if (apiQuestions.isEmpty()) {
                            setErrorState("No questions available for the exam.")
                        } else {
                            val backendIds = apiQuestions.map { it.questionId ?: "" }
                            val examQuestions = apiQuestions.mapIndexed { index, q -> q.toExamQuestion(index) }
                            // For random, just set duration 60 mins from now
                            val durationMillis = 60 * 60 * 1000L
                            examEndTimeMillis = System.currentTimeMillis() + durationMillis
                            _effectiveTotalTime.value = durationMillis
                            setupExam(
                                courseId = courseId,
                                quizId = null,
                                backendQuestionIds = backendIds,
                                questions = examQuestions
                            )
                        }
                    },
                    onFailure = {
                        setErrorState(ErrorUtils.getFriendlyErrorMessage(it))
                    }
                )
            } else {
                val result = apiService.getFullQuizById(quizId)
                if (result.isSuccessful && result.body() != null) {
                     val quiz = result.body()!!
                     applyQuiz(quiz)
                } else {
                     val errorMsg = result.errorBody()?.string() ?: "Unknown error"
                     _uiState.value = ExamUiState.Error(errorMsg)
                }
            }
        }
    }

    private fun applyQuiz(quiz: FullQuizDto) {
        val apiQuestions = quiz.questions
        if (apiQuestions.isEmpty()) {
            setErrorState("No questions available in this quiz.")
            return
        }

        loadedDurationMinutes = quiz.durationMinutes?.takeIf { it > 0 }
        
        // Strict Time Logic
        val scheduledDate = quiz.scheduledDate // e.g., "2023-10-27"
        val scheduledTime = quiz.scheduledTime // e.g., "10:00:00"
        
        if (scheduledDate != null && scheduledTime != null) {
            try {
                // Parse scheduled time (Assuming IST or local, convert to System millis)
                // Note: User said "App converts everything to Asia/Kolkata"
                // Assuming date/time format yyyy-MM-dd HH:mm:ss
                // Adjust format as per actual API response. Assuming ISO or similar.
                val dateTimeStr = "$scheduledDate $scheduledTime" 
                // Flexible parsing or standard
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // Adjust if needed
                val startDateTime = LocalDateTime.parse(dateTimeStr, formatter)
                val zonedDateTime = startDateTime.atZone(ZoneId.of("Asia/Kolkata"))
                val startTimeMillis = zonedDateTime.toInstant().toEpochMilli()
                val durationMillis = (loadedDurationMinutes ?: 60) * 60 * 1000L
                examEndTimeMillis = startTimeMillis + durationMillis
                
                val now = System.currentTimeMillis()
                
                // 2. Exam Entry Window
                // Allow 10 min late? User said "User joins 10 min late -> OK (less time)"
                // Condition: now < examEndTime
                
                if (now > examEndTimeMillis) {
                    setErrorState("Exam has ended. You cannot enter now.")
                    return
                }
                
                if (now < startTimeMillis) {
                    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
                    val startTimeStr = zonedDateTime.format(timeFormatter)
                    setErrorState("Exam has not started yet. Please wait. Exam will begin at $startTimeStr")
                    return
                }
                
                // Logic checks passed
                
            } catch (e: Exception) {
                // Fallback if parsing fails (should verify API format)
                e.printStackTrace()
                examEndTimeMillis = System.currentTimeMillis() + (loadedDurationMinutes ?: 60) * 60 * 1000L
            }
        } else {
            // No schedule, use duration from now
            val durationMillis = (loadedDurationMinutes ?: 60) * 60 * 1000L
            examEndTimeMillis = System.currentTimeMillis() + durationMillis
            _examEndTime.value = examEndTimeMillis
            _effectiveTotalTime.value = durationMillis // No late join for unscheduled/practice
        }

        // Take max 100 questions
        val limitedQuestions = apiQuestions.take(100)
        
        val backendIds = limitedQuestions.map { it.questionId ?: "" }
        val examQuestions = limitedQuestions.mapIndexed { index, q -> q.toExamQuestion(index) }

        setupExam(
            courseId = limitedQuestions.firstOrNull()?.courseId ?: limitedQuestions.firstOrNull()?.topicId,
            quizId = quiz.quizId,
            backendQuestionIds = backendIds,
            questions = examQuestions
        )
    }

    private fun setupExam(
        courseId: String?,
        quizId: String?,
        backendQuestionIds: List<String>,
        questions: List<ExamQuestion>
    ) {
        if (questions.isEmpty()) {
            setErrorState("No questions to display.")
            return
        }

        loadedCourseId = courseId
        loadedQuizId = quizId
        this.backendQuestionIds = backendQuestionIds

        _userAnswers.clear()
        _questions.clear()
        _questions.addAll(questions)
        _currentQuestionIndex.value = 0
        
        // Restore local answers
        viewModelScope.launch {
            if (quizId != null) {
                val savedAnswers = quizRepository.getLocalAnswers(quizId)
                savedAnswers.forEach { entity ->
                    // Find index for questionId
                    val index = backendQuestionIds.indexOf(entity.questionId)
                    if (index != -1) {
                        _userAnswers[index] = entity.selectedOption
                    }
                }
            }
            
            _selectedReset()
            totalTimer?.cancel()
            totalTimer = null
            _uiState.value = ExamUiState.Questions(questions)
            startTimers()
        }
    }

    private fun setErrorState(message: String) {
        _uiState.value = ExamUiState.Error(message)
    }

    fun startTimers() {
        if (_isPaused.value) {
            _isPaused.value = false
        }

        // Question Timer (30s per question, just visual/restriction?)
        // User didn't specify strict question timer, but keeping it as is.
        questionTimer?.cancel()
        questionTimer = object : CountDownTimer((_questionTimeLeft.value * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _questionTimeLeft.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                _questionTimeLeft.value = 0
                // Auto next?
                // goToNextQuestion() // Optional: User might want to stay on question
            }
        }.start()

        // Total Deadline Timer
        if (totalTimer == null) {
            val now = System.currentTimeMillis()
            val remainingMillis = examEndTimeMillis - now
            
            if (remainingMillis <= 0) {
                forceSubmit("Time is up")
                return
            }
            
            totalTimer = object : CountDownTimer(remainingMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    _totalTimeLeft.value = (millisUntilFinished / 1000).toInt()
                }

                override fun onFinish() {
                    _totalTimeLeft.value = 0
                    forceSubmit("Time is up")
                }
            }.start()
        }
    }
    
    // ... existing pauseTimers, goToNextQuestion, etc.

    fun selectOption(questionId: Int, optionId: Int) {
        _userAnswers[questionId] = optionId
        // Save locally
        viewModelScope.launch {
            val qId = backendQuestionIds.getOrNull(questionId)
            val quizId = loadedQuizId
            if (qId != null && quizId != null) {
                quizRepository.saveAnswerLocally(quizId, qId, optionId)
            }
        }
        // goToNextQuestion() // Removed auto-next to allow review? Or keep it? keeping as is.
        // Actually user said "Save on option click". 
    }
    
    // Force Submit Logic
    fun onBackgroundDetected() {
        backgroundSwitchCount++
        if (backgroundSwitchCount == 1) {
            _warningMessage.value = "⚠️ You are leaving the exam. Next time, your exam will be submitted automatically."
        } else if (backgroundSwitchCount >= 2) {
            forceSubmit("App background limit exceeded")
        }
    }
    
    fun onBackPress() {
        backPressCount++
        if (backPressCount >= 3) {
            forceSubmit("Excessive back button presses")
        } else {
             _warningMessage.value = "⚠️ Pressing back is not allowed ($backPressCount/3)"
        }
    }
    
    fun onScreenLock() {
        screenLockCount++
        if (screenLockCount >= 2) {
            forceSubmit("Screen lock limit exceeded")
        }
    }
    
    fun forceSubmit(reason: String) {
        if (_isSubmitting.value) return
        questionTimer?.cancel()
        totalTimer?.cancel()
        submitExam(onError = { /* UI handles error */ })
    }
    
    fun clearWarning() {
        _warningMessage.value = null
    }

    fun pauseTimers() {
        _isPaused.value = true
        questionTimer?.cancel()
        totalTimer?.cancel()
    }

    fun goToNextQuestion() {
        questionTimer?.cancel()

        if (_currentQuestionIndex.value < _questions.size - 1) {
            _currentQuestionIndex.value += 1
            _selectedReset()
            startTimers()
        }
    }

    fun goToPreviousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
            _selectedReset()
            startTimers()
        }
    }

    fun submitExam(
        durationMinutes: Int = loadedDurationMinutes ?: 60,
        onSubmitted: ((String) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        val courseId = loadedCourseId
        val quizId = loadedQuizId
        if (courseId.isNullOrBlank() || _questions.isEmpty() || backendQuestionIds.isEmpty()) {
            onError?.invoke("Exam not loaded")
            return
        }
        
        if (_isSubmitting.value) return
        _isSubmitting.value = true

        viewModelScope.launch {
            try {
                val answersPayload = linkedMapOf<String, Int>()
                val solved = mutableListOf<String>()
                val unsolved = mutableListOf<String>()

                backendQuestionIds.forEachIndexed { index, backendId ->
                    val ans = _userAnswers[index]
                    if (ans == null || ans !in 0..3) {
                        unsolved.add(backendId)
                    } else {
                        solved.add(backendId)
                        answersPayload[backendId] = ans + 1
                    }
                }

                val submit = quizRepository.submitQuiz(
                    quizId = quizId ?: "final_$courseId",
                    answers = answersPayload,
                    solvedQuestionIds = solved,
                    unsolvedQuestionIds = unsolved
                )

                submit.fold(
                    onSuccess = {
                        questionTimer?.cancel()
                        totalTimer?.cancel()
                        
                        if (quizId != null) {
                            quizRepository.clearLocalAnswers(quizId)
                        }

                        val result = buildExamResult()
                        val examId = "local_final_${System.currentTimeMillis()}"
                        examRepository.putLocalResult(examId, result)
                        
                        _isSubmitting.value = false
                        _navigateToResult.emit(examId)
                        onSubmitted?.invoke(examId)
                    },
                    onFailure = { e ->
                        _isSubmitting.value = false
                        onError?.invoke(ErrorUtils.getFriendlyErrorMessage(e))
                    }
                )
            } catch (e: Exception) {
                _isSubmitting.value = false
                onError?.invoke(ErrorUtils.getFriendlyErrorMessage(e))
            }
        }
    }

    private fun buildExamResult(): ExamResult {
        val userAnswers = _userAnswers
            .filterValues { it in 0..3 }
            .toMap()

        val correct = _questions.count { q ->
            val user = userAnswers[q.id]
            val correctOptionId = q.options.indexOfFirst { it.isCorrect }
            user != null && user == correctOptionId
        }
        val answered = userAnswers.size
        val wrong = answered - correct
        val skipped = _questions.size - answered

        return ExamResult(
            totalMarks = _questions.size,
            obtainedMarks = correct,
            correctAnswers = correct,
            wrongAnswers = wrong,
            skippedQuestions = skipped,
            questions = _questions.toList(),
            userAnswers = userAnswers
        )
    }

    private fun ApiQuestion.toExamQuestion(id: Int): ExamQuestion {
        // Handle potentially null correctOption. 
        // If null, default to 0. Backend should ideally provide 1..4
        val correctOpt = correctOption ?: 0
        // Convert 1-based index (if valid) to 0-based
        val correctIndex = if (correctOpt in 1..4) correctOpt - 1 else -1

        val options = listOf(
            ExamOption(0, option1English ?: "", option1Marathi ?: "", 0 == correctIndex),
            ExamOption(1, option2English ?: "", option2Marathi ?: "", 1 == correctIndex),
            ExamOption(2, (option3English as String?) ?: "", (option3Marathi as String?) ?: "", 2 == correctIndex),
            ExamOption(3, (option4English as String?) ?: "", (option4Marathi as String?) ?: "", 3 == correctIndex)
        )
        
        val isTrueFalse = (option3English as String?).isNullOrBlank() && (option4English as String?).isNullOrBlank()

        return ExamQuestion(
            id = id,
            text = questionEnglish ?: "",
            textMarathi = questionMarathi ?: "",
            options = options,
            isTrueFalse = isTrueFalse
        )
    }



    private fun _selectedReset() {
        _questionTimeLeft.value = 30
    }

    override fun onCleared() {
        super.onCleared()
        questionTimer?.cancel()
        totalTimer?.cancel()
    }
}

sealed class ExamUiState {
    object Loading : ExamUiState()
    data class Questions(val questions: List<ExamQuestion>) : ExamUiState()
    data class Result(val result: ExamResult) : ExamUiState()
    data class Error(val message: String) : ExamUiState()
}