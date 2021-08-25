package com.kyonggi.randhand_chat.Database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "chatroom_list")
data class ChatRoomTable(
    @PrimaryKey
    val sessionId: String,
    val toUser: String,
    var userName: String,
    var userImage: String?,
    var syncTime: LocalDateTime,
    val type: String?,
    var prefMessage: String?
)