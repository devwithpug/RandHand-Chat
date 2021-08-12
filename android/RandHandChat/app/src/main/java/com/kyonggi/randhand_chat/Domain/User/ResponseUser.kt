package com.kyonggi.randhand_chat.Domain.User

import com.google.gson.annotations.SerializedName

data class ResponseUser(
    @SerializedName("userId")
    var userId: String?,
    @SerializedName("email")
    var email: String?,
    @SerializedName("name")
    var name: String?,
    @SerializedName("statusMessage")
    var message: String?,
    @SerializedName("picture")
    var picture: String?,
    @SerializedName("userFriends")
    var userfriends: List<ResponseUser>?,
    @SerializedName("userBlocked")
    var userblocked: List<ResponseUser>?
)