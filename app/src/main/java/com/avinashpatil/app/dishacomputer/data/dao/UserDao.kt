package com.avinashpatil.app.dishacomputer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.avinashpatil.app.dishacomputer.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao {
    @Query("SELECT * FROM user_details WHERE userId = :userId")
    abstract fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM user_details WHERE userId = :userId")
    @JvmSuppressWildcards
    abstract suspend fun getUserSnapshot(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    abstract suspend fun insertUser(user: UserEntity): Long

    @Query("DELETE FROM user_details WHERE userId = :userId")
    @JvmSuppressWildcards
    abstract suspend fun deleteUser(userId: String): Int

    @Query("UPDATE user_details SET localProfileImagePath = :path WHERE userId = :userId")
    @JvmSuppressWildcards
    abstract suspend fun updateProfileImage(userId: String, path: String): Int
}
