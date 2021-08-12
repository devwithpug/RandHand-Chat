package com.kyonggi.randhand_chat.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kyonggi.randhand_chat.Domain.User.ResponseUser
import com.kyonggi.randhand_chat.R
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitUser
import com.kyonggi.randhand_chat.Retrofit.ServiceURL
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.ListBlockedBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class BlockedUserAdapter(val listData: MutableList<ResponseUser>) : RecyclerView.Adapter<BlockedUserAdapter.Holder>() {
    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: IRetrofitUser

    // 연결될 리스트 아이템들을 붙이는 작업 -> onCreateViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        // Adapter에 연결된 parent.context -> 모든 정보들을 가져온다
        val binding = ListBlockedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    // 현재 바인드 되고있는 position의 값을 가져온다
    override fun onBindViewHolder(holder: Holder, position: Int) {
        initRetrofit()
        holder.apply {
            val item = listData[position]
            userName.text = item.name
            userId = item.userId.toString()
            // 이미지 로드
            Glide.with(holder.itemView)
                .load(item.picture)
                .error(Glide.with(holder.itemView)
                    .load(R.drawable.no_image))
                .into(image)

            // 친구 차단해제 요청
            binding.btnBlocked.setOnClickListener {
                removeBlockedFriend(supplementService, holder.userId, holder.bindingAdapterPosition)
                addFriend(supplementService, holder.userId)
            }
        }

    }
    // 유저 차단 해제
    private fun removeBlockedFriend(supplementService: IRetrofitUser, blockedId: String, position: Int) {
        supplementService.getBlockUserRemove(AppUtil.prefs.getString("token", null),AppUtil.prefs.getString("userId", null), blockedId)
            .enqueue(object : Callback<List<ResponseUser>> {
                override fun onResponse(call: Call<List<ResponseUser>>, response: Response<List<ResponseUser>>) {
                    delete(position)
                }

                override fun onFailure(call: Call<List<ResponseUser>>, t: Throwable) {
                }

            })

    }

    // 친구 추가
    private fun addFriend(supplementService: IRetrofitUser, friendId: String) {
        supplementService.addUser(AppUtil.prefs.getString("token",null), AppUtil.prefs.getString("userId",null), friendId)
            .enqueue(object: Callback<List<ResponseUser>> {
                override fun onResponse(call: Call<List<ResponseUser>>, response: Response<List<ResponseUser>>) {
                    val body = response.body() as MutableList<ResponseUser>
                }

                override fun onFailure(call: Call<List<ResponseUser>>, t: Throwable) {
                }

            })
    }

    // 데이터 삭제
    fun delete(position: Int) {
        if (position >= 0) {
            listData.removeAt(position)
            // 갱신처리를 해주어야한다
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = listData.size

    private fun initRetrofit() {
        retrofit = ServiceURL.getInstance()
        supplementService = retrofit.create(IRetrofitUser::class.java)
    }

    // 내부클래스 뷰홀더를위한 클래스 -> 차단된 유저에 대한 View를 잡아준다.
    class Holder(val binding: ListBlockedBinding): RecyclerView.ViewHolder(binding.root) {
        // 받은 데이터를 화면에 출력한다
        val userName = binding.profileName
        val image = binding.profileImage
        var userId = ""
    }
}