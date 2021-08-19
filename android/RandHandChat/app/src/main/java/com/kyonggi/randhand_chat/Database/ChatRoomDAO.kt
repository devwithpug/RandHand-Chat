package com.kyonggi.randhand_chat.Database

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import java.time.LocalDateTime

@Dao
interface ChatRoomDAO {

    @Query("select * from chatroom_list")
    fun getAll() : List<ChatRoomTable>

    // 상대방의 userId를 가져온다
    @Query("select toUser from chatroom_list where sessionId = :sessionId")
    fun getUserIds(sessionId: String) : String

    // sessionId인 방을 가져온다
    @Query("select * from chatroom_list where sessionId = :sessionId")
    fun getChatRoomTable(sessionId: String) : ChatRoomTable

    @Insert(onConflict = REPLACE)   // 중복되는것이있으면 (출돌이 나면) 자동 update
    fun insertRoomInfo(room: ChatRoomTable)

    @Delete
    fun deleteRoomInfo(room: ChatRoomTable)

    @Query("update chatroom_list set syncTime = :syncTime where sessionId = :sessionId")
    fun updateSyncTime(syncTime: LocalDateTime, sessionId: String)

    @Query("update chatroom_list set prefMessage = :message where sessionId = :sessionId")
    fun updatePrefMessage(message: String, sessionId: String)

}