package com.kyonggi.randhand_chat.Fragments.ChatsActivity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.kyonggi.randhand_chat.Adapter.MessageAdapter
import com.kyonggi.randhand_chat.Database.*
import com.kyonggi.randhand_chat.Domain.Message.MessageInfo
import com.kyonggi.randhand_chat.Domain.Message.SyncInfo
import com.kyonggi.randhand_chat.Domain.User.ResponseUser
import com.kyonggi.randhand_chat.Fragments.FriendFragment
import com.kyonggi.randhand_chat.R
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitChat
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitUser
import com.kyonggi.randhand_chat.Retrofit.ServiceURL
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.ActivityChatBinding
import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ChatActivity : AppCompatActivity() {
    private lateinit var retrofit: Retrofit
    private lateinit var supplementServiceChat: IRetrofitChat
    private lateinit var supplementServiceUser: IRetrofitUser

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatBinding: ActivityChatBinding
    private lateinit var webSocket: WebSocket

    private lateinit var chatRoomDatabase: ChatRoomDatabase
    private lateinit var chatDAO: ChatRoomDAO
    private lateinit var messageDAO: MessageDAO
    private lateinit var chatId: String // 상대방에 대한 유저 아이디
    private lateinit var sessionId: String // 채팅방에 대한 sessionId
    private lateinit var chatRoomInfo: ChatRoomTable

    private lateinit var client: OkHttpClient

    private lateinit var getImage: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 자동으로 완성된 Activity Chat Binding 클래스를 인스턴스로 가져온다
        chatBinding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(chatBinding.root)
        /**
         * 툴바 설정해준다
         */
        setSupportActionBar(chatBinding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        initRetrofit()
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

        /**
         * SEND IMAGE REQUEST
         */
        // callback
        getImage =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                if (result.resultCode == Activity.RESULT_OK) {

                    val imagePath = result.data?.data

                    val image =
                        imagePath?.let {
                            contentResolver.openInputStream(it)?.use { inputStream ->
                                BitmapFactory.decodeStream(inputStream)
                            }
                        }

                    val stream = ByteArrayOutputStream()
                    image?.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                    val bytes = stream.toByteArray()
                    val imageString = Base64.encodeToString(bytes, Base64.NO_WRAP)

                    val byteArray = imageString.toByteArray(Charset.forName("UTF-8"))
                    // ByteBuffer
                    val byteBuffer = ByteBuffer.wrap(byteArray)

                    // 웹소켓으로 이미지 전송
                    webSocket.send(byteBuffer.toByteString())
                }

            }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()

    }

    override fun finish() {
        super.finish()
        webSocket.close(MyWebSocketListener(sessionId, chatId, chatDAO).NORMAL_CLOSURE_STATUS, null)
    }

    // 커스텀한 toolbar actionBar에 넣어준다
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.message_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.contextOutChatRoom -> {
                /**
                 * 채팅방 나가기
                 */
                super.onOptionsItemSelected(item)
            }
            R.id.search_message -> {
                /**
                 * 메시지 찾기
                 */
                super.onOptionsItemSelected(item)
            }
            R.id.addFriend -> {
                addFriend(supplementServiceUser, chatId)
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * 친구 추가
     */
    private fun addFriend(supplementServiceUser: IRetrofitUser, friendId: String) {
        supplementServiceUser.addUser(AppUtil.prefs.getString("token",null), AppUtil.prefs.getString("userId",null), friendId)
            .enqueue(object: Callback<List<ResponseUser>> {
                override fun onResponse(call: Call<List<ResponseUser>>, response: retrofit2.Response<List<ResponseUser>>) {
                    // null 값이 아니라면 친구 추가
                    when (response.code()) {
                        200 -> {
                            Toast.makeText(this@ChatActivity, "친구추가 되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                        400 -> {
                            Toast.makeText(this@ChatActivity, "이미 친구입니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<List<ResponseUser>>, t: Throwable) {
                    Log.d("ERROR", "오류: FriendFragment.addFriend")
                }

            })
    }

    private fun getSyncMessages(supplementServiceChat: IRetrofitChat, sessionId: String) {
        val token = AppUtil.prefs.getString("token", null)
        val userId = AppUtil.prefs.getString("userId", null)
        supplementServiceChat.syncMessages(
            chatRoomInfo.syncTime.plusSeconds(1).format(
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

                        val messageTable =
                            createMessageTable(sessionId, message, null, null, null, null)
                        messageList.add(messageTable)
                        messageDAO.insertMessage(messageTable)
                    }
                    // sync my ChatRoomTable syncTime from syncInfo.syncTime
                    chatDAO.updateSyncTime(LocalDateTime.parse(syncInfo.syncTime, formatter), sessionId)
                }

                // ex) [1,2,3,4,5]

                /**
                 *  데이터연결
                 */
                loadChatRoomInfo()

            }

            override fun onFailure(call: Call<SyncInfo>, t: Throwable) {
                Log.d("ERROR", "오류: ChatActivity.getSyncMessage")
            }

        })
    }

    private fun createMessageTable(
        sessionId: String,
        message: MessageInfo?,
        from: String?,
        type: String?,
        content: String?,
        time: LocalDateTime?
    ): MessageTable {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        // messageInfo O
        message?.let { messageInfo ->
            return MessageTable(
                null,
                sessionId,
                messageInfo.fromUser,
                messageInfo.type,
                messageInfo.content,
                LocalDateTime.parse(messageInfo.createdAt, formatter)
            )
        }

        // messageInfo X
        return MessageTable(
            null,
            sessionId,
            from,
            type!!,
            content!!,
            time!!
        )
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
        Handler(Looper.getMainLooper()).postDelayed({
            chatBinding.messageList.scrollToPosition(messageAdapter.itemCount - 1)
        }, 200)

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
        val listener = ChatActivity().MyWebSocketListener(sessionId, chatId, chatDAO)

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
                    val message = createMessageTable(
                        sessionId,
                        null,
                        userId,
                        "TEXT",
                        text,
                        LocalDateTime.now()
                    )
                    webSocket.send(text)
                    // scroll the RecyclerView to the last added element
                    messageList.scrollToPosition(messageAdapter.itemCount)
                    messageAdapter.addMessage(message)
                    messageDAO.insertMessage(message)

                    // 최신메시지로 바꾸어준다.
                    chatDAO.updatePrefMessage(text, sessionId)
                    chatDAO.updateSyncTime(message.time, sessionId)

                }
                editText.text = null
            }
            /**
             * SEND IMAGE
             */
            btnGallery.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = MediaStore.Images.Media.CONTENT_TYPE
                getImage.launch(intent)
            }

        }
    }

    private fun initRetrofit() {
        retrofit = ServiceURL.getInstance()
        supplementServiceChat = retrofit.create(IRetrofitChat::class.java)
        supplementServiceUser = retrofit.create(IRetrofitUser::class.java)
    }

    /**
     * @Subscribe 를 통하여 Main 쓰레드에서 실행
     * 메시지 보내준다.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(message: MessageTable) {
        chatBinding.messageList.scrollToPosition(messageAdapter.itemCount)
        messageAdapter.addMessage(message)
        messageDAO.insertMessage(message)
        chatDAO.updatePrefMessage(message.context, sessionId)
    }

    inner class MyWebSocketListener(
        val sessionId: String,
        private val chatId: String,
        private val chatDAO: ChatRoomDAO
    ) : WebSocketListener() {

        val NORMAL_CLOSURE_STATUS = 1000

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("Socket", "Closing : $code / $reason")
            webSocket.close(NORMAL_CLOSURE_STATUS, null)
            webSocket.cancel()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.d("Socket", "Error : " + t.message)
        }

        // TEXT MESSAGE
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("Socket", "Receiving : $text")
            // EventBus 로 message 를 post 해준다
            val now = LocalDateTime.now()
            chatDAO.updateSyncTime(now, sessionId)
            chatDAO.updatePrefMessage(text, sessionId)
            EventBus.getDefault()
                .post(createMessageTable(sessionId, null, chatId, "TEXT", text, now))
        }

        // BINARY MESSAGE (image)
        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d("Socket", "Receiving bytes : $bytes")
            val now = LocalDateTime.now()

            // 1. byteARRAY (image)

            // 2. amazon upload -> url

            // 3. onMessage(url // byteArray)

            val url = bytes.string(Charset.forName("UTF-8"))
            // amazon.com/{userId}image{UUID}
            val splits = url.split("/")
            val fromUser = splits[splits.size - 1].split("image")[0]

            EventBus.getDefault().post(
                createMessageTable(
                    sessionId,
                    null,
                    fromUser,
                    "IMAGE",
                    url,
                    now
                )
            )
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
//        webSocket.close(NORMAL_CLOSURE_STATUS, null) //없을 경우 끊임없이 서버와 통신함
        }
    }
}