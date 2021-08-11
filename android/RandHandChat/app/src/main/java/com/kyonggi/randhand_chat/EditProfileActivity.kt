package com.kyonggi.randhand_chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.kyonggi.randhand_chat.Domain.Client
import com.kyonggi.randhand_chat.Domain.ResponseUser
import com.kyonggi.randhand_chat.Retrofit.IRetrofit
import com.kyonggi.randhand_chat.Retrofit.Service.ServiceUser
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.ActivityEditProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class EditProfileActivity : AppCompatActivity() {
    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: IRetrofit

    private lateinit var binding : ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        initRetrofit()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            // 기존 이름으로 설정
            profileName.setText(intent.getStringExtra("name"))
            // 상태 메시지 가져오기
            statusMessage.setText(intent.getStringExtra("status"))
            // 이미지 가져오기
            Glide.with(this@EditProfileActivity)
                .load(intent.getStringExtra("image"))
                .into(profileImage)

            val userId = intent.getStringExtra("userId")
            var name = intent.getStringExtra("name")
            var message = intent.getStringExtra("status")
            profileName.addTextChangedListener { edit ->
                name = edit.toString()
            }
            statusMessage.addTextChangedListener { edit ->
                message = edit.toString()
            }

            // edit 버튼 클릭
            editButton.setOnClickListener {
                val client = Client(userId, "google",AppUtil.prefs.getString("email", null), name,
                    message, "https://lh3.googleusercontent.com/a-/AOh14GiB6_Kkxl5350YTF2UV_chCFqtL6HvkTPtnjzQw")
                editProfile(supplementService, client)
            }
        }
    }

    private fun editProfile(supplementService: IRetrofit, client: Client) {
        val token = AppUtil.prefs.getString("token", null)
        val userId = AppUtil.prefs.getString("userId", null)
        supplementService.editClient(token, userId, client).enqueue(object : Callback<ResponseUser> {
            override fun onResponse(call: Call<ResponseUser>, response: Response<ResponseUser>) {
                Toast.makeText(this@EditProfileActivity, "변경성공", Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onFailure(call: Call<ResponseUser>, t: Throwable) {
            }

        })
    }

    private fun initRetrofit() {
        retrofit = ServiceUser.getInstance()
        supplementService = retrofit.create(IRetrofit::class.java)
    }
}