package com.kyonggi.randhand_chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.kyonggi.randhand_chat.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var loginBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        loginBinding.button.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        loginBinding.button2.setOnClickListener {
            startActivity(Intent(this,ChatActivity::class.java))
        }

    }
}