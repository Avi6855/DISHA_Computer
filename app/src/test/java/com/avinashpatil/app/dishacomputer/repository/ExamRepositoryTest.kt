package com.avinashpatil.app.dishacomputer.repository

import com.avinashpatil.app.dishacomputer.api.ApiService
import com.avinashpatil.app.dishacomputer.api.models.LeaderboardUser
import com.avinashpatil.app.dishacomputer.data.model.ExamResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

/**
 * Exam results are either cached in memory on a "local_" id (practice/offline exams) or fetched
 * from the backend. Both paths are asserted so a finished exam can always show its result screen.
 */
class ExamRepositoryTest {

    private val apiService = mockk<ApiService>()

    private fun examResult(obtainedMarks: Int) = ExamResult(
        totalMarks = 100,
        obtainedMarks = obtainedMarks,
        correctAnswers = obtainedMarks / 4,
        wrongAnswers = 1,
        skippedQuestions = 2,
        questions = emptyList(),
        userAnswers = emptyMap()
    )

    private fun errorBody() = "{}".toResponseBody("application/json".toMediaType())

    @Test
    fun `local result is served from cache without calling the api`() = runTest {
        val repository = ExamRepository(apiService)
        val stored = examResult(obtainedMarks = 84)
        repository.putLocalResult("local_101", stored)

        val result = repository.getExamResult("local_101")

        assertEquals(stored, result.getOrNull())
        coVerify(exactly = 0) { apiService.getExamResult(any()) }
    }

    @Test
    fun `unknown local result fails fast`() = runTest {
        val repository = ExamRepository(apiService)

        val result = repository.getExamResult("local_404")

        assertTrue(result.isFailure)
        assertEquals("Local exam result not found", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { apiService.getExamResult(any()) }
    }

    @Test
    fun `server result is returned for a backend exam id`() = runTest {
        coEvery { apiService.getExamResult("exam-1") } returns Response.success(examResult(72))

        val result = ExamRepository(apiService).getExamResult("exam-1")

        assertEquals(72, result.getOrThrow().obtainedMarks)
    }

    @Test
    fun `http error code is reported with the status`() = runTest {
        coEvery { apiService.getExamResult("exam-2") } returns Response.error(500, errorBody())

        val result = ExamRepository(apiService).getExamResult("exam-2")

        assertTrue(result.isFailure)
        assertEquals("Failed to fetch exam result: 500", result.exceptionOrNull()?.message)
    }

    @Test
    fun `empty body is treated as a failure`() = runTest {
        coEvery { apiService.getExamResult("exam-3") } returns Response.success(null)

        val result = ExamRepository(apiService).getExamResult("exam-3")

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `network exception is wrapped instead of crashing the caller`() = runTest {
        coEvery { apiService.getExamResult("exam-4") } throws IOException("no internet")

        val result = ExamRepository(apiService).getExamResult("exam-4")

        assertTrue(result.isFailure)
        assertEquals("no internet", result.exceptionOrNull()?.message)
    }

    @Test
    fun `leaderboard rows are returned on success`() = runTest {
        val row = LeaderboardUser(
            rank = 1,
            score = 900,
            userId = "user-1",
            dishaId = "DISHA-01",
            fullName = "Student One",
            email = "student@example.com",
            accountNonLocked = true,
            batches = emptyList(),
            certificates = emptyList(),
            roles = emptyList()
        )
        coEvery { apiService.getLeaderboard() } returns Response.success(listOf(row))

        val result = ExamRepository(apiService).getLeaderboard()

        assertEquals(listOf(row), result.getOrThrow())
    }

    @Test
    fun `leaderboard failure carries the status code`() = runTest {
        coEvery { apiService.getLeaderboard() } returns Response.error(404, errorBody())

        val result = ExamRepository(apiService).getLeaderboard()

        assertTrue(result.isFailure)
        assertEquals("Failed to fetch leaderboard: 404", result.exceptionOrNull()?.message)
    }
}
