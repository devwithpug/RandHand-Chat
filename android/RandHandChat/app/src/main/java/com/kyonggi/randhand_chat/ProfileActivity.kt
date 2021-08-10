package com.kyonggi.randhand_chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.kyonggi.randhand_chat.Domain.ResponseUser
import com.kyonggi.randhand_chat.Retrofit.IRetrofit
import com.kyonggi.randhand_chat.Retrofit.Service.ServiceUser
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.ActivityProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProfileBinding
    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: IRetrofit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRetrofit()
        val userId = intent.getStringExtra("userId")
        val intent = Intent(this@ProfileActivity, EditProfileActivity::class.java)
        getMyInfo(supplementService, userId!!, intent)

        with(binding) {
            // Edit 버튼 클릭시
            editProfile.setOnClickListener {
                startActivity(intent)
            }
        }
    }

    override fun onRestart() {
        finish()
        startActivity(intent)
        super.onRestart()
    }

    private fun getMyInfo(supplementService: IRetrofit, infoId: String, intent: Intent) {
        val token = AppUtil.prefs.getString("token",null)
        val userId = AppUtil.prefs.getString("userId", null)
        supplementService.getUserInfo(token, userId, infoId).enqueue(object : Callback<ResponseUser> {
            override fun onResponse(call: Call<ResponseUser>, response: Response<ResponseUser>) {
                val info = response.body()
                // 이름 가져오기
                with(binding) {
                    profileName.text = info?.name
                    // 상태 메시지 가져오기
                    statusMessage.text = info?.message
                    // 이미지 가져오기
                    Glide.with(this@ProfileActivity)
                        .load(info?.picture)
                        .into(profileImage)
                }

                intent.apply {
                    putExtra("name", info?.name)
                    putExtra("image", info?.picture)
                    putExtra("status", info?.message)
                    putExtra("userId", infoId)
                }

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