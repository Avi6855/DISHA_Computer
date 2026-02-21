package com.avinashpatil.app.dishacomputer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.avinashpatil.app.dishacomputer.data.model.TopicsEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TopicsDao {
    @Query("SELECT * FROM topics ORDER BY topicName ASC")
    abstract fun getAllTopics(): Flow<List<TopicsEntity>>

    @Query("SELECT * FROM topics WHERE courseId IN (:courseIds) ORDER BY topicName ASC")
    abstract fun getTopicsByCourseIds(courseIds: List<String>): Flow<List<TopicsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    abstract suspend fun insertAll(topics: List<TopicsEntity>): List<Long>

    @Query("DELETE FROM topics WHERE courseId IN (:courseIds)")
    @JvmSuppressWildcards
    abstract suspend fun deleteByCourseIds(courseIds: List<String>): Int

    @Query("DELETE FROM topics")
    @JvmSuppressWildcards
    abstract suspend fun clearAll(): Int
}
