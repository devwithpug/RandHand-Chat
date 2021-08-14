package com.kyonggi.randhand_chat.Util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun fromMillisToTimeString(millis: Long): String {
        val format = SimpleDateFormat("hh:mm a", Locale.KOREA)
        return format.format(millis)
    }
}