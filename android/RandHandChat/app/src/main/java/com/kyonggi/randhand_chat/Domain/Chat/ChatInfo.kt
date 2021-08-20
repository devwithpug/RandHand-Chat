package com.kyonggi.randhand_chat.Domain.Chat

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class ChatInfo(
    @SerializedName("sessionId")
    val sessionId: String,
    @SerializedName("syncTime")
    val syncTime: String,
    @SerializedName("userIds")
    val userIds: List<String>
)
