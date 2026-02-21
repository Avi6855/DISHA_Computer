package com.avinashpatil.app.dishacomputer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.dishacomputer.data.model.ExamResult
import com.avinashpatil.app.dishacomputer.api.models.LeaderboardUser
import com.avinashpatil.app.dishacomputer.repository.ExamRepository
import com.avinashpatil.app.dishacomputer.utils.ErrorUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExamResultViewModel @Inject constructor(
    private val examRepository: ExamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExamResultUiState>(ExamResultUiState.Loading)
    val uiState: StateFlow<ExamResultUiState> = _uiState.asStateFlow()

    private val _leaderboardState = MutableStateFlow<LeaderboardUiState>(LeaderboardUiState.Hidden)
    val leaderboardState: StateFlow<LeaderboardUiState> = _leaderboardState.asStateFlow()

    fun loadExamResult(examId: String) {
        viewModelScope.launch {
            _uiState.value = ExamResultUiState.Loading
            examRepository.getExamResult(examId).fold(
                onSuccess = { result ->
                    _uiState.value = ExamResultUiState.Success(result)
                },
                onFailure = { error ->
                    _uiState.value = ExamResultUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _leaderboardState.value = LeaderboardUiState.Loading
            examRepository.getLeaderboard().fold(
                onSuccess = { users ->
                    _leaderboardState.value = LeaderboardUiState.Success(users)
                },
                onFailure = { error ->
                    _leaderboardState.value = LeaderboardUiState.Error(ErrorUtils.getFriendlyErrorMessage(error))
                }
            )
        }
    }
}

sealed class ExamResultUiState {
    object Loading : ExamResultUiState()
    data class Success(val examResult: ExamResult) : ExamResultUiState()
    data class Error(val message: String) : ExamResultUiState()
}

sealed class LeaderboardUiState {
    object Hidden : LeaderboardUiState()
    object Loading : LeaderboardUiState()
    data class Success(val users: List<LeaderboardUser>) : LeaderboardUiState()
    data class Error(val message: String) : LeaderboardUiState()
}