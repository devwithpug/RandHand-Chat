package com.kyonggi.randhand_chat.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kyonggi.randhand_chat.Domain.Chat.ChatRoom
import com.kyonggi.randhand_chat.Fragments.ChatsActivity.ChatActivity
import com.kyonggi.randhand_chat.R
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitUser
import com.kyonggi.randhand_chat.Retrofit.ServiceURL
import com.kyonggi.randhand_chat.Util.DateUtils
import com.kyonggi.randhand_chat.databinding.ListChatsBinding
import retrofit2.Retrofit

class ChatRoomAdapter(val chatRoomList: MutableList<ChatRoom>) : RecyclerView.Adapter<ChatRoomAdapter.Holder>() {
    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: IRetrofitUser

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
    override fun onBindViewHolder(holder: Holder, position: Int) {
        initRetrofit()
        holder.apply {
            val item = chatRoomList[position]
            userName.text = item.userName
            userId = item.userId
            // 이미지 로드
            Glide.with(holder.itemView)
                .load(item.userImage)
                .error(Glide.with(holder.itemView)
                    .load(R.drawable.no_image))
                .into(userImage)
            // 메시지 리스트
            val messageList = item.messageList
            if (messageList == null) {
                preMessage.text = null
                time.text = null
            } else {
                preMessage.text = messageList.last().context
                time.text = messageList.last().time.let { DateUtils.fromMillisToTimeString(it) }
            }

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ChatActivity::class.java)
                // chatRoom 에 대한 정보를 넘겨준다
                intent.putExtra("chatRoomInfo", item)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = chatRoomList.size

    private fun initRetrofit() {
        retrofit = ServiceURL.getInstance()
        supplementService = retrofit.create(IRetrofitUser::class.java)
    }

    // 내부클래스 뷰홀더를위한 클래스 -> 차단된 유저에 대한 View를 잡아준다.
    class Holder(val binding: ListChatsBinding) : RecyclerView.ViewHolder(binding.root) {
        // 받은 데이터를 화면에 출력한다
        val userName = binding.chatProfile
        val userImage = binding.chatUserImage
        val preMessage = binding.chatMessage
        val time = binding.chatTime
        var userId = ""
    }
}