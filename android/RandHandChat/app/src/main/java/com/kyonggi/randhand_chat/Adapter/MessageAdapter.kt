package com.kyonggi.randhand_chat.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kyonggi.randhand_chat.Domain.Message.Message
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitUser
import com.kyonggi.randhand_chat.Retrofit.ServiceURL
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.Util.DateUtils
import com.kyonggi.randhand_chat.databinding.ApplyMessageBinding
import com.kyonggi.randhand_chat.databinding.MyMessageBinding
import retrofit2.Retrofit


/**
 * Message 관련 ViewHolder open 클래스로 만들어준다.
 * 이를 통하여 사용자와 상대방과 메세지를 관리한다.
 */

class MessageAdapter(private val messages : MutableList<Message> = mutableListOf()) : RecyclerView.Adapter<MessageViewHolder>() {
    /**
     * 처음 메시지 리스트를 가져오는 작업이 있어야한다.
     * (지금까지의 메시지들)
     */

    companion object {
        private const val VIEW_TYPE_MY_MESSAGE = 1
        private const val VIEW_TYPE_OTHER_MESSAGE = 2
    }

    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: IRetrofitUser

    // 메시지를 추가한다.
    fun addMessage(message: Message) {
        messages.add(message)
        notifyDataSetChanged()
    }

    /**
     * ID를 통한 메시지 Type 을 가져오기
     */
    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        val userId = AppUtil.prefs.getString("userId", null)
        return if (userId == message.fromUser) {
            VIEW_TYPE_MY_MESSAGE
        } else {
            VIEW_TYPE_OTHER_MESSAGE
        }
    }

    /**
     * viewType 에 따라서 나의 메시지와 상대메시지 ViewHolder에 바인딩
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        initRetrofit()
        return if (viewType == VIEW_TYPE_MY_MESSAGE) {
            MyMessageViewHolder(
                MyMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            OtherMessageViewHolder(
                ApplyMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    private fun initRetrofit() {
        retrofit = ServiceURL.getInstance()
        supplementService = retrofit.create(IRetrofitUser::class.java)
    }

    /**
     * 사용자의 MessageViewHolder
     */
    class MyMessageViewHolder(myMessageBinding: MyMessageBinding) : MessageViewHolder(myMessageBinding.root) {
        private var fromUser = ""
        private var messageText =  myMessageBinding.myMessageBody
        private var timeText = myMessageBinding.textTime
        private var dateText = myMessageBinding.textTime

        // 메시지의 정보를 View 에 바인딩 해준다
        override fun bind(message: Message) {
            fromUser = message.fromUser.toString()
            messageText.text = message.context
            timeText.text = DateUtils.fromMillisToTimeString(message.time)
        }
    }

    /**
     * 상대방의 MessageViewHolder
     */
    class OtherMessageViewHolder(applyMessageBinding: ApplyMessageBinding) : MessageViewHolder(applyMessageBinding.root) {
        private var fromUser = ""
        private var messageText = applyMessageBinding.applyMessageBody
        private var timeText = applyMessageBinding.textTime
        private var dateText = applyMessageBinding.textTime

        // 메시지의 정보를 View 에 바인딩 해준다
        override fun bind(message: Message) {
            fromUser = message.fromUser.toString()
            // 보내는 사람 프로필 정보 가져오기
            messageText.text = message.context
            timeText.text = DateUtils.fromMillisToTimeString(message.time)
        }
    }
}


open class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    open fun bind(message: Message) {}
}


