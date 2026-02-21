package com.avinashpatil.app.dishacomputer.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Topic(
    val topicId: String,
    val topicName: String,
    val courseId: String,
    val message: String? = null
)
