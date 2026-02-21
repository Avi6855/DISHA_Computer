package com.avinashpatil.app.dishacomputer.di

import com.avinashpatil.app.dishacomputer.api.ApiService
import com.avinashpatil.app.dishacomputer.api.AuthApi
import com.avinashpatil.app.dishacomputer.api.QuizApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import com.avinashpatil.app.dishacomputer.network.AuthInterceptor
import com.avinashpatil.app.dishacomputer.network.LockInterceptor
import com.avinashpatil.app.dishacomputer.network.TokenAuthenticator
import com.avinashpatil.app.dishacomputer.data.store.TokenManager
import com.avinashpatil.app.dishacomputer.api.UserApiService
import com.avinashpatil.app.dishacomputer.api.CourseApiService

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

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

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(tokenManager: TokenManager): TokenAuthenticator {
        return TokenAuthenticator(tokenManager, BASE_URL)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        lockInterceptor: LockInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(lockInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor { chain ->
                val request = chain.request()
                var response = chain.proceed(request)
                var retryCount = 0
                while (!response.isSuccessful && retryCount < MAX_RETRIES) {
                    retryCount++
                    response.close()
                    Thread.sleep(2000L * retryCount)
                    response = chain.proceed(request)
                }
                response
            }
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideQuizApiService(retrofit: Retrofit): QuizApiService {
        return retrofit.create(QuizApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCourseApiService(retrofit: Retrofit): CourseApiService {
        return retrofit.create(CourseApiService::class.java)
    }
}
