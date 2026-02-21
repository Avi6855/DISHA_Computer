package com.avinashpatil.app.dishacomputer.repository

import com.avinashpatil.app.dishacomputer.api.ApiService
import com.avinashpatil.app.dishacomputer.api.models.LoginRequest
import com.avinashpatil.app.dishacomputer.api.models.LoginResponse
import com.avinashpatil.app.dishacomputer.api.models.UserDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {

    data class AuthResult(
        val response: LoginResponse,
        val isLocked: Boolean
    )

    suspend fun login(request: LoginRequest): Result<AuthResult> = withContext(Dispatchers.IO) {
        try {
            val resp = apiService.login(request)
            if (!resp.isSuccessful) {
                val errorBody = resp.errorBody()?.string()
                val errorMessage = try {
                    if (errorBody != null && errorBody.startsWith("{")) {
                        org.json.JSONObject(errorBody).optString("message", "Login failed")
                    } else {
                        errorBody ?: "Login failed: ${resp.code()}"
                    }
                } catch (e: Exception) {
                    "Login failed: ${resp.code()}"
                }
                return@withContext Result.failure(Exception(errorMessage))
            }

            val body = resp.body()
            if (body == null) {
                return@withContext Result.failure(Exception("Empty response body"))
            }

            val user: UserDTO? = body.user
            val locked = deriveLocked(user)

            Result.success(AuthResult(body, locked))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun deriveLocked(user: UserDTO?): Boolean {
        if (user == null) return false
        val statusText = user.accountStatus?.trim()?.uppercase()
        val explicitLocked = user.isLocked == true
        val nonLockedFlag = user.accountNonLocked
        val statusFlag = user.status

        if (explicitLocked) return true
        if (nonLockedFlag == false) return true
        if (statusText == "LOCKED" || statusText == "SUSPENDED" || statusText == "DISABLED") return true
        return false
    }
}

