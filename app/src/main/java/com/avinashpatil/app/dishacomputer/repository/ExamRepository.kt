package com.avinashpatil.app.dishacomputer.repository

import com.avinashpatil.app.dishacomputer.api.ApiService
import com.avinashpatil.app.dishacomputer.api.models.LeaderboardUser
import com.avinashpatil.app.dishacomputer.data.model.ExamResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

@Singleton
class ExamRepository @Inject constructor(
    private val apiService: ApiService
) {
    private val localResults = ConcurrentHashMap<String, ExamResult>()

    fun putLocalResult(examId: String, result: ExamResult) {
        localResults[examId] = result
    }

    suspend fun getExamResult(examId: String): Result<ExamResult> = withContext(Dispatchers.IO) {
        try {
            if (examId.startsWith("local_")) {
                val local = localResults[examId]
                if (local != null) {
                    return@withContext kotlin.Result.success(local)
                }
                return@withContext kotlin.Result.failure(Exception("Local exam result not found"))
            }

            val response = apiService.getExamResult(examId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    kotlin.Result.success(body)
                } else {
                    kotlin.Result.failure(Exception("Empty response body"))
                }
            } else {
                kotlin.Result.failure(Exception("Failed to fetch exam result: ${response.code()}"))
            }
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }

    suspend fun getLeaderboard(): Result<List<LeaderboardUser>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLeaderboard()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        kotlin.Result.success(body)
                    } else {
                        kotlin.Result.failure(Exception("Empty response body"))
                    }
                } else {
                    kotlin.Result.failure(Exception("Failed to fetch leaderboard: ${response.code()}"))
                }
            } catch (e: Exception) {
                kotlin.Result.failure(e)
            }
        }
}
