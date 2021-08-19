package com.kyonggi.randhand_chat.Fragments.ChatsActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.kyonggi.randhand_chat.Adapter.MessageAdapter
import com.kyonggi.randhand_chat.Database.*
import com.kyonggi.randhand_chat.Domain.Message.SyncInfo
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitChat
import com.kyonggi.randhand_chat.Retrofit.ServiceURL
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.ActivityChatBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString
import okhttp3.WebSocket
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ChatActivity : AppCompatActivity() {
    private lateinit var retrofit: Retrofit
    private lateinit var supplementServiceChat: IRetrofitChat

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatBinding: ActivityChatBinding
    private lateinit var webSocket: WebSocket

    private lateinit var chatRoomDatabase: ChatRoomDatabase
    private lateinit var chatDAO: ChatRoomDAO
    private lateinit var messageDAO: MessageDAO
    private lateinit var chatId: String // 상대방에 대한 유저 아이디
    private lateinit var sessionId: String // 채팅방에 대한 sessionId
    private lateinit var chatRoomInfo: ChatRoomTable

    /**
     * 테스트용 client
     */
    private lateinit var client: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 자동으로 완성된 Activity Chat Binding 클래스를 인스턴스로 가져온다
        chatBinding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(chatBinding.root)
        initChatRetrofit()
        sessionId = intent.getStringExtra("sessionId").toString()

        EventBus.getDefault().register(this)
        /**
         *
         */
        chatRoomDatabase = ChatRoomDatabase.getInstance(this)!!
        chatDAO = chatRoomDatabase.roomChatRoomDAO()
        messageDAO = chatRoomDatabase.roomMessageDAO()
        chatId = chatDAO.getUserIds(sessionId)
        /**
         * 현재 채팅방의 정보를 가져온다
         */
        chatRoomInfo = chatDAO.getChatRoomTable(sessionId)

        // SyncInfo 에 message 리스트가 비어있지 않으면(sync 해야하는 메세지가 있으면)
        getSyncMessages(supplementServiceChat, sessionId)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()

    }

    override fun finish() {
        super.finish()
        webSocket.close(MyWebSocketListener(sessionId, chatId, chatDAO).NORMAL_CLOSURE_STATUS,null)
    }

    private fun getSyncMessages(supplementServiceChat: IRetrofitChat, sessionId: String) {
        val token = AppUtil.prefs.getString("token", null)
        val userId = AppUtil.prefs.getString("userId", null)
        supplementServiceChat.syncMessages(
            chatRoomInfo.syncTime.format(
                DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd'T'HH:mm:ss"
                )
            ), token, userId, sessionId
        ).enqueue(object : Callback<SyncInfo> {
            override fun onResponse(call: Call<SyncInfo>, response: retrofit2.Response<SyncInfo>) {
                val syncInfo = response.body()!!

                // get MessageTable
                val messageList: MutableList<MessageTable> =
                    messageDAO.getChatRoomMessage(intent.getStringExtra("sessionId")!!)
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

                // ex) [1,2,3]
                // have to sync
                if (syncInfo.messageList.isNotEmpty()) {


                    // ex) [4,5]
                    for (message in syncInfo.messageList) {
                        // change each MessageInfo to new MessageTable & save to sqlite
                        val messageTable = MessageTable(
                            null,
                            sessionId,
                            message.fromUser,
                            message.type,
                            message.content,
                            LocalDateTime.parse(message.createdAt, formatter)
                        )
                        messageList.add(messageTable)
                        messageDAO.insertMessage(messageTable)
                    }
                }

                // ex) [1,2,3,4,5]
                // sync my ChatRoomTable syncTime from syncInfo.syncTime
                chatDAO.updateSyncTime(LocalDateTime.parse(syncInfo.syncTime, formatter), sessionId)

                /**
                 *  데이터연결
                 */
                loadChatRoomInfo()

            }

            override fun onFailure(call: Call<SyncInfo>, t: Throwable) {
                Log.d("ERROR", "SyncTime Error")
            }

        })
    }
    private fun loadChatRoomInfo() {
        /**
         * 테스트용 메시지 들어있는 List 생성
         */
        val chatList = messageDAO.getChatRoomMessage(sessionId)

        /**
         * 메시지 어뎁터 설정 및 RecyclerView와 연결
         */
        messageAdapter = MessageAdapter(chatList)
        chatBinding.messageList.adapter = messageAdapter
        /**
         * 메시지 어뎁터 매니저 설정
         */
        chatBinding.messageList.layoutManager = LinearLayoutManager(this)

        val token = AppUtil.prefs.getString("token", null)
        val userId = AppUtil.prefs.getString("userId", null)
        /**
         * 웹소켓 통신하기
         */
        client = OkHttpClient()
        val request = Request.Builder()
            .url("ws://3.36.37.197:8000/chat-service/websocket/session/$sessionId")
            .addHeader("Authorization", token)
            .addHeader("userId", userId)
            .build()
        val listener =  ChatActivity().MyWebSocketListener(sessionId, chatId, chatDAO)

        webSocket = client.newWebSocket(request, listener)
        client.dispatcher.executorService.shutdown()

        with(chatBinding) {
            var text = ""
            editText.addTextChangedListener { editText ->
                text = editText.toString()
            }
            // TEXT
            btnSend.setOnClickListener {
                // 메시지 보내기
                if (text.isNotEmpty()) {
                    val message = MessageTable(null,
                        sessionId,
                        userId,
                        "TEXT",
                        text,
                        LocalDateTime.now()
                    )
                    // scroll the RecyclerView to the last added element
                    messageList.scrollToPosition(messageAdapter.itemCount)
                    messageAdapter.addMessage(message)
                    // 최신메시지로 바꾸어준다.
                    chatDAO.updatePrefMessage(text, sessionId)

                    /**
                     * 데이터베이스에 보낸 문자를 넣는다
                     */
                    messageDAO.insertMessage(message)
                }
                webSocket.send(text)
                editText.text = null
            }

            /**
             * SEND IMAGE
             */
            // 1. send Image
            // 2. receive result (image url)
            // 3. create MessageTable with image url

        }
    }

    private fun initChatRetrofit() {
        retrofit = ServiceURL.getInstance()
        supplementServiceChat = retrofit.create(IRetrofitChat::class.java)
    }

    /**
     * @Subscribe 를 통하여 Main 쓰레드에서 실행
     * 메시지 보내준다.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(message: MessageTable) {
            message.fromUser = chatId
            chatBinding.messageList.scrollToPosition(messageAdapter.itemCount)
            messageAdapter.addMessage(message)
            messageDAO.insertMessage(message)
    }

   inner class MyWebSocketListener(val sessionId: String, val chatId: String, val chatDAO: ChatRoomDAO) : WebSocketListener() {

        val NORMAL_CLOSURE_STATUS = 1000

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("Socket","Closing : $code / $reason")
            webSocket.close(NORMAL_CLOSURE_STATUS, null)
            webSocket.cancel()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.d("Socket","Error : " + t.message)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("Socket","Receiving : $text")
            // EventBus 로 message 를 post 해준다
            EventBus.getDefault().post(MessageTable(null,sessionId,chatId,"TEXT",text, LocalDateTime.now()))
            chatDAO.updatePrefMessage(text, sessionId)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d("Socket", "Receiving bytes : $bytes")
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
//            webSocket.send("{\"type\":\"ticker\", \"symbols\": [\"BTC_KRW\"], \"tickTypes\": [\"30M\"]}")
//        webSocket.close(NORMAL_CLOSURE_STATUS, null) //없을 경우 끊임없이 서버와 통신함
        }
    }
}