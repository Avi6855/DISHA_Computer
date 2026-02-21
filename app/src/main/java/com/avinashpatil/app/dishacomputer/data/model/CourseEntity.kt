package com.avinashpatil.app.dishacomputer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.avinashpatil.app.dishacomputer.api.models.BatchDto
import com.avinashpatil.app.dishacomputer.api.models.Course

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val courseId: String,
    val courseName: String,
    val courseDescription: String? = null,
    val courseDuration: String,
    val coursePrice: Double,
    val courseOfferPrice: Double? = null,
    val batches: List<BatchDto> = emptyList()
) {
    fun toDomainModel(): Course {
        return Course(
            courseId = courseId,
            courseName = courseName,
            courseDescription = courseDescription,
            courseDuration = courseDuration,
            coursePrice = coursePrice,
            courseOfferPrice = courseOfferPrice,
            batches = batches
        )
    }
}

fun Course.toEntity(): CourseEntity {
    return CourseEntity(
        courseId = courseId,
        courseName = courseName,
        courseDescription = courseDescription,
        courseDuration = courseDuration,
        coursePrice = coursePrice,
        courseOfferPrice = courseOfferPrice,
        batches = batches
    )
}
