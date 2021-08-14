package com.kyonggi.randhand_chat.Retrofit.IRetrofit

import com.kyonggi.randhand_chat.Domain.Chat.ChatInfo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface IRetrofitChat {
    // 추가되는 url를 넣는다
    // 채팅방 정보요청(sessionId)
    @GET("chats/{sessionId}")
    fun getChatRoomInfoBySessionId(
        @Path("sessionId") sessionId: String
    ): Call<ChatInfo>

    // 채팅방 정보요청(userId)
    @GET("chats/session")
    fun getChatRoomInfoByUserId(
        @Header("userId") userId: String
    ): Call<ChatInfo>

    // 채팅방 나가기 요청
    @GET("chats/leave")
    fun removeChatRoom(
        @Header("userId") userId: String
    )
}