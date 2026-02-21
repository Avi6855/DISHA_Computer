package com.avinashpatil.app.dishacomputer.screens.login

import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.dishacomputer.api.models.LoginRequest
import com.avinashpatil.app.dishacomputer.data.store.TokenManager
import com.avinashpatil.app.dishacomputer.data.store.UserPreferences
import com.avinashpatil.app.dishacomputer.repository.AuthRepository
import com.avinashpatil.app.dishacomputer.repository.UserRepository
import com.avinashpatil.app.dishacomputer.utils.ErrorUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class StartDestination {
    object Login : StartDestination()
    object Restriction : StartDestination()
    object Home : StartDestination()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences,
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _deviceName = MutableStateFlow(Build.MODEL)
    val deviceName: StateFlow<String> = _deviceName
    
    suspend fun hasActiveSession(): Boolean {
        return userPreferences.isLoggedIn() && tokenManager.hasTokens()
    }
    
    // Function to check if user is locked
    suspend fun isUserLocked(): Boolean {
        return userPreferences.getUserLockStatus()
    }

    suspend fun resolveStartDestination(): StartDestination {
        val hasSession = hasActiveSession()
        if (!hasSession) {
            return StartDestination.Login
        }
        
        // Try to fetch latest status from API
        val userId = userPreferences.getUserId()
        if (!userId.isNullOrEmpty()) {
            try {
                val result = userRepository.getUserById(userId)
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    if (user != null) {
                        // API Source of Truth
                        return if (user.lock == true) StartDestination.Restriction else StartDestination.Home
                    }
                }
            } catch (e: Exception) {
                // Ignore API error and fall back to local
            }
        }

        val isLocked = isUserLocked()
        if (isLocked) {
            return StartDestination.Restriction
        }
        return StartDestination.Home
    }

    fun login(email: String, dishaId: String) {
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading

                val loginRequest = LoginRequest(
                    email = email,
                    dishaId = dishaId,
                    rememberMe = true,
                    deviceName = deviceName.value
                )

                val result = authRepository.login(loginRequest)
                result.fold(
                    onSuccess = { auth ->
                        val loginResponse = auth.response
                        val user = loginResponse.user

                        if (user == null || user.userId.isNullOrEmpty() || user.dishaId.isNullOrEmpty()) {
                            _loginState.value = LoginState.Error("Invalid user data in response.")
                            return@fold
                        }

                        if (auth.isLocked) {
                            userPreferences.setUserLockStatus(true)
                            _loginState.value = LoginState.Error("Your account is locked. Please contact the branch.")
                            return@fold
                        }

                        userPreferences.saveUserData(
                            userId = user.userId ?: "",
                            dishaId = user.dishaId ?: "",
                            email = user.email ?: "",
                            fullName = user.fullName ?: ""
                        )
                        userPreferences.setUserLockStatus(false)

                        tokenManager.saveTokens(
                            accessToken = loginResponse.token ?: "",
                            refreshToken = loginResponse.refreshToken?.token ?: ""
                        )

                        _loginState.value = LoginState.Success
                    },
                    onFailure = { e ->
                        _loginState.value = LoginState.Error(ErrorUtils.getFriendlyErrorMessage(e))
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Login Exception: ${e.message}")
                _loginState.value = LoginState.Error(ErrorUtils.getFriendlyErrorMessage(e))
            }
        }
    }
}
