package com.kyonggi.randhand_chat.Database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface ChatRoomDAO {

    @Query("select * from chatroom_list")
    fun getAll() : List<ChatRoomTable>

    // 상대방의 userId를 가져온다
    @Query("select toUser from chatroom_list where sessionId = :sessionId")
    fun getUserIds(sessionId: String) : String

    @Insert(onConflict = REPLACE)   // 중복되는것이있으면 (출돌이 나면) 자동 update
    fun insertRoomInfo(room: ChatRoomTable)

    @Delete
    fun deleteRoomInfo(room: ChatRoomTable)
}

@Dao
interface MessageDAO {
    /**
     * 특정 sessionId 의 채팅방의 메시지들 리스트를 가져온다
     */
    @Query("select * from chatroom_message_list where sessionId = :sessionId")
    fun getChatRoomMessage(sessionId: String): MutableList<MessageTable>

    @Insert(onConflict = REPLACE)   // 중복되는것이있으면 (출돌이 나면) 자동 update
    fun insertMessage(message: MessageTable)

    /**
     * 테스트용 list 메시지 삽입
     */
    @Insert(onConflict = REPLACE)
    fun insertMessageList(message: MutableList<MessageTable>)
}