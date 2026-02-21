package com.avinashpatil.app.dishacomputer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.avinashpatil.app.dishacomputer.api.models.TopicDto

@Entity(tableName = "topics")
data class TopicsEntity(
    @PrimaryKey val topicId: String,
    val topicName: String,
    val courseId: String,
    val message: String? = null
) {
    fun toDomainModel(): TopicDto {
        return TopicDto(
            topicId = topicId,
            topicName = topicName,
            courseId = courseId
        )
    }
}

fun TopicDto.toEntity(): TopicsEntity {
    return TopicsEntity(
        topicId = topicId ?: "",
        topicName = topicName ?: "",
        courseId = courseId ?: "",
        message = null
    )
}
