package com.avinashpatil.app.dishacomputer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.dishacomputer.api.models.Course
import com.avinashpatil.app.dishacomputer.api.models.EnrollmentDetails
import com.avinashpatil.app.dishacomputer.api.models.UserAllDetails
import com.avinashpatil.app.dishacomputer.api.models.UserDetailResponse
import com.avinashpatil.app.dishacomputer.data.store.UserPreferences
import com.avinashpatil.app.dishacomputer.data.store.TokenManager
import com.avinashpatil.app.dishacomputer.utils.ErrorUtils
import com.avinashpatil.app.dishacomputer.repository.CourseRepository
import com.avinashpatil.app.dishacomputer.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

data class EnrolledCourseItem(
    val courseId: String,
    val courseName: String,
    val courseDescription: String,
    val batchNo: String,
    val batchTime: String
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _userDetailsState = MutableStateFlow<UiState<UserDetailResponse>>(UiState.Loading)
    val userDetailsState: StateFlow<UiState<UserDetailResponse>> = _userDetailsState.asStateFlow()

    private val _allCoursesState = MutableStateFlow<UiState<List<Course>>>(UiState.Loading)
    val allCoursesState: StateFlow<UiState<List<Course>>> = _allCoursesState.asStateFlow()

    private val _searchQueryState = MutableStateFlow("")
    val searchQueryState: StateFlow<String> = _searchQueryState.asStateFlow()

    private val _filteredCoursesState = MutableStateFlow<List<Course>>(emptyList())
    val filteredCoursesState: StateFlow<List<Course>> = _filteredCoursesState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        loadAllCourses()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _userDetailsState.value = UiState.Loading
            val userId = userPreferences.getUserId()
            if (userId != null) {
                // Observe local data
                launch {
                    userRepository.getUserStream(userId).collect { user ->
                        if (user != null) {
                            _userDetailsState.value = UiState.Success(user)
                        }
                    }
                }
                
                // Refresh data from API
                launch {
                    val result = userRepository.refreshUser(userId)
                    result.onFailure { e ->
                        if (_userDetailsState.value is UiState.Loading) {
                            _userDetailsState.value = UiState.Error(ErrorUtils.getFriendlyErrorMessage(e))
                        }
                    }
                }
            } else {
                _userDetailsState.value = UiState.Error("User ID not found")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQueryState.value = query
        val data = (_allCoursesState.value as? UiState.Success<List<Course>>)?.data.orEmpty()
        _filteredCoursesState.value = data.filter {
            it.courseName.contains(query, ignoreCase = true) ||
            (it.courseDescription ?: "").contains(query, ignoreCase = true)
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            val userId = userPreferences.getUserId()
            if (userId != null) {
                if (_userDetailsState.value is UiState.Error) {
                    _userDetailsState.value = UiState.Loading
                }
                val result = userRepository.refreshUser(userId)
                result.onFailure { e ->
                    if (_userDetailsState.value !is UiState.Success) {
                        _userDetailsState.value = UiState.Error(ErrorUtils.getFriendlyErrorMessage(e))
                    }
                }
            }
        }

        viewModelScope.launch {
            if (_allCoursesState.value is UiState.Error) {
                _allCoursesState.value = UiState.Loading
            }
            val result = courseRepository.refreshCourses()
            result.onFailure { e ->
                val currentState = _allCoursesState.value
                if (currentState !is UiState.Success || currentState.data.isEmpty()) {
                    _allCoursesState.value = UiState.Error(ErrorUtils.getFriendlyErrorMessage(e))
                }
            }
        }
    }

    private fun loadAllCourses() {
        // Observe local data
        viewModelScope.launch {
            courseRepository.getCoursesStream().collect { list ->
                _allCoursesState.value = UiState.Success(list)
                
                val query = _searchQueryState.value
                if (query.isNotEmpty()) {
                    _filteredCoursesState.value = list.filter {
                        it.courseName.contains(query, ignoreCase = true) ||
                        (it.courseDescription ?: "").contains(query, ignoreCase = true)
                    }
                } else {
                    _filteredCoursesState.value = list
                }
            }
        }

        // Refresh data from API
        viewModelScope.launch {
            val result = courseRepository.refreshCourses()
            result.onFailure { e ->
                val currentState = _allCoursesState.value
                if (currentState is UiState.Loading || (currentState is UiState.Success && currentState.data.isEmpty())) {
                    _allCoursesState.value = UiState.Error(ErrorUtils.getFriendlyErrorMessage(e))
                }
            }
        }
    }
}
