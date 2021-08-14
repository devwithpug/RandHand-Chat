package com.kyonggi.randhand_chat.Domain.Message

import java.io.Serializable

data class Message(
    var fromUser: String?,
    val context: String,
    val time: Long
) : Serializable
