package com.kyonggi.randhand_chat.Domain.Chat

import com.kyonggi.randhand_chat.Domain.Message.Message
import java.io.Serializable

data class ChatRoom(
    var userId: String,
    var userName: String,
    var userImage: String?,
    var messageList: List<Message>?
) : Serializable
