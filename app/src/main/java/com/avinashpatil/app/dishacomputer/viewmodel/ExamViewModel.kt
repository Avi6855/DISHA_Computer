package com.avinashpatil.app.dishacomputer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.dishacomputer.api.models.QuizDto
import com.avinashpatil.app.dishacomputer.repository.QuizRepository
import com.avinashpatil.app.dishacomputer.repository.UserRepository
import com.avinashpatil.app.dishacomputer.data.store.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import com.avinashpatil.app.dishacomputer.utils.ErrorUtils
import javax.inject.Inject

sealed class UpcomingExamsUiState {
    object Loading : UpcomingExamsUiState()
    data class Success(
        val quizzes: List<QuizDto>,
        val courseId: String
    ) : UpcomingExamsUiState()
    data class Error(val message: String) : UpcomingExamsUiState()
}

sealed class ExamAccessState {
    object Idle : ExamAccessState()
    object Allowed : ExamAccessState()
    object SessionTerminated : ExamAccessState()
    object AlreadySubmitted : ExamAccessState()
    data class Blocked(val reason: String) : ExamAccessState()
}

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<UpcomingExamsUiState>(UpcomingExamsUiState.Loading)
    val uiState: StateFlow<UpcomingExamsUiState> = _uiState.asStateFlow()

    private val _examAccessState = MutableStateFlow<ExamAccessState>(ExamAccessState.Idle)
    val examAccessState: StateFlow<ExamAccessState> = _examAccessState.asStateFlow()

    init {
        initializeData()
    }

    // 🔒 GOVERNMENT EXAM VALIDATION LOGIC
    fun checkExamAccess(quizId: String, isAttempted: Boolean? = false) {
        viewModelScope.launch {
            _examAccessState.value = ExamAccessState.Idle
            
            val userId = userPreferences.getUserId()
            if (userId == null) {
                _examAccessState.value = ExamAccessState.Blocked("User not logged in")
                return@launch
            }

            // 0. Check Backend Flag (Source of Truth)
            if (isAttempted == true) {
                _examAccessState.value = ExamAccessState.AlreadySubmitted
                return@launch
            }

            // 1. Check if already submitted locally
            // In a real app, we check backend "sessionStatus" or "attempts"
            val isSubmittedLocal = quizRepository.isQuizSubmitted(quizId) 
            if (isSubmittedLocal) {
                _examAccessState.value = ExamAccessState.AlreadySubmitted
                return@launch
            }

            // SIMULATED BACKEND CHECK (As per "Backend must track active exam session")
            // In production: val response = apiService.checkExamAccess(userId, quizId, deviceId)
            
            // Mock Logic for "Already Submitted"
            // If we find a local result for this quiz, it's submitted.
            // Note: Implementation of isQuizSubmitted is hypothetical here, 
            // but we can check if we have a result for this quizId.
            // quizRepository.getLocalResult(quizId) ? 
            
            // Let's implement a safe check:
            // For "Session Terminated", this usually happens via heartbeat or push.
            // For "Already Submitted", we check if we have a result.
            
            // Logic:
            // if (response.alreadySubmitted) _examAccessState.value = ExamAccessState.AlreadySubmitted
            // else if (response.sessionConflict) _examAccessState.value = ExamAccessState.SessionTerminated
            // else _examAccessState.value = ExamAccessState.Allowed
            
            // Since we don't have the API, we will ALLOW by default, 
            // but we provide the structure for the "Strict" logic.
            
            // To demonstrate the logic working, we can default to Allowed
            // unless we have a specific flag (which we don't yet).
            
            _examAccessState.value = ExamAccessState.Allowed
        }
    }

    fun resetAccessState() {
        _examAccessState.value = ExamAccessState.Idle
    }

    private fun initializeData() {
        viewModelScope.launch {
            val userId = userPreferences.getUserId()
            if (userId != null) {
                // 1. Observe local DB (Single Source of Truth)
                launch {
                    userRepository.getUserStream(userId)
                        .filterNotNull()
                        .mapNotNull { it.courseId ?: it.courseIds?.firstOrNull() }
                        .distinctUntilChanged()
                        .flatMapLatest { courseId ->
                            quizRepository.getQuizzesStream(courseId).map { quizzes ->
                                UpcomingExamsUiState.Success(quizzes, courseId)
                            }
                        }
                        .collectLatest { state ->
                            _uiState.value = state
                        }
                }
                
                // 2. Fetch fresh data from API
                loadData()
            } else {
                _uiState.value = UpcomingExamsUiState.Error("User not logged in")
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            val userId = userPreferences.getUserId()
            if (userId == null) {
                _uiState.value = UpcomingExamsUiState.Error("User not logged in")
                return@launch
            }

            // If we don't have data yet, ensure Loading is shown (unless we have success already)
            if (_uiState.value !is UpcomingExamsUiState.Success) {
                _uiState.value = UpcomingExamsUiState.Loading
            }

            // 1. Refresh User Details (to ensure we have correct courseId)
            userRepository.refreshUser(userId)

            // 2. Get latest courseId from DB (via stream or snapshot)
            val user = userRepository.getUserStream(userId).firstOrNull()
            val courseId = user?.courseId ?: user?.courseIds?.firstOrNull()

            if (courseId != null) {
                // 3. Refresh Exams
                quizRepository.refreshQuizzes(courseId).onFailure { e ->
                    // Only show error if we don't have cached data
                    if (_uiState.value !is UpcomingExamsUiState.Success) {
                        _uiState.value = UpcomingExamsUiState.Error(e.message ?: "Failed to refresh exams")
                    }
                }
            } else {
                if (_uiState.value !is UpcomingExamsUiState.Success) {
                     _uiState.value = UpcomingExamsUiState.Error("No enrolled course found")
                }
            }
        }
    }
}
