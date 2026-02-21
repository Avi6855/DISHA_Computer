package com.avinashpatil.app.dishacomputer.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.dishacomputer.api.AuthApi
import com.avinashpatil.app.dishacomputer.api.models.RefreshTokenRequest
import com.avinashpatil.app.dishacomputer.api.models.UserDetailResponse
import com.avinashpatil.app.dishacomputer.data.store.TokenManager
import com.avinashpatil.app.dishacomputer.data.store.UserPreferences
import com.avinashpatil.app.dishacomputer.repository.UserRepository
import com.avinashpatil.app.dishacomputer.utils.ErrorUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ModernProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authApi: AuthApi,
    private val userPreferences: UserPreferences,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ModernProfileUiState>(ModernProfileUiState.Loading)
    val uiState: StateFlow<ModernProfileUiState> = _uiState.asStateFlow()

    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()

    init {
        refreshProfile()
    }

    fun refreshProfile() {
        observeUserProfile()
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            val userId = userPreferences.getUserId()
            if (userId != null) {
                // Observe local database (Single Source of Truth)
                launch {
                    userRepository.getUserStream(userId).collect { user ->
                        if (user != null) {
                            _uiState.value = ModernProfileUiState.Success(user)
                        } else {
                            // If DB is empty, state remains loading or we can set error/empty state
                            // But usually, refreshUser will fill it soon.
                            if (_uiState.value !is ModernProfileUiState.Success) {
                                _uiState.value = ModernProfileUiState.Loading
                            }
                        }
                    }
                }

                // Trigger API refresh
                launch {
                    val result = userRepository.refreshUser(userId)
                    result.onFailure { e ->
                        if (_uiState.value !is ModernProfileUiState.Success) {
                            _uiState.value = ModernProfileUiState.Error(ErrorUtils.getFriendlyErrorMessage(e))
                        }
                    }
                }
            } else {
                _uiState.value = ModernProfileUiState.Error("User ID not found")
            }
        }
    }

    fun onImageSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            val userId = userPreferences.getUserId() ?: return@launch
            try {
                // Get existing user to find old image path
                val existingUser = userRepository.getUserStream(userId).firstOrNull()?.let { 
                    // This returns UserDetailResponse, but we need the raw entity or just the path.
                    // Actually getUserStream returns UserDetailResponse which has the path if we mapped it.
                    // Let's check UserDetailResponse mapping.
                    // Wait, getUserStream maps to UserDetailResponse. Does UserDetailResponse have localProfileImagePath?
                    // Yes, I added it.
                    it.localProfileImagePath
                }

                if (existingUser != null) {
                    val oldFile = File(existingUser)
                    if (oldFile.exists()) {
                        oldFile.delete()
                    }
                }

                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = "profile_${userId}_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                val outputStream = FileOutputStream(file)

                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                val localPath = file.absolutePath
                userRepository.updateLocalProfileImage(userId, localPath)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error if needed
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = LogoutState.Loading
            try {
                val refreshToken = tokenManager.getRefreshToken()
                if (!refreshToken.isNullOrEmpty()) {
                    authApi.logout(RefreshTokenRequest(refreshToken))
                }
                // Always clear local data
                userPreferences.logout()
                tokenManager.clearTokens()
                _logoutState.value = LogoutState.Success
            } catch (e: Exception) {
                // Even if API fails, we clear local session
                userPreferences.logout()
                tokenManager.clearTokens()
                _logoutState.value = LogoutState.Success
            }
        }
    }

    fun resetLogoutState() {
        _logoutState.value = LogoutState.Idle
    }
}

sealed class ModernProfileUiState {
    object Loading : ModernProfileUiState()
    data class Success(val user: UserDetailResponse) : ModernProfileUiState()
    data class Error(val message: String) : ModernProfileUiState()
}

sealed class LogoutState {
    object Idle : LogoutState()
    object Loading : LogoutState()
    object Success : LogoutState()
    data class Error(val message: String) : LogoutState()
}
