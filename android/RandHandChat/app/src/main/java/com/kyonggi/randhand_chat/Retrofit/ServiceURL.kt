package com.kyonggi.randhand_chat.Retrofit

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton 으로 설정
object ServiceURL {
    // Base Url
    private const val BASE_URL = "http://devwithpug-lb-1172533163.ap-northeast-2.elb.amazonaws.com/chat-service/"
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
