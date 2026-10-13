package com.avinashpatil.app.dishacomputer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class RankingViewModel @Inject constructor(
    private val examRepository: ExamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RankingUiState>(RankingUiState.Loading)
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _uiState.value = RankingUiState.Loading
            examRepository.getLeaderboard().fold(
                onSuccess = { users ->
                    _uiState.value = RankingUiState.Success(users)
                },
                onFailure = { error ->
                    _uiState.value = RankingUiState.Error(
                        ErrorUtils.getFriendlyErrorMessage(error)
                    )
                }
            )
        }
    }
}

sealed class RankingUiState {
    object Loading : RankingUiState()
    data class Success(val users: List<LeaderboardUser>) : RankingUiState()
    data class Error(val message: String) : RankingUiState()
}
