package com.avinashpatil.app.dishacomputer.repository

import com.avinashpatil.app.dishacomputer.api.UserApiService
import com.avinashpatil.app.dishacomputer.api.models.UserAllDetails
import com.avinashpatil.app.dishacomputer.api.models.UserDetailResponse
import com.avinashpatil.app.dishacomputer.data.dao.UserDao
import com.avinashpatil.app.dishacomputer.data.model.toEntity
import com.avinashpatil.app.dishacomputer.data.store.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService,
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) {
    fun getUserStream(userId: String): Flow<UserDetailResponse?> {
        return userDao.getUserById(userId).map { it?.toDomainModel() }
    }

    suspend fun refreshUser(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = userApiService.getUserById(userId)
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    // Update lock status
                    user.lock?.let { isLocked ->
                        userPreferences.setUserLockStatus(isLocked)
                    }

                    val existingUser = userDao.getUserSnapshot(userId)
                    val userEntity = user.toEntity()
                    // Preserve local profile image if exists
                    val finalEntity = if (existingUser?.localProfileImagePath != null) {
                        userEntity.copy(localProfileImagePath = existingUser.localProfileImagePath)
                    } else {
                        userEntity
                    }
                    userDao.insertUser(finalEntity)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                // Check if the error body indicates the user is locked
                val errorBody = response.errorBody()?.string()
                if (errorBody?.contains("Account is locked", ignoreCase = true) == true) {
                    userPreferences.setUserLockStatus(true)
                }
                Result.failure(Exception("API call failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(): Result<List<UserAllDetails>> = withContext(Dispatchers.IO) {
        try {
            val response = userApiService.getAllUsers()
            if (response.isSuccessful) {
                val users = response.body()
                if (users != null) {
                    Result.success(users)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                // Check if the error body indicates the user is locked
                val errorBody = response.errorBody()?.string()
                if (errorBody?.contains("Account is locked", ignoreCase = true) == true) {
                    userPreferences.setUserLockStatus(true)
                }
                Result.failure(Exception("API call failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLocalProfileImage(userId: String, path: String) {
        userDao.updateProfileImage(userId, path)
    }

    suspend fun getUserById(userId: String): Result<UserDetailResponse> = withContext(Dispatchers.IO) {
        try {
            val response = userApiService.getUserById(userId)
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    // Update lock status
                    user.lock?.let { isLocked ->
                        userPreferences.setUserLockStatus(isLocked)
                    }

                    val existingUser = userDao.getUserSnapshot(userId)
                    val userEntity = user.toEntity()
                    val finalEntity = if (existingUser?.localProfileImagePath != null) {
                        userEntity.copy(localProfileImagePath = existingUser.localProfileImagePath)
                    } else {
                        userEntity
                    }
                    userDao.insertUser(finalEntity)
                    Result.success(user)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                // Check if the error body indicates the user is locked
                val errorBody = response.errorBody()?.string()
                if (errorBody?.contains("Account is locked", ignoreCase = true) == true) {
                    userPreferences.setUserLockStatus(true)
                }
                Result.failure(Exception("API call failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
