package com.kyonggi.randhand_chat.Adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kyonggi.randhand_chat.Database.*
import com.kyonggi.randhand_chat.Fragments.ChatsActivity.ChatActivity
import com.kyonggi.randhand_chat.R
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitChat
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitUser
import com.kyonggi.randhand_chat.Retrofit.ServiceURL
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.ListChatsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.time.format.DateTimeFormatter

class ChatRoomAdapter(var chatRoomList: MutableList<ChatRoomTable>) : RecyclerView.Adapter<ChatRoomAdapter.Holder>(){
    private lateinit var retrofit: Retrofit
    private lateinit var supplementServiceUser: IRetrofitUser
    private lateinit var supplementServiceChat: IRetrofitChat
    private lateinit var item: ChatRoomTable

    private lateinit var chatRoomDatabase: ChatRoomDatabase
    private lateinit var chatDAO: ChatRoomDAO

    private lateinit var binding: ListChatsBinding

    /**
     * 테스트용 client
     */

    // 연결될 리스트 아이템들을 붙이는 작업 -> onCreateViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        // Adapter에 연결된 parent.context -> 모든 정보들을 가져온다
        binding = ListChatsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    // 현재 바인드 되고있는 position의 값을 가져온다
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: Holder, position: Int) {
        initRetrofit()
        chatRoomDatabase = ChatRoomDatabase.getInstance(holder.itemView.context)!!
        chatDAO = chatRoomDatabase.roomChatRoomDAO()
        holder.apply {
            item = chatRoomList[position]
            userName.text = item.userName
            userId = item.toUser
            // 이미지 로드
            Glide.with(holder.itemView)
                .load(item.userImage)
                .error(Glide.with(holder.itemView)
                    .load(R.drawable.no_image))
                .into(userImage)
            // 메시지 리스트
            if (item.prefMessage == null) {
                preMessage.text = null
                time.text = null
            } else {
                if (item.type == "IMAGE") {
                    preMessage.text = "[IMAGE]"
                } else {
                    preMessage.text = item.prefMessage
                }
                time.text =item.syncTime.format(DateTimeFormatter.ofPattern("a K:mm"))
            }

            /**
             * 채팅방 접속
             */
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ChatActivity::class.java)
                intent.putExtra("sessionId",item.sessionId)
                // chatRoom 에 대한 정보를 넘겨준다
                itemView.context.startActivity(intent)
            }

            itemView.setOnLongClickListener {
                val pop= PopupMenu(binding.root.context,it)
                pop.inflate(R.menu.messagelist_context_menu)

                pop.setOnMenuItemClickListener { click->
                    when (click.itemId) {
                        R.id.contextOutChatRoom -> {
                            /**
                             * 채팅방 나가기
                             * 데이터베이스에서 chatRoomInfo, Message삭제, 서버에서 websocket 삭제
                             */
                            val token = AppUtil.prefs.getString("token", null)
                            val userId = AppUtil.prefs.getString("userId", null)
                            deleteChatRoom(supplementServiceChat, item.sessionId ,token, userId, position)
                        }
                    }
                    true
                }
                pop.show()
                true
            }
        }
    }

    override fun getItemCount() = chatRoomList.size

    private fun initRetrofit() {
        retrofit = ServiceURL.getInstance()
        supplementServiceUser = retrofit.create(IRetrofitUser::class.java)
        supplementServiceChat = retrofit.create(IRetrofitChat::class.java)
    }

    // 채팅방의 내용이 바뀌었을때
    fun dataSetChanged(changeList: MutableList<ChatRoomTable>) {
        chatRoomList = changeList
        notifyDataSetChanged()
    }

    // 데이터 삭제
    fun delete(position: Int) {
        if (position >= 0) {
            chatRoomList.removeAt(position)
            // 갱신처리를 해주어야한다
            notifyDataSetChanged()
        }
    }

    // 내부클래스 뷰홀더를위한 클래스 -> 차단된 유저에 대한 View를 잡아준다.
    inner class Holder(val binding: ListChatsBinding) : RecyclerView.ViewHolder(binding.root) {
        // 받은 데이터를 화면에 출력한다
        val userName = binding.chatProfile
        val userImage = binding.chatUserImage
        val preMessage = binding.chatMessage
        val time = binding.chatTime
        var userId = ""
    }

    // 채팅방 나가기 요청
    private fun deleteChatRoom(supplementServiceChat: IRetrofitChat, sessionId: String, token: String, userId: String, position: Int) {
        supplementServiceChat.removeChatRoom(sessionId, token, userId).enqueue(object: Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                when (response.code()) {
                    200 -> {
                        chatDAO.deleteRoomInfo(item)
                        delete(position)
                        Toast.makeText(binding.root.context, "채팅방을 나갔습니다.", Toast.LENGTH_SHORT).show()
                    }
                    400 -> {
                        Log.d("ERROR", "오류: ChatRoomAdapter.deleteChatRoom STATUS CODE 400")
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("ERROR", "오류: ChatRoomAdapter.deleteChatRoom")
            }

        })
    }
}