package com.kyonggi.randhand_chat.Retrofit.IRetrofit

import com.kyonggi.randhand_chat.Domain.Chat.ChatInfo
import com.kyonggi.randhand_chat.Domain.Message.SyncInfo
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*
import java.time.LocalDateTime

interface IRetrofitChat {
    // 추가되는 url를 넣는다
    // 채팅방 정보요청(sessionId)
    @GET("chats/{sessionId}")
    fun getChatRoomInfoBySessionId(
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String,
        @Path("sessionId") sessionId: String
    ): Call<ChatInfo>

    // 채팅방 정보요청(userId)
    @GET("chats/session")
    fun getChatRoomInfoByUserId(
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String
    ): Call<List<ChatInfo>>

    // 채팅방 나가기 요청
    @GET("chats/leave")
    fun removeChatRoom(
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String,
    ) : Call<Void>

    // 채팅방 동기화
    @GET("chats/sync")
    fun syncMessages(
        // 앱에서의 sync time
        @Query("date") date: String,
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String,
        @Header("sessionId") session: String
    ) : Call<SyncInfo>

    // 채팅방 이미지보내기 요청
    @POST
    fun sendImageMessage(
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String,
        @Part image: MultipartBody.Part
    ) : Call<Void>
}