package com.kyonggi.randhand_chat.Database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chatroom_list")
data class ChatRoomTable(
    @PrimaryKey
    val sessionId: String,
    val toUser: String,
    var userName: String,
    var userImage: String?,
    var message: List<MessageTable>
)

@Entity(tableName = "chatroom_message_list")
data class MessageTable (
    @PrimaryKey(autoGenerate = true)
    val messageId : Long? = null,
    val sessionId: String,
    var fromUser: String?,
    val context: String,
    val time: Long
)
