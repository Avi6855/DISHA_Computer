package com.avinashpatil.app.dishacomputer.api

import com.avinashpatil.app.dishacomputer.api.models.UserAllDetails
import com.avinashpatil.app.dishacomputer.api.models.UserDetailResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApiService {
    @GET("api/users/all")
    suspend fun getAllUsers(): Response<List<UserAllDetails>>

    @GET("api/users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): Response<UserDetailResponse>
}
