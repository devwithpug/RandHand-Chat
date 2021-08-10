package com.kyonggi.randhand_chat.Retrofit.Service

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton 으로 설정
object ServiceUser {
    // Base Url
    private const val BASE_URL = "http://3.36.37.197:8000/chat-service/"
    // Retrofit 선언
    private lateinit var retrofitUser: Retrofit
    private var gson = GsonBuilder().setLenient().create()

    // getInstance 함수로 Retrofit 반환
    fun getInstance(): Retrofit {
        retrofitUser = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        return retrofitUser
    }
}
