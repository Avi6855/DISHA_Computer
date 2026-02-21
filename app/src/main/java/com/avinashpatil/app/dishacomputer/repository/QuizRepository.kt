package com.avinashpatil.app.dishacomputer.repository

import androidx.room.withTransaction
import com.avinashpatil.app.dishacomputer.api.ApiService
import com.avinashpatil.app.dishacomputer.api.models.Course
import com.avinashpatil.app.dishacomputer.api.models.QuestionDto as ApiQuestion
import com.avinashpatil.app.dishacomputer.api.models.QuizDto as ApiQuiz
import com.avinashpatil.app.dishacomputer.api.models.FullQuizDto
import com.avinashpatil.app.dishacomputer.api.models.StudentQuizSubmission
import com.avinashpatil.app.dishacomputer.api.models.QuizResult
import com.avinashpatil.app.dishacomputer.data.AppDatabase
import com.avinashpatil.app.dishacomputer.data.dao.QuizDao
import com.avinashpatil.app.dishacomputer.data.model.toEntity
import com.avinashpatil.app.dishacomputer.data.store.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

import com.avinashpatil.app.dishacomputer.data.dao.ExamAnswerDao
import com.avinashpatil.app.dishacomputer.data.model.ExamAnswerEntity

@Singleton
class QuizRepository @Inject constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences,
    private val database: AppDatabase,
    private val quizDao: QuizDao,
    private val examAnswerDao: ExamAnswerDao
) {
    // Exam Answer Persistence
    suspend fun saveAnswerLocally(quizId: String, questionId: String, selectedOption: Int) {
        withContext(Dispatchers.IO) {
            examAnswerDao.insertAnswer(
                ExamAnswerEntity(
                    quizId = quizId,
                    questionId = questionId,
                    selectedOption = selectedOption,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun getLocalAnswers(quizId: String): List<ExamAnswerEntity> {
        return withContext(Dispatchers.IO) {
            examAnswerDao.getAnswersForQuiz(quizId)
        }
    }

    suspend fun clearLocalAnswers(quizId: String) {
        withContext(Dispatchers.IO) {
            examAnswerDao.clearAnswersForQuiz(quizId)
        }
    }

    data class FinalExamResolution(
        val quiz: ApiQuiz? = null,
        val userCourseId: String? = null,
        val quizCourseId: String? = null
    )

    suspend fun getRandomCccQuestions(limit: Int): Result<Pair<String, List<ApiQuestion>>> =
        withContext(Dispatchers.IO) {
            try {
                val courseId = resolveCccCourseId()
                    ?: return@withContext Result.failure(Exception("CCC course not found"))

                val resp = apiService.getQuestionsByCourseId(courseId)
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(Exception("API call failed with code: ${resp.code()}"))
                }

                val body = resp.body().orEmpty()
                if (body.isEmpty()) {
                    return@withContext Result.failure(Exception("No questions available for this course"))
                }

                val questions = body.mapNotNull { it.question }
                Result.success(courseId to questions.shuffled().take(limit))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun resolveFinalExam(): Result<FinalExamResolution> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(resolveFinalExamViaUserEnrollmentRefs())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getQuestionsByQuizId(quizId: String): Result<List<ApiQuestion>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = apiService.getQuestionsByQuizId(quizId)
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(Exception("API call failed with code: ${resp.code()}"))
                }
                val questions = resp.body().orEmpty().mapNotNull { it.question }
                Result.success(questions)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getQuestionsByCourseId(courseId: String): Result<List<ApiQuestion>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = apiService.getQuestionsByCourseId(courseId)
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(Exception("API call failed with code: ${resp.code()}"))
                }
                val questions = resp.body().orEmpty().mapNotNull { it.question }
                Result.success(questions)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getQuestionsByTopicId(topicId: String): Result<List<ApiQuestion>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = apiService.getQuestionsByTopicId(topicId)
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(Exception("API call failed with code: ${resp.code()}"))
                }
                val questions = resp.body().orEmpty()
                Result.success(questions)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getCourseTopicsQuestions(courseId: String): Result<List<ApiQuestion>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = apiService.getCourseTopicsQuestions(courseId)
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(Exception("API call failed with code: ${resp.code()}"))
                }
                val body = resp.body()
                if (body == null) {
                    return@withContext Result.failure(Exception("Empty response body"))
                }
                // Flatten all questions from all topics
                val allQuestions = body.topics.flatMap { it.questions }
                Result.success(allQuestions)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getFullQuizById(quizId: String): Result<FullQuizDto> = withContext(Dispatchers.IO) {
        try {
            val resp = apiService.getFullQuizById(quizId)
            if (!resp.isSuccessful) {
                return@withContext Result.failure(Exception("API call failed with code: ${resp.code()}"))
            }
            val body = resp.body() ?: return@withContext Result.failure(Exception("Empty response body"))
            Result.success(body)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getQuizzesStream(courseId: String): Flow<List<ApiQuiz>> {
        return quizDao.getQuizzesByCourseId(courseId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun refreshQuizzes(courseId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val resp = apiService.getQuizzesByCourseId(courseId)
            if (!resp.isSuccessful) {
                return@withContext Result.failure(Exception("API call failed with code: ${resp.code()}"))
            }
            val quizzes = resp.body().orEmpty()
            
            database.withTransaction {
                quizDao.deleteByCourseId(courseId)
                quizDao.insertAll(quizzes.map { it.toEntity() })
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQuizzesByCourseId(courseId: String): Result<List<ApiQuiz>> = withContext(Dispatchers.IO) {
        try {
            val resp = apiService.getQuizzesByCourseId(courseId)
            if (!resp.isSuccessful) {
                return@withContext Result.failure(Exception("API call failed with code: ${resp.code()}"))
            }
            val body = resp.body().orEmpty()
            Result.success(body)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQuizById(quizId: String): Result<ApiQuiz> = withContext(Dispatchers.IO) {
        try {
            val resp = apiService.getQuizById(quizId)
            if (!resp.isSuccessful) {
                return@withContext Result.failure(Exception("API call failed with code: ${resp.code()}"))
            }
            val body = resp.body()
            if (body == null) {
                return@withContext Result.failure(Exception("Empty response body"))
            }
            Result.success(body)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitQuiz(
        quizId: String,
        answers: Map<String, Int>,
        solvedQuestionIds: List<String>,
        unsolvedQuestionIds: List<String>
    ): Result<QuizResult> = withContext(Dispatchers.IO) {
        try {
            val studentId = userPreferences.getUserId().orEmpty()
            val submission = StudentQuizSubmission(
                quizId = quizId,
                studentId = studentId,
                solvedQuestionIds = solvedQuestionIds,
                unsolvedQuestionIds = unsolvedQuestionIds,
                answers = answers
            )

            val resp = apiService.submitQuiz(submission)
            if (!resp.isSuccessful) {
                val msg = resp.errorBody()?.string()?.take(500).orEmpty()
                val suffix = if (msg.isBlank()) "" else " - $msg"
                return@withContext Result.failure(Exception("Submit failed with code: ${resp.code()}$suffix"))
            }
            val body = resp.body()
            if (body == null) {
                return@withContext Result.failure(Exception("Empty response body"))
            }
            Result.success(body)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isQuizSubmitted(quizId: String): Boolean = withContext(Dispatchers.IO) {
        // Placeholder for local submission check.
        // In a real offline-first app, we'd check the local database for a 'submitted' flag or result.
        // For now, we return false to allow the ViewModel to proceed to the backend check.
        // Future: return quizDao.isSubmitted(quizId)
        false
    }

    private suspend fun resolveCccCourseId(): String? {
        val resp = apiService.getAllCourses()
        if (!resp.isSuccessful) return null
        val courses: List<Course> = resp.body().orEmpty()
        if (courses.isEmpty()) return null

        val ccc = courses.firstOrNull { it.courseName.contains("CCC", ignoreCase = true) }
        return (ccc ?: courses.first()).courseId
    }

    private suspend fun resolveFinalExamViaUserEnrollmentRefs(): FinalExamResolution {
        val userId = userPreferences.getUserId().orEmpty()
        if (userId.isBlank()) return FinalExamResolution()

        val userResp = apiService.getUserById(userId)
        if (!userResp.isSuccessful) return FinalExamResolution()
        val user = userResp.body() ?: return FinalExamResolution()

        val batchId = user.batchIds.firstOrNull().orEmpty()
        if (batchId.isBlank()) return FinalExamResolution()

        val batchResp = apiService.getBatchById(batchId)
        if (!batchResp.isSuccessful) return FinalExamResolution()
        val batch = batchResp.body() ?: return FinalExamResolution()
        val userCourseId = batch.courseId

        val quizzesResp = apiService.getQuizzesByBatchId(batchId)
        if (!quizzesResp.isSuccessful) return FinalExamResolution(userCourseId = userCourseId)
        val quizzes = quizzesResp.body().orEmpty()

        val finalExamQuiz = quizzes.firstOrNull { it.sample != true }
            ?: return FinalExamResolution(userCourseId = userCourseId)

        val quizId = finalExamQuiz.quizId ?: return FinalExamResolution(userCourseId = userCourseId)

        val quizResp = apiService.getQuizById(quizId)
        if (!quizResp.isSuccessful) return FinalExamResolution(userCourseId = userCourseId)
        val fullQuiz = quizResp.body() ?: return FinalExamResolution(userCourseId = userCourseId)

        val quizCourseId = fullQuiz.courseId
        return FinalExamResolution(
            quiz = fullQuiz,
            userCourseId = userCourseId,
            quizCourseId = quizCourseId
        )
    }
}
