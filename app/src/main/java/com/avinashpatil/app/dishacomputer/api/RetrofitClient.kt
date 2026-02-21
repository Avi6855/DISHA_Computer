package com.avinashpatil.app.dishacomputer.api

import com.avinashpatil.app.dishacomputer.api.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // 10.0.2.2 is the special IP for Android emulator to access host machine's localhost
    //private const val BASE_URL = "http://10.0.2.2:8080/"
    //private const val BASE_URL = "http://localhost:8080/"
    //private const val BASE_URL = "http://localhost:9090/"
    //private const val BASE_URL = "http://10.0.2.2:9090/"
    //private const val BASE_URL = "http://192.168.1.2:9090/"
    //private const val BASE_URL = "http://192.168.1.3:9090/"
    //private const val BASE_URL = "http://192.168.1.4:9090/"
    //private const val BASE_URL = "http://192.168.1.5:9090/"
    //private const val BASE_URL = "http://192.168.1.149:9090/"
    //private const val BASE_URL = "http://10.240.170.172:9090/"
    //private const val BASE_URL = "http://10.65.0.172:9090/"
    //private const val BASE_URL = "https://unheritable-precognizantly-sienna.ngrok-free.dev/"
    private const val BASE_URL = "http://203.57.85.108:9090/"
    private const val TIMEOUT_SECONDS = 30L
    private const val MAX_RETRIES = 3

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request()
            var response = chain.proceed(request)
            var retryCount = 0

            while (!response.isSuccessful && retryCount < MAX_RETRIES) {
                retryCount++
                response.close()
                Thread.sleep(2000L * retryCount) // Exponential backoff
                response = chain.proceed(request)
            }
            response
        }
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
