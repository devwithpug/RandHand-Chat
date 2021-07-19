package com.kyonggi.randhand_chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kyonggi.randhand_chat.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {
    private lateinit var chatBinding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 자동으로 완성된 Activity Chat Binding 클래스를 인스턴스로 가져온다
        chatBinding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(chatBinding.root)
    }
}