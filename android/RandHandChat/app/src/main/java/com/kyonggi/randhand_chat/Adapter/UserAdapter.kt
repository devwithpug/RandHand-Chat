package com.kyonggi.randhand_chat.Adapter

import android.content.Intent
import android.view.*
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kyonggi.randhand_chat.Domain.ResponseUser
import com.kyonggi.randhand_chat.ProfileActivity
import com.kyonggi.randhand_chat.R
import com.kyonggi.randhand_chat.Retrofit.IRetrofit
import com.kyonggi.randhand_chat.Retrofit.Service.ServiceUser
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.ListFriendsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class UserAdapter(var profileList: MutableList<ResponseUser>) : RecyclerView.Adapter<UserAdapter.CustomViewHolder>(){
    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: IRetrofit
    private lateinit var listFriendsBinding: ListFriendsBinding


    // 연결될 리스트 아이템들을 붙이는 작업 -> onCreateViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.CustomViewHolder {
        // Adapter에 연결된 parent.context -> 모든 정보들을 가져온다
        val binding = ListFriendsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        listFriendsBinding = binding
        return CustomViewHolder(listFriendsBinding)
    }

    override fun onBindViewHolder(holder: UserAdapter.CustomViewHolder, position: Int) {
        initRetrofit()
        // 현재 바인드 되고있는 position의 값을 가져온다
        holder.apply {
            val item  = profileList[position]
            userName.text = item.name
            statusMessage.text = item.message
            // 유저 아이디 가져오기
            val userId = item.userId
            // 이미지 로드
            Glide.with(holder.itemView)
                .load(item.picture)
                .into(image)

            // 각 리사이클러뷰의 ViewHolder 클릭시 화면 전환
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ProfileActivity::class.java)
                intent.putExtra("name", item.name)
                intent.putExtra("image", item.picture)
                intent.putExtra("status", item.message)
                intent.putExtra("userId", item.userId)
                itemView.context.startActivity(intent)
            }
            // 각 리사이클러뷰의 ViewHolder 길게 클릭시 context menu 팝업
            itemView.setOnLongClickListener {
                val pop = PopupMenu(itemView.context, it)
                pop.inflate(R.menu.user_context_menu)

                pop.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.deleteMenu -> {
                            deleteFriend(supplementService, "$userId", position)
                        }
                        R.id.blockMenu -> {
                            // 유저 차단하기
                            blockedFriend(supplementService, "$userId", position)
                        }
                    }
                    true
                }
                pop.show()
                true
            }
        }
    }

    // 총 리스트의 개수
    override fun getItemCount() =  profileList.size

    // 유저 차단하기
    private fun blockedFriend(supplementService: IRetrofit, blockedId: String, position: Int) {
        supplementService.blockUser(AppUtil.prefs.getString("token",null), AppUtil.prefs.getString("userId",null), blockedId)
            .enqueue(object: Callback<List<ResponseUser>> {
                override fun onResponse(call: Call<List<ResponseUser>>, response: Response<List<ResponseUser>>) {
                    deleteFriend(supplementService, blockedId,position)
                }

                override fun onFailure(call: Call<List<ResponseUser>>, t: Throwable) {

                }

            })
    }

    // 유저 삭제하기
    private fun deleteFriend(supplementService: IRetrofit, friendId: String, position: Int) {
        supplementService.deleteUser(AppUtil.prefs.getString("token",null), AppUtil.prefs.getString("userId", null), friendId)
            .enqueue(object :Callback<List<ResponseUser>>{
                override fun onResponse(call: Call<List<ResponseUser>>, response: Response<List<ResponseUser>>) {
                    delete(position)
                }

                override fun onFailure(call: Call<List<ResponseUser>>, t: Throwable) {
                }

            })
    }

    // 데이터 삭제
    fun delete(position: Int) {
        if (position >= 0) {
            profileList.removeAt(position)
            // 갱신처리를 해주어야한다
            notifyDataSetChanged()
        }
    }

    private fun initRetrofit() {
        retrofit = ServiceUser.getInstance()
        supplementService = retrofit.create(IRetrofit::class.java)
    }

    // 내부클래스 뷰홀더를위한 클래스 -> 유저의 프로필에 대한 View를 잡아준다.
    class CustomViewHolder(val binding: ListFriendsBinding) : RecyclerView.ViewHolder(binding.root) {
        val userName = binding.profileName // 유저 이름
        val image = binding.profileImage // 유저 이미지 url
        var statusMessage = binding.profileMessage // 유저 상태메시지

    }
}
