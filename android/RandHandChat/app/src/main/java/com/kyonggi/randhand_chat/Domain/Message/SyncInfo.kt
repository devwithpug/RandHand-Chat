package com.kyonggi.randhand_chat.Domain.Message

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class SyncInfo(
    @SerializedName("syncTime")
    val syncTime: String,
    @SerializedName("message")
    val messageList: List<MessageInfo>
)
