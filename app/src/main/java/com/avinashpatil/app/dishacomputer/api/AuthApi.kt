package com.avinashpatil.app.dishacomputer.api

import com.avinashpatil.app.dishacomputer.api.models.RefreshTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequest): Response<Unit>
}
