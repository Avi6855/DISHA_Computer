package com.avinashpatil.app.dishacomputer.repository

import androidx.room.withTransaction
import com.avinashpatil.app.dishacomputer.api.ApiService
import com.avinashpatil.app.dishacomputer.api.CourseApiService
import com.avinashpatil.app.dishacomputer.api.models.Course
import com.avinashpatil.app.dishacomputer.api.models.TopicDto
import com.avinashpatil.app.dishacomputer.data.AppDatabase
import com.avinashpatil.app.dishacomputer.data.dao.CourseDao
import com.avinashpatil.app.dishacomputer.data.model.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepository @Inject constructor(
    private val apiService: ApiService,
    private val courseApiService: CourseApiService,
    private val courseDao: CourseDao,
    private val appDatabase: AppDatabase
) {
    fun getCoursesStream(): Flow<List<Course>> {
        return courseDao.getAllCourses().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun refreshCourses(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllCourses()
            if (response.isSuccessful) {
                val courses = response.body()
                if (courses != null) {
                    appDatabase.withTransaction {
                        courseDao.clearAll()
                        courseDao.insertAll(courses.map { it.toEntity() })
                    }
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("API call failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCourses(): Result<List<Course>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllCourses()
            if (response.isSuccessful) {
                val courses = response.body()
                if (courses != null) {
                    appDatabase.withTransaction {
                        courseDao.clearAll()
                        courseDao.insertAll(courses.map { it.toEntity() })
                    }
                    Result.success(courses)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("API call failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTopicsByCourseId(courseId: String): Result<List<TopicDto>> = withContext(Dispatchers.IO) {
        try {
            val response = courseApiService.getTopicsByCourseId(courseId)
            if (response.isSuccessful) {
                val topics = response.body()
                if (topics != null) {
                    Result.success(topics)
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Exception("API call failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
