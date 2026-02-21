package com.avinashpatil.app.dishacomputer.api

import com.avinashpatil.app.dishacomputer.api.models.Course
import com.avinashpatil.app.dishacomputer.api.models.TopicDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CourseApiService {
    @GET("api/courses")
    suspend fun getAllCourses(): Response<List<Course>>

    @GET("api/courses/{courseId}")
    suspend fun getCourseById(@Path("courseId") courseId: String): Response<Course>

    @GET("api/courses/{courseId}/topics")
    suspend fun getTopicsByCourseId(@Path("courseId") courseId: String): Response<List<TopicDto>>
}
