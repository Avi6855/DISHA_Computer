package com.avinashpatil.app.dishacomputer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.avinashpatil.app.dishacomputer.api.models.UserDetailResponse

@Entity(tableName = "user_details")
data class UserEntity(
    @PrimaryKey val userId: String,
    val dishaId: String? = null,
    val email: String? = null,
    val fullName: String? = null,
    val role: String? = null,
    val enrollmentId: String? = null,
    val enrollmentDateTime: String? = null,
    val userName: String? = null,
    val courseId: String? = null,
    val courseName: String? = null,
    val courseDescription: String? = null,
    val courseDuration: String? = null,
    val coursePrice: Double? = null,
    val courseOfferPrice: Double? = null,
    val batchId: String? = null,
    val batchName: String? = null,
    val batchNo: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val timing: String? = null,
    val finalPrice: String? = null,
    val paidAmount: String? = null,
    val paymentStatus: String? = null,
    val deviceName: String? = null,
    val lock: Boolean? = null,
    val courseIds: List<String>? = null,
    val batchIds: List<String>? = null,
    val certificateIds: List<String>? = null,
    val localProfileImagePath: String? = null
) {
    fun toDomainModel(): UserDetailResponse {
        return UserDetailResponse(
            userId = userId,
            dishaId = dishaId,
            email = email,
            fullName = fullName,
            role = role,
            enrollmentId = enrollmentId,
            enrollmentDateTime = enrollmentDateTime,
            userName = userName,
            courseId = courseId,
            courseName = courseName,
            courseDescription = courseDescription,
            courseDuration = courseDuration,
            coursePrice = coursePrice,
            courseOfferPrice = courseOfferPrice,
            batchId = batchId,
            batchName = batchName,
            batchNo = batchNo,
            startDate = startDate,
            endDate = endDate,
            timing = timing,
            finalPrice = finalPrice,
            paidAmount = paidAmount,
            paymentStatus = paymentStatus,
            deviceName = deviceName,
            lock = lock,
            courseIds = courseIds,
            batchIds = batchIds,
            certificateIds = certificateIds,
            localProfileImagePath = localProfileImagePath
        )
    }
}

fun UserDetailResponse.toEntity(): UserEntity {
    return UserEntity(
        userId = userId ?: "",
        dishaId = dishaId,
        email = email,
        fullName = fullName,
        role = role,
        enrollmentId = enrollmentId,
        enrollmentDateTime = enrollmentDateTime,
        userName = userName,
        courseId = courseId,
        courseName = courseName,
        courseDescription = courseDescription,
        courseDuration = courseDuration,
        coursePrice = coursePrice,
        courseOfferPrice = courseOfferPrice,
        batchId = batchId,
        batchName = batchName,
        batchNo = batchNo,
        startDate = startDate,
        endDate = endDate,
        timing = timing,
        finalPrice = finalPrice,
        paidAmount = paidAmount,
        paymentStatus = paymentStatus,
        deviceName = deviceName,
        lock = lock,
        courseIds = courseIds,
        batchIds = batchIds,
        certificateIds = certificateIds,
        localProfileImagePath = localProfileImagePath
    )
}
