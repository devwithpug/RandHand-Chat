package com.kyonggi.randhand_chat.Retrofit

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GestureServiceURL {
    // Base Url
    private const val BASE_URL = "http://3.36.37.197:8000/gesture-service/"
    // Retrofit 선언
    private lateinit var retrofitGesture: Retrofit
    private var gson = GsonBuilder().setLenient().create()

    // getInstance 함수로 Retrofit 반환
    fun getInstance(): Retrofit {
        retrofitGesture = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        return retrofitGesture
    }
}