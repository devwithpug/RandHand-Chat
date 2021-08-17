package com.kyonggi.randhand_chat.Fragments.ChatsActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.kyonggi.randhand_chat.Adapter.MessageAdapter
import com.kyonggi.randhand_chat.Database.ChatRoomDAO
import com.kyonggi.randhand_chat.Database.ChatRoomDatabase
import com.kyonggi.randhand_chat.Database.MessageDAO
import com.kyonggi.randhand_chat.Database.MessageTable
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.ActivityChatBinding
import okhttp3.*
import okio.ByteString
import java.util.*
import okhttp3.WebSocket
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class ChatActivity : AppCompatActivity() {
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatBinding: ActivityChatBinding
    private lateinit var webSocket: WebSocket

    private lateinit var chatRoomDatabase: ChatRoomDatabase
    private lateinit var chatDAO: ChatRoomDAO
    private lateinit var messageDAO: MessageDAO
    private lateinit var chatId: String // 상대방에 대한 유저 아이디

    /**
     * 테스트용 client
     */
    private lateinit var client: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 자동으로 완성된 Activity Chat Binding 클래스를 인스턴스로 가져온다
        chatBinding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(chatBinding.root)

        EventBus.getDefault().register(this)
        /**
         *
         */
        chatRoomDatabase = ChatRoomDatabase.getInstance(this)!!
        chatDAO = chatRoomDatabase.roomChatRoomDAO()
        messageDAO = chatRoomDatabase.roomMessageDAO()
        chatId = chatDAO.getUserIds("1")

        /**
         * 테스트용 메시지 들어있는 List 생성
         */
        val chatList = messageDAO.getChatRoomMessage("1")

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
            .url("ws://3.36.37.197:8000/chat-service/websocket/session/1")
            .addHeader("Authorization", token)
            .addHeader("userId", userId)
            .addHeader("debug", "true")
            .addHeader("skip-validate-session", "true")
            .build()
        val listener =  ChatActivity().MyWebSocketListener()

        webSocket = client.newWebSocket(request, listener)
        client.dispatcher.executorService.shutdown()

        with(chatBinding) {
            var text = ""
            editText.addTextChangedListener { editText ->
                text = editText.toString()
            }
            btnSend.setOnClickListener {
                // 메시지 보내기
                if (text.isNotEmpty()) {
                    val message = MessageTable(null,
                        "1",
                        userId,
                        text,
                        Calendar.getInstance().timeInMillis
                    )
                    // scroll the RecyclerView to the last added element
                    messageList.scrollToPosition(messageAdapter.itemCount)
                    messageAdapter.addMessage(message)

                    /**
                     * 데이터베이스에 보낸 문자를 넣는다
                     */
                    messageDAO.insertMessage(message)
                }
                webSocket.send(text)
                editText.text = null
            }
        }
    }


    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()

    }

    override fun finish() {
        super.finish()
        webSocket.close(MyWebSocketListener().NORMAL_CLOSURE_STATUS,null)
    }

    /**
     * @Subscribe를 통하여 Main 쓰레드에서 실행
     * 메시지 보내준다.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(message: MessageTable) {
            message.fromUser = chatId
            chatBinding.messageList.scrollToPosition(messageAdapter.itemCount)
            messageAdapter.addMessage(message)
            messageDAO.insertMessage(message)
    }

   inner class MyWebSocketListener : WebSocketListener() {

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
            EventBus.getDefault().post(MessageTable(null,"1",null,text,Calendar.getInstance().timeInMillis))
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