package com.avinashpatil.app.dishacomputer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.dishacomputer.api.models.UserDetailResponse
import com.avinashpatil.app.dishacomputer.data.store.UserPreferences
import com.avinashpatil.app.dishacomputer.utils.ErrorUtils
import com.avinashpatil.app.dishacomputer.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentDetailsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentDetailsUiState>(StudentDetailsUiState.Loading)
    val uiState: StateFlow<StudentDetailsUiState> = _uiState.asStateFlow()

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            val userId = userPreferences.getUserId()
            if (userId != null) {
                // 1. Observe local DB (Single Source of Truth)
                launch {
                    userRepository.getUserStream(userId).collectLatest { user ->
                        if (user != null) {
                            _uiState.value = StudentDetailsUiState.Success(user)
                        } else {
                            // If DB is empty, only set Loading if we aren't already in Success
                            // (Avoid flickering if we have data but stream emits null momentarily during migration, though unlikely)
                            if (_uiState.value !is StudentDetailsUiState.Success) {
                                _uiState.value = StudentDetailsUiState.Loading
                            }
                        }
                    }
                }
                
                // 2. Fetch fresh data from API
                refreshData(userId)
            } else {
                _uiState.value = StudentDetailsUiState.Error("User ID not found")
            }
        }
    }

    fun loadUserDetails() {
        viewModelScope.launch {
            val userId = userPreferences.getUserId()
            if (userId != null) {
                refreshData(userId)
            } else {
                _uiState.value = StudentDetailsUiState.Error("User ID not found")
            }
        }
    }

    private suspend fun refreshData(userId: String) {
        // If we don't have data, ensure we show Loading
        if (_uiState.value !is StudentDetailsUiState.Success) {
            _uiState.value = StudentDetailsUiState.Loading
        }

        userRepository.refreshUser(userId).onFailure { e ->
            // Only show error if we don't have cached data
            if (_uiState.value !is StudentDetailsUiState.Success) {
                _uiState.value = StudentDetailsUiState.Error(ErrorUtils.getFriendlyErrorMessage(e))
            }
        }
    }
}

sealed class StudentDetailsUiState {
    object Loading : StudentDetailsUiState()
    data class Success(val user: UserDetailResponse) : StudentDetailsUiState()
    data class Error(val message: String) : StudentDetailsUiState()
}
