package com.avinashpatil.app.dishacomputer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avinashpatil.app.dishacomputer.api.models.TopicDto
import com.avinashpatil.app.dishacomputer.api.models.UserDetailResponse
import com.avinashpatil.app.dishacomputer.data.store.UserPreferences
import com.avinashpatil.app.dishacomputer.repository.TopicsRepository
import com.avinashpatil.app.dishacomputer.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TopicsUiState {
    object Loading : TopicsUiState()
    data class Success(val topics: List<TopicDto>) : TopicsUiState()
    data class Error(val message: String) : TopicsUiState()
}

@HiltViewModel
class TopicsViewModel @Inject constructor(
    private val topicsRepository: TopicsRepository,
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TopicsUiState> = flow {
        emit(userPreferences.getUserId())
    }.flatMapLatest { userId ->
        if (userId != null) {
            userRepository.getUserStream(userId)
        } else {
            flowOf(null)
        }
    }.flatMapLatest { user ->
        if (user == null) {
            flowOf(TopicsUiState.Loading)
        } else {
            val courseIds = getCourseIds(user)
            if (courseIds.isEmpty()) {
                flowOf(TopicsUiState.Success(emptyList()))
            } else {
                topicsRepository.getTopics(courseIds).combine(_searchQuery) { topics, query ->
                    val filtered = if (query.isBlank()) topics else topics.filter { 
                        it.topicName?.contains(query, ignoreCase = true) == true 
                    }
                    TopicsUiState.Success(filtered)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TopicsUiState.Loading
    )

    init {
        refreshTopics()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
    
    fun refreshTopics() {
        viewModelScope.launch {
            val userId = userPreferences.getUserId() ?: return@launch
            
            // 1. Refresh User (updates DB -> triggers uiState flow)
            try {
                userRepository.refreshUser(userId)
            } catch (e: Exception) {
                // Ignore user refresh error, proceed to try refreshing topics with existing user data
            }

            // 2. Refresh Topics (using latest user data from DB)
            val user = userRepository.getUserStream(userId).firstOrNull()
            if (user != null) {
                val ids = getCourseIds(user)
                if (ids.isNotEmpty()) {
                    try {
                        topicsRepository.refreshTopics(ids)
                    } catch (e: Exception) {
                        // Error handling if needed
                    }
                }
            }
        }
    }

    private fun getCourseIds(user: UserDetailResponse): List<String> {
        val allCourseIds = mutableSetOf<String>()
        user.courseId?.takeIf { it.isNotBlank() }?.let { allCourseIds.add(it) }
        user.courseIds?.filter { it.isNotBlank() }?.let { allCourseIds.addAll(it) }
        return allCourseIds.toList()
    }
}
