package com.kyonggi.randhand_chat.Database

import androidx.room.*

@Dao
interface MessageDAO {
    /**
     * 특정 sessionId 의 채팅방의 메시지들 리스트를 가져온다
     */
    @Query("select * from chatroom_message_list where sessionId = :sessionId")
    fun getChatRoomMessage(sessionId: String): MutableList<MessageTable>

    @Insert(onConflict = OnConflictStrategy.REPLACE)   // 중복되는것이있으면 (출돌이 나면) 자동 update
    fun insertMessage(message: MessageTable)

    /**
     * 테스트용 list 메시지 삽입
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessageList(message: MutableList<MessageTable>)

    /**
     * 메시지 삭제
     */
    @Delete
    fun deleteMessage(message: MessageTable)

    /**
     * find recently
     */
    @Query("select * from chatroom_message_list where sessionId = :sessionId order by messageId desc limit 1")
    fun getRecentMessage(sessionId: String) : MessageTable
}