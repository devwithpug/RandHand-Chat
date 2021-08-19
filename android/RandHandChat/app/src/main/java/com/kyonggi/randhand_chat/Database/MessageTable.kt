package com.kyonggi.randhand_chat.Database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "chatroom_message_list")
data class MessageTable (
    @PrimaryKey(autoGenerate = true)
    val messageId : Long? = null,
    val sessionId: String,
    var fromUser: String?,
    val type: String,
    val context: String,
    val time: LocalDateTime
)
