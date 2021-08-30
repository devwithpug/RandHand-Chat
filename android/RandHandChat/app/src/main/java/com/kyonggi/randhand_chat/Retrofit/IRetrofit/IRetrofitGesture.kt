package com.kyonggi.randhand_chat.Retrofit.IRetrofit

import retrofit2.Call
import retrofit2.http.*

interface IRetrofitGesture {
    // 추가되는 url를 넣는다
    // 채팅 매칭 생성 요청
    @POST("queue")
    fun sendGestureMatching(
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String,
        @Body gesture: Map<String, String>
    ): Call<Void>

    // 채팅 매칭 취소
    @POST("queue/cancel")
    fun getCancelMatching(
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String
    ): Call<Void>
}