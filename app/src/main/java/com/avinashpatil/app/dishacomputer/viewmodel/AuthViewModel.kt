package com.avinashpatil.app.dishacomputer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.dishacomputer.api.AuthApi
import com.avinashpatil.app.dishacomputer.api.models.RefreshTokenRequest
import com.avinashpatil.app.dishacomputer.data.store.TokenManager
import com.avinashpatil.app.dishacomputer.data.store.UserPreferences
import com.avinashpatil.app.dishacomputer.api.models.UserDetailResponse
import com.avinashpatil.app.dishacomputer.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val userPreferences: UserPreferences,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _logoutState = MutableStateFlow(false)
    val logoutState: StateFlow<Boolean> = _logoutState.asStateFlow()

    private val _userIdState = MutableStateFlow<String?>(null)
    val userIdState: StateFlow<String?> = _userIdState.asStateFlow()

    private val _userProfileState = MutableStateFlow<UserDetailResponse?>(null)
    val userProfileState: StateFlow<UserDetailResponse?> = _userProfileState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.getUserId().let { id ->
                _userIdState.value = id
                if (id != null) {
                    fetchUserProfile(id)
                }
            }
        }
    }

    private fun fetchUserProfile(userId: String) {
        viewModelScope.launch {
            val result = userRepository.getUserById(userId)
            result.onSuccess { user ->
                _userProfileState.value = user
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val refreshToken = tokenManager.getRefreshToken()
                if (!refreshToken.isNullOrEmpty()) {
                    authApi.logout(RefreshTokenRequest(refreshToken))
                }
            } catch (_: Exception) {
            } finally {
                userPreferences.logout()
                tokenManager.clearTokens()
                _logoutState.value = true
            }
        }
    }

    fun resetLogoutState() {
        _logoutState.value = false
    }
}

