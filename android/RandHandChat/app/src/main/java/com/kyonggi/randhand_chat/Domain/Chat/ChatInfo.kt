package com.kyonggi.randhand_chat.Domain.Chat

import com.google.gson.annotations.SerializedName

data class ChatInfo(
    @SerializedName("sessionId")
    val sessionId: String,
    @SerializedName("userIds")
    val userIds: List<String>
)
