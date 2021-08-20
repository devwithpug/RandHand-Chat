package com.kyonggi.randhand_chat.Domain.Message

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class MessageInfo(
    @SerializedName("type")
    val type: String,
    @SerializedName("fromUser")
    val fromUser: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("createdAt")
    val createdAt: String
)
