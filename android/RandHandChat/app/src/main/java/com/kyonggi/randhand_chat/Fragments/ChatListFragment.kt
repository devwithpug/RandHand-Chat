package com.kyonggi.randhand_chat.Fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kyonggi.randhand_chat.Adapter.ChatRoomAdapter
import com.kyonggi.randhand_chat.Database.*
import com.kyonggi.randhand_chat.Domain.Chat.ChatInfo
import com.kyonggi.randhand_chat.Domain.User.ResponseUser
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitUser
import com.kyonggi.randhand_chat.Retrofit.ServiceURL
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.FragmentChatsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*

class ChatListFragment : Fragment() {
    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: IRetrofitUser

    private lateinit var chatBinding: FragmentChatsBinding
    private var chatRoomList: MutableList<ChatRoomTable> = mutableListOf()


    // 채팅방 정보에대한 sessionId 와 List<String>
    private lateinit var chatInfo: ChatInfo

    /**
     * 테스트용 CharRoom Database
     */
    private lateinit var chatRoomDatabase: ChatRoomDatabase
    private lateinit var chatDAO: ChatRoomDAO
    private lateinit var messageDAO: MessageDAO

    companion object {
        const val TAG : String = "로그"
        // 채팅방 정보에대한 Fragment 생성
        fun newInstance(): ChatListFragment {
            return ChatListFragment()
        }
    }

    // 메모리에 올라갔을때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "ChatFragment -onCreate() called")

        chatRoomDatabase = context?.let { ChatRoomDatabase.getInstance(it) }!!
        chatDAO = chatRoomDatabase.roomChatRoomDAO()    // 채팅방에대한 데이터베이스
        messageDAO = chatRoomDatabase.roomMessageDAO()  // 메시지에 대한 데이터베이스

        /**
         *                      테스트용 데이터베이스 데이터
         *                      채팅관련 테스트 아이디
         */
//        val list : MutableList<MessageTable> = mutableListOf(
//            MessageTable(null,"1","3be6ce8b-5974-47d3-8722-e36e5ca86723","메시지 테스트1입니다.", Calendar.getInstance().timeInMillis),
//            MessageTable(null,"1","1c4b3282-1007-4cd8-9882-4723ca248779","메시지 테스트2입니다.\n메시지 줄바꿈 입니다", Calendar.getInstance().timeInMillis),
//            MessageTable(null,"1","1c4b3282-1007-4cd8-9882-4723ca248779","메시지 테스트3입니다.", Calendar.getInstance().timeInMillis),
//            MessageTable(null,"1","3be6ce8b-5974-47d3-8722-e36e5ca86723","메시지 테스트4입니다.", Calendar.getInstance().timeInMillis),
//            MessageTable(null,"1","3be6ce8b-5974-47d3-8722-e36e5ca86723","메시지 테스트5입니다.", Calendar.getInstance().timeInMillis),
//        )
//        // 메시지 넣어주기
//        messageDAO.insertMessageList(list)



        chatInfo = ChatInfo("1", listOf("1c4b3282-1007-4cd8-9882-4723ca248779","3be6ce8b-5974-47d3-8722-e36e5ca86723"))



    }
    // Fragment 를 안고 있는 Activity 에 붙였을때
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "ChatFragment -onAttach() called")
    }

    // View 가 생성되었을때
    // Fragment 와 레이아웃을 연결시켜준다
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "ChatFragment -onCreateView() called")
//        val view = inflater.inflate(R.layout.fragment_chattings, container, false )
        chatBinding = FragmentChatsBinding.inflate(inflater, container, false)
        return chatBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(FriendFragment.TAG, "ChatFragment -onViewCreated() called")

        initRetrofit()

        with(chatBinding) {
            // 리사이클러뷰 설정
            chatRoom.apply {
                // 세로로 만들어주기
                layoutManager = LinearLayoutManager(activity)
                // 어뎁터 성능을 위해서 추가
                setHasFixedSize(true)
                /**
                 * 채팅방 테스트용 chatId
                 */
                // 채팅하는 유저 아이디
                val chatId = chatInfo.userIds.filter { it != AppUtil.prefs.getString("userId", null) }[0]

                getUserInfo(supplementService, chatId)
            }
        }

    }

    private fun getUserInfo(supplementService: IRetrofitUser, chatId: String) {
        val token = AppUtil.prefs.getString("token",null)
        val userId = AppUtil.prefs.getString("userId", null)
        supplementService.getUserInfo(token, userId, chatId).enqueue(object :
            Callback<ResponseUser> {
            override fun onResponse(call: Call<ResponseUser>, response: Response<ResponseUser>) {
                val info = response.body()
                /**
                 * 채팅방 관련한 테이블들
                 */
                val chatList = messageDAO.getChatRoomMessage("1")
                /**
                 *                        테스트용 데이터베이스 데이터
                 *          채팅하고 있는 상대방의 seesionID, 상대방ID와 프로필정보를 넣어준다
                 */
//                chatDAO.insertRoomInfo(
//                    ChatRoomTable("1",
//                        chatId,info?.name!!, info?.picture,chatList)
//                )

                with(chatBinding) {
                    // 어답터 설정
                    chatRoomList = chatDAO.getAll() as MutableList<ChatRoomTable>
                    chatRoom.adapter = ChatRoomAdapter(chatRoomList,chatList)

                }
            }

            override fun onFailure(call: Call<ResponseUser>, t: Throwable) {
            }

        })
    }

    private fun initRetrofit() {
        retrofit = ServiceURL.getInstance()
        supplementService = retrofit.create(IRetrofitUser::class.java)
    }
}