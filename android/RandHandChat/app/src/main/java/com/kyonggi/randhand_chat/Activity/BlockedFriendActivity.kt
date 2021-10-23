package com.kyonggi.randhand_chat.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.kyonggi.randhand_chat.Adapter.BlockedUserAdapter
import com.kyonggi.randhand_chat.Domain.User.ResponseUser
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitUser
import com.kyonggi.randhand_chat.Retrofit.ServiceURL
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.ActivityBlockedFriendBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class BlockedFriendActivity : AppCompatActivity() {
    val binding by lazy { ActivityBlockedFriendBinding.inflate(layoutInflater) }

    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: IRetrofitUser
    private var blockedList: MutableList<ResponseUser> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initRetrofit()

        binding.toolbar.title = "차단목록"
        setSupportActionBar(binding.toolbar)
        getBlockedList(supplementService)
        binding.blockedRecycler.layoutManager = LinearLayoutManager(this)
    }


    private fun getBlockedList(supplementService: IRetrofitUser) {
        supplementService.getBlockUserList(AppUtil.prefs.getString("token", null), AppUtil.prefs.getString("userId",null))
            .enqueue(object : Callback<List<ResponseUser>> {
                override fun onResponse(call: Call<List<ResponseUser>>, response: Response<List<ResponseUser>>) {
                    blockedList = response.body() as MutableList<ResponseUser>
                    if (blockedList.size != 0) {
                        binding.blockedListText.visibility = View.INVISIBLE
                    }
                    // 화면의 recyclerView와 연결
                    binding.blockedRecycler.adapter = BlockedUserAdapter(blockedList)
                }

                override fun onFailure(call: Call<List<ResponseUser>>, t: Throwable) {
                    Log.d("ERROR", "오류: BlockedFriendActivity.getBlockedList")
                }

            })
    }

    private fun initRetrofit() {
        retrofit = ServiceURL.getInstance()
        supplementService = retrofit.create(IRetrofitUser::class.java)
    }
}