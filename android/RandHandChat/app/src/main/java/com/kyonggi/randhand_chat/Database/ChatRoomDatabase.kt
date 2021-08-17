package com.kyonggi.randhand_chat.Database

import android.content.Context
import androidx.room.*

/**
 * ChatRoomTable -> 채팅방관련 데이터베이스
 * MessageTable -> 메시지관련 데이터베이스
 */
@Database(entities = [ChatRoomTable::class, MessageTable::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ChatRoomDatabase : RoomDatabase() {
    abstract fun roomChatRoomDAO() : ChatRoomDAO
    abstract fun roomMessageDAO() : MessageDAO

    companion object {
        private var chatRoomInstance : ChatRoomDatabase? = null

        @Synchronized
        fun getInstance(context : Context): ChatRoomDatabase? {
            if (chatRoomInstance == null) {
                synchronized(ChatRoomDatabase::class) {
                    chatRoomInstance = Room.databaseBuilder(
                        context.applicationContext,
                        ChatRoomDatabase::class.java,
                        "chatroom_db"
                    )
                        /**
                         *  테스트용
                         *  allowMainThreadQueries()를 넣으면 메인쓰레드에서 돌아가게 해준다
                         */
                        .allowMainThreadQueries()
                        .build()
                }
            }
            return chatRoomInstance
        }
    }
}