package com.kyonggi.randhand_chat.Database

import androidx.room.TypeConverter
import com.google.gson.Gson

/**
 * List를 데이터베이스에 넣기위한 Converters Class 생성
 */
class Converters {
    @TypeConverter
    fun listToJson(value: List<MessageTable>?): String = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<MessageTable>::class.java).toList()
}