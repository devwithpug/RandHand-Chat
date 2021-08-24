package com.kyonggi.randhand_chat.Fragments.UserActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.kyonggi.randhand_chat.Domain.User.Client
import com.kyonggi.randhand_chat.Domain.User.ResponseUser
import com.kyonggi.randhand_chat.R
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitUser
import com.kyonggi.randhand_chat.Retrofit.ServiceURL
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.Util.BitmapRequestBody
import com.kyonggi.randhand_chat.databinding.ActivityEditProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class EditProfileActivity : AppCompatActivity() {
    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: IRetrofitUser

    private lateinit var binding: ActivityEditProfileBinding

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
                .error(
                    Glide.with(this@EditProfileActivity)
                        .load(R.drawable.no_image)
                )
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

            // edit 버튼 클릭 -> 프리필 변경
            editButton.setOnClickListener {
                val client = Client(
                    userId, "google", AppUtil.prefs.getString("email", null), name,
                    message, intent.getStringExtra("image")
                )
                editProfile(supplementService, client)
            }

            // 갤러리 버튼 클릭
            btnGallery.setOnClickListener {
                /**
                 * 갤러리를 들어가서 이미지 선택
                 * 선택된 이미지를 glide로 보여줌
                 * 저장을하면 서버에 저장
                 */
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = MediaStore.Images.Media.CONTENT_TYPE
//                intent.type = "image/*"
                getImage.launch(intent)

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getEditProfileImage(supplementService: IRetrofitUser, data: Bitmap) {
        val token = AppUtil.prefs.getString("token", null)
        val userId = AppUtil.prefs.getString("userId", null)

        val bitmapRequestBody = BitmapRequestBody(data)
        val image = MultipartBody.Part.createFormData("image", "image.bmp", bitmapRequestBody)

        image.let {
            supplementService.editImage(token, userId, it)
                .enqueue(object : Callback<ResponseUser> {
                    override fun onResponse(
                        call: Call<ResponseUser>,
                        response: Response<ResponseUser>
                    ) {
                        val userImage = response.body()?.picture

                        Glide.with(this@EditProfileActivity)
                            .load(userImage)
                            .into(binding.profileImage)

                    }

                    override fun onFailure(call: Call<ResponseUser>, t: Throwable) {
                        Log.d("ERROR", "오류: EditProfileActivity.getEditProfileImage")
                    }

                })
        }
    }

    private fun editProfile(supplementService: IRetrofitUser, client: Client) {
        val token = AppUtil.prefs.getString("token", null)
        val userId = AppUtil.prefs.getString("userId", null)
        supplementService.editClient(token, userId, client)
            .enqueue(object : Callback<ResponseUser> {
                override fun onResponse(
                    call: Call<ResponseUser>,
                    response: Response<ResponseUser>
                ) {
                    Toast.makeText(this@EditProfileActivity, "변경성공", Toast.LENGTH_SHORT).show()
                    finish()
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