package com.kyonggi.randhand_chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.kyonggi.randhand_chat.Database.ChatRoomDAO
import com.kyonggi.randhand_chat.Database.ChatRoomDatabase
import com.kyonggi.randhand_chat.Database.ChatRoomTable
import com.kyonggi.randhand_chat.Domain.Chat.ChatInfo
import com.kyonggi.randhand_chat.Domain.User.ResponseUser
import com.kyonggi.randhand_chat.Fragments.ChatsActivity.ChatActivity
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitChat
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitUser
import com.kyonggi.randhand_chat.Retrofit.ServiceURL
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.ActivityProgressBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

class ProgressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProgressBinding

    private lateinit var retrofit: Retrofit
    private lateinit var supplementServiceChat: IRetrofitChat
    private lateinit var supplementServiceUser: IRetrofitUser

    private lateinit var chatRoomDatabase: ChatRoomDatabase
    private lateinit var chatRoomDAO: ChatRoomDAO
    private lateinit var chatInfoFromServer: List<ChatInfo>
    private lateinit var sessionId: String
    private var retry: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRetrofit()

        // 채팅방 데이터베이스 설정
        chatRoomDatabase = ChatRoomDatabase.getInstance(this)!!
        chatRoomDAO = chatRoomDatabase.roomChatRoomDAO()

        // <- 메인쓰레드
        showProgress(true)

        thread(start = true) { // -> 서브쓰레드
            while (retry) {

                val token = AppUtil.prefs.getString("token", null)
                val userId = AppUtil.prefs.getString("userId", null)

                // 1. GET chats/session
                getChatRoomInfoFromServer(supplementServiceChat, token, userId)
                Thread.sleep(7000)
            }

            // 화면에 영향을 미치는 코드는
            // 메인쓰레드로 다시 보내줘야한다
            runOnUiThread {
                showProgress(false)
                showProgressComplete(sessionId)
            } // 서브 쓰레드
        } // 메인 쓰레드
    }


    private fun showProgress(show: Boolean) {
        binding.progressLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showProgressComplete(sessionId: String) {
        binding.progressCompleteLayout.visibility = View.VISIBLE
        /**
         * 매칭이 성공시 2초후 채팅방으로 이동
         */
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("sessionId", sessionId)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(intent)
            finish()
        }, 2000)
    }

    private fun getChatRoomInfoFromServer (supplementServiceChat: IRetrofitChat, token: String, userId: String) {
        supplementServiceChat.getChatRoomInfoByUserId(token, userId).enqueue(object: Callback<List<ChatInfo>> {
            override fun onResponse(call: Call<List<ChatInfo>>, response: Response<List<ChatInfo>>) {
                val result = response.body()
                chatInfoFromServer = result ?: listOf()

                // 2. compare DAO chat and result chat

                val chatInfoFromDAO = chatRoomDAO.getAll()
                val sessions = chatInfoFromDAO
                    .map { c -> c.sessionId }

                if (chatInfoFromDAO.size < chatInfoFromServer.size) {
                    // new Chat Room CREATED!!!
                    val newChat = chatInfoFromServer
                        .filter { c -> !sessions.contains(c.sessionId)}[0]

                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                    val syncTime = LocalDateTime.parse(newChat.syncTime, formatter)

                    sessionId = newChat.sessionId
                    val chatId = newChat.userIds.filter { i -> i != userId }[0]

                    getUserInfo(supplementServiceUser, chatId, syncTime)
                    Thread.sleep(2000)
                }

            }

            override fun onFailure(call: Call<List<ChatInfo>>, t: Throwable) {
                Log.d("ProgressActivity", "getChatRoomInfoFromServer Error")
            }

        })
    }

    private fun getUserInfo(supplementServiceUser: IRetrofitUser, chatId: String, syncTime: LocalDateTime) {
        val token = AppUtil.prefs.getString("token",null)
        val userId = AppUtil.prefs.getString("userId", null)
        supplementServiceUser.getUserInfo(token, userId, chatId).enqueue(object :
            Callback<ResponseUser> {
            override fun onResponse(call: Call<ResponseUser>, response: Response<ResponseUser>) {
                val info = response.body()

                // 채팅방 정보 넣기
                chatRoomDAO.insertRoomInfo(
                    ChatRoomTable(
                        sessionId,
                        chatId, info?.name!!, info.picture, syncTime, "", null
                    )
                )
                retry = false
            }
            override fun onFailure(call: Call<ResponseUser>, t: Throwable) {
                Log.d("ERROR", "오류: ChatListFragment.getUserInfo")
            }
        })
    }

    private fun initRetrofit() {
        retrofit = ServiceURL.getInstance()
        supplementServiceChat = retrofit.create(IRetrofitChat::class.java)
        supplementServiceUser = retrofit.create(IRetrofitUser::class.java)
    }
}