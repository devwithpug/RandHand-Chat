package com.kyonggi.randhand_chat.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kyonggi.randhand_chat.Database.ChatRoomDAO
import com.kyonggi.randhand_chat.Database.ChatRoomDatabase
import com.kyonggi.randhand_chat.Database.MessageDAO
import com.kyonggi.randhand_chat.Database.MessageTable
import com.kyonggi.randhand_chat.R
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitUser
import com.kyonggi.randhand_chat.Retrofit.ServiceURL
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.ApplyImageMessageBinding
import com.kyonggi.randhand_chat.databinding.ApplyTextMessageBinding
import com.kyonggi.randhand_chat.databinding.MyImageMessageBinding
import com.kyonggi.randhand_chat.databinding.MyTextMessageBinding
import retrofit2.Retrofit
import java.time.format.DateTimeFormatter


/**
 * Message 관련 ViewHolder open 클래스로 만들어준다.
 * 이를 통하여 사용자와 상대방과 메세지를 관리한다.
 */

class MessageAdapter(private var messages: MutableList<MessageTable>) : RecyclerView.Adapter<MessageViewHolder>() {

    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: IRetrofitUser

    private lateinit var chatRoomDatabase: ChatRoomDatabase
    private lateinit var messageDAO: MessageDAO
    private lateinit var chatDAO: ChatRoomDAO

    companion object {
        private const val VIEW_TYPE_MY_MESSAGE = 1
        private const val VIEW_TYPE_OTHER_MESSAGE = 2
        private const val VIEW_TYPE_MY_MESSAGE_IMAGE = 3
        private const val VIEW_TYPE_OTHER_MESSAGE_IMAGE = 4
    }

    // 메시지를 추가한다.
    fun addMessage(message: MessageTable) {
        messages.add(message)
        notifyDataSetChanged()
    }

    // 메시지를 삭제한다.
    fun deleteMessage(message: MessageTable) {
        messages.remove(message)
        // 선택된 메시지 데이터 베이스에서 삭제
        messageDAO.deleteMessage(message)
        notifyDataSetChanged()
    }

    /**
     * ID를 통한 메시지 Type 을 가져오기
     * 보낸 메시지의 Type도 확인 -> "Text", or "IMAGE"
     */
    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        val userId = AppUtil.prefs.getString("userId", null)
        return if (userId == message.fromUser) {
            if (message.type == "TEXT") {
                VIEW_TYPE_MY_MESSAGE
            } else {
                VIEW_TYPE_MY_MESSAGE_IMAGE
            }
        } else {
            if (message.type == "TEXT") {
                VIEW_TYPE_OTHER_MESSAGE
            } else {
                VIEW_TYPE_OTHER_MESSAGE_IMAGE
            }
        }
    }

    /**
     * viewType 에 따라서 나의 메시지와 상대메시지 ViewHolder에 바인딩
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        chatRoomDatabase = ChatRoomDatabase.getInstance(parent.context)!!
        messageDAO = chatRoomDatabase.roomMessageDAO()
        chatDAO = chatRoomDatabase.roomChatRoomDAO()

        return if (viewType == VIEW_TYPE_MY_MESSAGE) {
            MyMessageViewHolder(
                MyTextMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else if (viewType == VIEW_TYPE_MY_MESSAGE_IMAGE){
            MyMessageImageViewHolder(
                MyImageMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else if (viewType == VIEW_TYPE_OTHER_MESSAGE) {
            initRetrofit()
            OtherMessageViewHolder(
                ApplyTextMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            initRetrofit()
            OtherMessageImageViewHolder(
                ApplyImageMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        holder.apply {
            // 메시지 타입별로 bind를 해준다.
            bind(message)
            // 각 리사이클러뷰의 ViewHolder 길게 클릭시 context menu 팝업
            itemView.setOnLongClickListener {
                val pop = PopupMenu(itemView.context, it)
                pop.inflate(R.menu.message_context_menu)

                pop.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.deleteMessage -> {
                            deleteMessage(message)
                        }
                    }
                    true
                }
                pop.show()
                true
            }
        }
    }

    /**
     * 메시지 text 복사
     */

    override fun getItemCount(): Int {
        return messages.size
    }

    /**
     * 사용자의 MessageViewHolder -> TEXT
     */
    class MyMessageViewHolder(val myMessageBinding: MyTextMessageBinding) : MessageViewHolder(myMessageBinding.root) {
        private var fromUser = ""
        private var messageText =  myMessageBinding.myMessageBody
        private var timeText = myMessageBinding.textTime
        private var dateText = myMessageBinding.textDate

        // 메시지의 정보를 View 에 바인딩 해준다
        override fun bind(message: MessageTable) {
            fromUser = message.fromUser.toString()
            messageText.text = message.context
            timeText.text = message.time.format(DateTimeFormatter.ofPattern("a K:mm"))
//            dateText.text = message.time.format(DateTimeFormatter.ofPattern("M월 dd일"))
        }
    }
    /**
     * 사용자의 MessageViewHolder -> IMAGE
     */
    class MyMessageImageViewHolder(val myMessageImageBinding: MyImageMessageBinding) : MessageViewHolder(myMessageImageBinding.root) {

        private var fromUser = ""
        private var imageView = myMessageImageBinding.imageView
        private var timeText = myMessageImageBinding.textTime
        private var dateText = myMessageImageBinding.textDate
        // 메시지의 정보를 View 에 바인딩 해준다
        override fun bind(message: MessageTable) {
            fromUser = message.fromUser.toString()
            timeText.text = message.time.format(DateTimeFormatter.ofPattern("a K:mm"))
//            dateText.text = message.time.format(DateTimeFormatter.ofPattern("M월 dd일"))

            // Glide 이미지 로드
            Glide.with(itemView)
                .load(message.context)
                .error(Glide.with(itemView)
                    .load(R.drawable.messageimage_error))
                .into(imageView)
        }
    }

    /**
     * 상대방의 MessageViewHolder -> TEXT
     */
    inner class OtherMessageViewHolder(val applyMessageBinding: ApplyTextMessageBinding) : MessageViewHolder(applyMessageBinding.root) {
        private var fromUser = ""
        private var fromUserImage = applyMessageBinding.userImage
        private var fromUserName = applyMessageBinding.userName
        private var messageText = applyMessageBinding.applyMessageBody
        private var timeText = applyMessageBinding.textTime
        private var dateText = applyMessageBinding.textDate

        // 메시지의 정보를 View 에 바인딩 해준다
        override fun bind(message: MessageTable) {
            fromUser = message.fromUser.toString()
            /**
             * 현재 채팅방의 room 정보 데이터베이스에서 가져오기
             */
            val chatRoomData = chatDAO.getChatRoomTable(message.sessionId)

            with(chatRoomData) {
                // 채팅하는 상대방 이름
                fromUserName.text = userName

                // 채팅하는 상대방방 프로필 이미지
                Glide.with(itemView)
                    .load(userImage)
                    .error(Glide.with(itemView)
                        .load(R.drawable.no_image))
                    .into(fromUserImage)
            }
            messageText.text = message.context
            timeText.text = message.time.format(DateTimeFormatter.ofPattern("a K:mm"))
//            dateText.text = message.time.format(DateTimeFormatter.ofPattern("M월 dd일"))
        }
    }

    /**
     * 상대방의 MessageViewHolder -> IMAGE
     */
    inner class OtherMessageImageViewHolder(val applyImageMessageBinding: ApplyImageMessageBinding) : MessageViewHolder(applyImageMessageBinding.root) {
        private var fromUser = ""
        private var fromUserImage = applyImageMessageBinding.userImage
        private var fromUserName = applyImageMessageBinding.userName
        private var imageView = applyImageMessageBinding.imageView
        private var timeText = applyImageMessageBinding.textTime
        private var dateText = applyImageMessageBinding.textDate

        // 메시지의 정보를 View 에 바인딩 해준다
        override fun bind(message: MessageTable) {
            fromUser = message.fromUser.toString()
            /**
             * 현재 채팅방의 room 정보 데이터베이스에서 가져오기
             */
            val chatRoomData = chatDAO.getChatRoomTable(message.sessionId)

            with(chatRoomData) {
                // 채팅하는 상대방 이름
                fromUserName.text = userName

                // 채팅하는 상대방방 프로필 이미지
                Glide.with(itemView)
                    .load(userImage)
                    .error(Glide.with(itemView)
                        .load(R.drawable.no_image))
                    .into(fromUserImage)
            }

            // 상대방이 보낸 이미지 파일
            Glide.with(itemView)
                .load(message.context)
                .into(imageView)

            timeText.text = message.time.format(DateTimeFormatter.ofPattern("a K:mm"))
//            dateText.text = message.time.format(DateTimeFormatter.ofPattern("M월 dd일"))
        }
    }

    private fun initRetrofit() {
        retrofit = ServiceURL.getInstance()
        supplementService = retrofit.create(IRetrofitUser::class.java)
    }
}


open class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    open fun bind(message: MessageTable) {}
}


