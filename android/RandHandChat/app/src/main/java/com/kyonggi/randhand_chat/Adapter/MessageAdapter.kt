package com.kyonggi.randhand_chat.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kyonggi.randhand_chat.Database.MessageTable
import com.kyonggi.randhand_chat.Domain.User.ResponseUser
import com.kyonggi.randhand_chat.R
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitUser
import com.kyonggi.randhand_chat.Retrofit.ServiceURL
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.Util.DateUtils
import com.kyonggi.randhand_chat.databinding.ApplyMessageBinding
import com.kyonggi.randhand_chat.databinding.MyMessageBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


/**
 * Message 관련 ViewHolder open 클래스로 만들어준다.
 * 이를 통하여 사용자와 상대방과 메세지를 관리한다.
 */

class MessageAdapter(private var messages: MutableList<MessageTable>) : RecyclerView.Adapter<MessageViewHolder>() {
    /**
     * 처음 메시지 리스트를 가져오는 작업이 있어야한다.
     * (지금까지의 메시지들)
     */

    companion object {
        private const val VIEW_TYPE_MY_MESSAGE = 1
        private const val VIEW_TYPE_OTHER_MESSAGE = 2
    }

    // 메시지를 추가한다.
    fun addMessage(message: MessageTable) {
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

    /**
     * 사용자의 MessageViewHolder
     */
    class MyMessageViewHolder(myMessageBinding: MyMessageBinding) : MessageViewHolder(myMessageBinding.root) {
        private var fromUser = ""
        private var messageText =  myMessageBinding.myMessageBody
        private var timeText = myMessageBinding.textTime
        private var dateText = myMessageBinding.textTime

        // 메시지의 정보를 View 에 바인딩 해준다
        override fun bind(message: MessageTable) {
            fromUser = message.fromUser.toString()
            messageText.text = message.context
            timeText.text = DateUtils.fromMillisToTimeString(message.time)
        }
    }

    /**
     * 상대방의 MessageViewHolder
     */
    class OtherMessageViewHolder(val applyMessageBinding: ApplyMessageBinding) : MessageViewHolder(applyMessageBinding.root) {
        private lateinit var retrofit: Retrofit
        private lateinit var supplementService: IRetrofitUser

        private var fromUser = ""
        private var messageText = applyMessageBinding.applyMessageBody
        private var timeText = applyMessageBinding.textTime
        private var dateText = applyMessageBinding.textTime

        // 메시지의 정보를 View 에 바인딩 해준다
        override fun bind(message: MessageTable) {
            initRetrofit()
            fromUser = message.fromUser.toString()
            /**
             * 보내는 유저의 프로필 정보 가져오기 -> 이름, 사진
             */
            getUserInfo(supplementService, fromUser)

            messageText.text = message.context
            timeText.text = DateUtils.fromMillisToTimeString(message.time)
        }

        /**
         * 사용자 정보를 가져온후 view에 바인딩 시켜준다.
         */
        private fun getUserInfo(supplementService: IRetrofitUser, fromUser: String) {
            val token = AppUtil.prefs.getString("token", null)
            val userId = AppUtil.prefs.getString("userId", null)
            supplementService.getUserInfo(token, userId, fromUser).enqueue(object : Callback<ResponseUser> {
                override fun onResponse(call: Call<ResponseUser>, response: Response<ResponseUser>) {
                    val info = response.body()
                    with(applyMessageBinding) {
                        userName.text = info?.name

                        Glide.with(itemView)
                            .load(info?.picture)
                            .error(Glide.with(itemView)
                                .load(R.drawable.no_image))
                            .into(userImage)
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
}


open class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    open fun bind(message: MessageTable) {}
}


