package com.avinashpatil.app.dishacomputer.api.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class UserDetailResponse(
    val userId: String? = null,
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
    @SerializedName("lock")
    val lock: Boolean? = null,
    val courseIds: List<String>? = null,
    val batchIds: List<String>? = null,
    val certificateIds: List<String>? = null,
    val localProfileImagePath: String? = null
)
