package com.kyonggi.randhand_chat.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.kyonggi.randhand_chat.Domain.User.ResponseUser
import com.kyonggi.randhand_chat.R
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitUser
import com.kyonggi.randhand_chat.Retrofit.ServiceURL
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.ActivityProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProfileBinding
    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: IRetrofitUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRetrofit()
        val userId = intent.getStringExtra("userId")
        val check = intent.getStringExtra("myProfile")
        val intent = Intent(this@ProfileActivity, EditProfileActivity::class.java)
        getMyInfo(supplementService, userId!!, intent)

        with(binding) {
            if (check != null) {
                editProfile.visibility = View.VISIBLE
            }
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

    private fun getMyInfo(supplementService: IRetrofitUser, infoId: String, intent: Intent) {
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
                        .error(Glide.with(this@ProfileActivity)
                            .load(R.drawable.no_image))
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
                Log.d("ERROR", "오류: ProfileActivity.getMyInfo")
            }

        })
    }

    private fun initRetrofit() {
        retrofit = ServiceURL.getInstance()
        supplementService = retrofit.create(IRetrofitUser::class.java)
    }
}