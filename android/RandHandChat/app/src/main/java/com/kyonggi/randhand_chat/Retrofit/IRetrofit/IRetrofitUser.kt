package com.kyonggi.randhand_chat.Retrofit.IRetrofit

import com.kyonggi.randhand_chat.Domain.User.Client
import com.kyonggi.randhand_chat.Domain.User.ResponseUser
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface IRetrofitUser {
    // 추가되는 url를 넣는다
    // 유저 회원가입
    @POST("users/")
    fun signupClient(
        @Body client: Client
    ) : Call<Client>

    // 유저 아이디 조회
    @GET("users/")
    fun requestUserId(
        @Header("auth") auth: String,
        @Header("email") email: String
    ) : Call<Client>

    // 회원정보 변경
    @PUT("users/update")
    fun editClient(
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String,
        @Body client: Client
    ) : Call<ResponseUser>

    // 회원이미지 변경
    @Multipart
    @PUT("users/update/image")
    fun editImage(
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String,
        @Part image: MultipartBody.Part
    ) : Call<ResponseUser>

    // 로그인 확인 -> 토큰을 내부에서 확인하여 로그인 유지하기
    @POST("login")
    fun requestLogin(
        @Body client: Client
    ) : Call<Void>

    //유저 정보 조회
    @GET("users/{userId}")
    fun getUserInfo(
        // Header 에 넣는 userId는 지금 사용자가 누구인지
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String,
        // 조회하고 싶은 userId -> 위의 {}안의 이름과 동일
        @Path("userId") findId: String
    ) : Call<ResponseUser>

    // 친구 목록 조회
    @GET("users/friends")
    fun getUserFriendsList(
        // Header 에 넣는 userId는 지금 사용자가 누구인지
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String
    ) : Call<List<ResponseUser>>

    // 친구 단일 조회
    @GET("users/friends/{friendId}")
    fun getUserFriends(
        // Header 에 넣는 userId는 지금 사용자가 누구인지
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String,
        // 조회하고 싶은 유저 친구목록
        @Path("friendId") friendId: String
    ) : Call<ResponseUser>

    // 친구 추가 요청
    @PATCH("users/friends/{friendId}")
    fun addUser(
        // Header 에 넣는 userId는 지금 사용자가 누구인지
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String,
        // 친구 추가할 friendId
        @Path("friendId") friendId: String
    ) : Call<List<ResponseUser>>

    // 친구 삭제 요청
    @DELETE("users/friends/{friendId}")
    fun deleteUser(
        // Header 에 넣는 userId는 지금 사용자가 누구인지
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String,
        // 친구 추가할 friendId
        @Path("friendId") friendId: String
    ) : Call<List<ResponseUser>>

    // 유저 차단 요청
    @PATCH("users/blacklist/{blockId}")
    fun blockUser(
        // Header 에 넣는 userId는 지금 사용자가 누구인지
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String,
        // 친구 추가할 friendId
        @Path("blockId") blockId: String
    ) : Call<List<ResponseUser>>

    // 차단 목록 조회
    @GET("users/blacklist")
    fun getBlockUserList(
        // Header 에 넣는 userId는 지금 사용자가 누구인지
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String,
    ) : Call<List<ResponseUser>>

    // 차단 유저 단일 조회
    @GET("users/blacklist/{blockId}")
    fun getBlockUser(
        // Header 에 넣는 userId는 지금 사용자가 누구인지
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String,
        // 친구 추가할 friendId
        @Path("blockId") blockId: String
    ) : Call<ResponseUser>

    // 유저 차단 해제 요청
    @DELETE("users/blacklist/{blockId}")
    fun getBlockUserRemove(
        // Header 에 넣는 userId는 지금 사용자가 누구인지
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String,
        // 친구 추가할 friendId
        @Path("blockId") blockId: String
    ) : Call<List<ResponseUser>>

    // 회원 서비스 탈퇴
    @DELETE("users/{userId}")
    fun userWithdrawal(
        @Header("Authorization") userToken: String,
        @Header("userId") userId: String,
        @Path("userId") deleteId: String
    ) : Call<Void>






}