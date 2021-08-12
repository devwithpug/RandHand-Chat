package com.kyonggi.randhand_chat.Domain.User

import com.google.gson.annotations.SerializedName

data class Client(
    @SerializedName("userId")
    var userId: String?,
    @SerializedName("auth")
    var auth: String?,
    @SerializedName("email")
    var email: String?,
    @SerializedName("name")
    var name: String?,
    @SerializedName("statusMessage")
    var message: String?,
    @SerializedName("picture")
    var picture: String?
)
