package com.avinashpatil.app.dishacomputer.repository

import com.avinashpatil.app.dishacomputer.api.models.TopicDto
import com.avinashpatil.app.dishacomputer.data.AppDatabase
import com.avinashpatil.app.dishacomputer.data.dao.TopicsDao
import com.avinashpatil.app.dishacomputer.data.model.toEntity
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicsRepository @Inject constructor(
    private val topicsDao: TopicsDao,
    private val courseRepository: CourseRepository,
    private val appDatabase: AppDatabase
) {
    fun getTopics(courseIds: List<String>): Flow<List<TopicDto>> {
        return topicsDao.getTopicsByCourseIds(courseIds).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun refreshTopics(courseIds: List<String>) {
        val combinedTopics = mutableListOf<TopicDto>()
        courseIds.forEach { id ->
            courseRepository.getTopicsByCourseId(id).onSuccess { topics ->
                combinedTopics.addAll(topics)
            }
        }
        
        if (combinedTopics.isNotEmpty()) {
             appDatabase.withTransaction {
                 topicsDao.deleteByCourseIds(courseIds)
                 topicsDao.insertAll(combinedTopics.map { it.toEntity() })
             }
        }
    }
}
