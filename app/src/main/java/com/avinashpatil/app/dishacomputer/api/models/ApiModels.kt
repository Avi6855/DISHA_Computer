package com.avinashpatil.app.dishacomputer.api.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

// Authentication Models
@Serializable
data class LoginRequest(
    val email: String? = null,
    val dishaId: String? = null,
    val rememberMe: Boolean = false,
    val deviceName: String? = null
)

@Serializable
data class LoginResponse(
    val token: String? = null,
    val user: UserDTO? = null,
    val refreshToken: RefreshTokenDto? = null
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class RefreshTokenDto(
    val id: Int,
    val token: String,
    val expiryDate: String
)

// User Models
@Serializable
data class UserDTO(
    val userId: String? = null,
    val dishaId: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val accountNonLocked: Boolean? = null,
    @SerializedName("lock")
    val isLocked: Boolean? = null,
    val status: Boolean? = null,
    val accountStatus: String? = null,
    val batches: List<Batch> = emptyList(),
    val certificates: List<Certificate> = emptyList(),
    val roles: List<Role> = emptyList()
)

@Serializable
data class UserEnrollmentRefsDto(
    val userId: String? = null,
    val courseIds: List<String>? = null,
    val batchIds: List<String> = emptyList()
)

@Serializable
data class User(
    val userId: String,
    val dishaId: String,
    val fullName: String,
    val email: String,
    val accountNonLocked: Boolean,
    val batches: List<Batch>,
    val certificates: List<Certificate>,
    val roles: List<Role>
)

@Serializable
data class Role(
    val roleId: String,
    val name: String
)

// Course Models
@Serializable
data class Course(
    val courseId: String,
    val courseName: String,
    val courseDescription: String? = null,
    val courseDuration: String,
    val coursePrice: Double,
    val courseOfferPrice: Double? = null,
    val batches: List<BatchDto>
)

@Serializable
data class TopicDto(
    val topicId: String? = null,
    val topicName: String? = null,
    val courseId: String? = null,
    val questions: List<TopicQuestionsDto> = emptyList()
)

@Serializable
data class TopicQuestionsDto(
    val topicQuestionId: String? = null,
    val topicId: String? = null,
    val questionId: String? = null,
    val question: QuestionDto? = null
)

@Serializable
data class CourseTopicsResponse(
    val courseId: String? = null,
    val topics: List<TopicWithQuestionsDto> = emptyList()
)

@Serializable
data class TopicWithQuestionsDto(
    val topicId: String? = null,
    val topicName: String? = null,
    val questions: List<QuestionDto> = emptyList()
)

@Serializable
data class CourseQuestionsDto(
    val courseQuestionId: String? = null,
    val courseId: String? = null,
    val questionId: String? = null,
    val question: QuestionDto? = null
)

@Serializable
data class CourseDto(
    val courseId: String? = null,
    val courseName: String,
    val courseDescription: String? = null,
    val courseDuration: String,
    val coursePrice: Double,
    val courseOfferPrice: Double? = null,
    val batches: List<BatchDto> = emptyList()
)

// Batch Models
@Serializable
data class Batch(
    val batchId: String,
    val batchNo: String,
    val startDate: String,
    val endDate: String,
    val timing: String,
    val courseId: String,
    val studentIds: List<String>,
    val batchUsersList: BatchUsersList
)

@Serializable
data class BatchDto(
    val batchId: String? = null,
    val batchNo: String,
    val startDate: String,
    val endDate: String,
    val timing: String,
    val courseId: String,
    val studentIds: List<String> = emptyList(),
    val batchUsersList: BatchUsersList? = null
)

@Serializable
data class LocalTime(
    val hour: Int,
    val minute: Int,
    val second: Int = 0,
    val nano: Int = 0
)

@Serializable
data class BatchUser(
    val fullName: String
)

@Serializable
data class BatchUsersList(
    val batchId: String,
    val batchUsers: List<BatchUser>
)

// Quiz Models
@Serializable
data class QuizDto(
    val quizId: String? = null,
    val title: String? = null,
    val durationMinutes: Int? = null,
    val scheduledTime: String? = null,
    val scheduledDate: String? = null,
    val courseId: String? = null,
    val topicId: String? = null,
    val sample: Boolean? = null,
    val attempted: Boolean? = null,
    val questions: List<QuizQuestionDTO> = emptyList()
)

@Serializable
data class FullQuizDto(
    val quizId: String? = null,
    val title:  String? = null,
    val durationMinutes: Int? = null,
    val scheduledTime: String? = null,
    val scheduledDate: String? = null,
    val courseId: String? = null,
    val topicId: String? = null,
    val sample: Boolean? = null,
    val attempted: Boolean? = null,
    val questions: List<QuestionDto> = emptyList()
)

@Serializable
data class QuizQuestionDTO(
    val quizQuestionId: String? = null,
    val quizId: String? = null,
    val questionId: String? = null,
    val question: QuestionDto? = null
)

@Serializable
data class QuizScheduleDto(
    val quizId: String,
    val courseId: String,
    val scheduledTime: String? = null
)

// Question Models
@Serializable
data class QuestionDto(
    val questionId: String? = null,
    val questionEnglish: String? = null,
    val questionMarathi: String? = null,
    val option1English: String? = null,
    val option1Marathi: String? = null,
    val option2English: String? = null,
    val option2Marathi: String? = null,
    val option3English: String? = null,
    val option3Marathi: String? = null,
    val option4English: String? = null,
    val option4Marathi: String? = null,
    val correctOption: Int? = null,
    val marks: Int? = null,
    val courseId: String? = null,
    val topicId: String? = null
)

// Quiz Submission Models
@Serializable
data class StudentQuizSubmission(
    val quizId: String,
    val studentId: String,
    val solvedQuestionIds: List<String> = emptyList(),
    val unsolvedQuestionIds: List<String> = emptyList(),
    val answers: Map<String, Int> = emptyMap()
)

@Serializable
data class QuizResult(
    val quizId: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val score: Int,
    val message: String
)

// Certificate Models
@Serializable
data class Certificate(
    val certificateId: String,
    val certificateNumber: String,
    val issueDate: String,
    val userId: String,
    val courseId: String,
    val batchId: String
)

@Serializable
data class CertificateDTO(
    val certificateId: String? = null,
    val certificateNumber: String? = null,
    val issueDate: String,
    val userId: String,
    val courseId: String,
    val batchId: String
)

// Enrollment Models
@Serializable
data class EnrollmentDTO(
    val enrollmentId: String,
    val enrollmentDateTime: String,
    val userName: String,
    val email: String,
    val courseName: String,
    val batchName: String,
    val finalPrice: String,
    val paidAmount: String,
    val paymentStatus: String
)

@Serializable
data class EnrollmentDetails(
    val enrolledCourse: String,
    val assignedBatch: String
)

@Serializable
data class UserAllDetails(
    val userId: String,
    val userName: String,
    val enrollments: List<EnrollmentDetails>? = emptyList(),
    val dishaId: String
)
