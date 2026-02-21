package com.avinashpatil.app.dishacomputer.api

import com.avinashpatil.app.dishacomputer.api.models.*
import com.avinashpatil.app.dishacomputer.data.model.ExamResult
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // User Management
    @GET("api/users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): Response<UserEnrollmentRefsDto>
    
    @GET("api/users/all")
    suspend fun getAllUsers(): Response<List<User>>
    
    @POST("api/users/register")
    suspend fun registerUser(@Body user: User): Response<User>
    
    // Course Management
    @GET("api/courses")
    suspend fun getAllCourses(): Response<List<Course>>
    
    @GET("api/courses/{courseId}")
    suspend fun getCourseById(@Path("courseId") courseId: String): Response<Course>
    
    @POST("api/courses")
    suspend fun createCourse(@Body course: Course): Response<Course>
    
    // Batch Management
    @GET("api/batches/{batchId}")
    suspend fun getBatchById(@Path("batchId") batchId: String): Response<Batch>
    
    @POST("api/batches")
    suspend fun createBatch(@Body batch: Batch): Response<Batch>
    
    // Quiz Management
    @GET("api/quizzes/batch/{batchId}")
    suspend fun getQuizzesByBatchId(@Path("batchId") batchId: String): Response<List<QuizDto>>
    
    @GET("api/quizzes/{quizId}")
    suspend fun getQuizById(@Path("quizId") quizId: String): Response<QuizDto>
    
    @POST("api/quizzes")
    suspend fun createQuiz(@Body quiz: QuizDto): Response<QuizDto>
    
    // Question Management
    @GET("api/questions/quiz/{quizId}")
    suspend fun getQuestionsByQuizId(@Path("quizId") quizId: String): Response<List<QuizQuestionDTO>>
    
    @GET("api/questions/course/{courseId}")
    suspend fun getQuestionsByCourseId(@Path("courseId") courseId: String): Response<List<CourseQuestionsDto>>

    @GET("api/questions/courses/topics/{topicId}")
    suspend fun getQuestionsByTopicId(@Path("topicId") topicId: String): Response<List<QuestionDto>>
    
    @GET("api/questions/courses/{courseId}/topics/questions")
    suspend fun getCourseTopicsQuestions(@Path("courseId") courseId: String): Response<CourseTopicsResponse>

    @GET("api/quizzes/courses/quizzes/{quizId}")
    suspend fun getFullQuizById(@Path("quizId") quizId: String): Response<FullQuizDto>

    @GET("api/quizzes/courses/{courseId}")
    suspend fun getQuizzesByCourseId(@Path("courseId") courseId: String): Response<List<QuizDto>>
    
    @GET("api/questions/{questionId}")
    suspend fun getQuestionById(@Path("questionId") questionId: String): Response<QuestionDto>
    
    @POST("api/questions")
    suspend fun createQuestion(@Body question: QuestionDto): Response<QuestionDto>
    
    // Authentication
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @POST("api/auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequest): Response<Unit>
    
    @POST("api/auth/regenerate-token")
    suspend fun regenerateToken(@Body refreshTokenRequest: RefreshTokenRequest): Response<LoginResponse>
    
    // Quiz Submission
    @POST("api/quizzes/submit-quiz")
    suspend fun submitQuiz(@Body submission: StudentQuizSubmission): Response<QuizResult>
    
    // Exam Results
    @GET("api/exams/result/{examId}")
    suspend fun getExamResult(@Path("examId") examId: String): Response<com.avinashpatil.app.dishacomputer.data.model.ExamResult>
    
    // Leaderboard
    @GET("api/leaderboard")
    suspend fun getLeaderboard(): Response<List<LeaderboardUser>>
    
    // Certificate Management
    @GET("api/certificates/user/{userId}")
    suspend fun getCertificatesByUserId(@Path("userId") userId: String): Response<List<Certificate>>
    
    @GET("api/certificates/{certificateId}")
    suspend fun getCertificateById(@Path("certificateId") certificateId: String): Response<Certificate>
    
    @POST("api/certificates")
    suspend fun createCertificate(@Body certificate: Certificate): Response<Certificate>
}
