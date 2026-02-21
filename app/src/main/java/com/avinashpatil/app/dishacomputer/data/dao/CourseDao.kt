package com.avinashpatil.app.dishacomputer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.avinashpatil.app.dishacomputer.data.model.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CourseDao {
    @Query("SELECT * FROM courses")
    abstract fun getAllCourses(): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    abstract suspend fun insertAll(courses: List<CourseEntity>): List<Long>

    @Query("DELETE FROM courses")
    @JvmSuppressWildcards
    abstract suspend fun clearAll(): Int
}
