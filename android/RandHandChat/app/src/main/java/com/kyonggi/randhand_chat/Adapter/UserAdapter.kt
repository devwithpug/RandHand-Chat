package com.kyonggi.randhand_chat.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.kyonggi.randhand_chat.Fragments.UserProfileFragment
import com.kyonggi.randhand_chat.Model.User
import com.kyonggi.randhand_chat.databinding.ListFriendsBinding

class UserAdapter(val profileList: ArrayList<User>,
)
    : RecyclerView.Adapter<UserAdapter.CustomViewHolder>() {
    private lateinit var listFriendsBinding: ListFriendsBinding

//    private lateinit var mlistener: onItemClick
//    // 클릭한 요소의 position 을 위한 인터페이스
//    interface onItemClick {
//        fun onItemClick(position: Int)
//    }
    // 내부클래스 뷰홀더를위한 클래스 -> 유저의 프로필에 대한 View를 잡아준다.
    class CustomViewHolder(private val binding: ListFriendsBinding) : RecyclerView.ViewHolder(binding.root) {
        val userId = "1234" // 테스트용 id
        val userName = binding.profileName // 유저 이름
        val imageUrl = binding.profileImage // 유저 이미지 url

        init {
            // 클릭스 이벤트 설정
            itemView.setOnClickListener {
                val position: Int = adapterPosition
                Toast.makeText(itemView.context,"You clicked on item # ${position + 1}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 연결될 리스트 아이템들을 붙이는 작업 -> onCreateViewHolder
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserAdapter.CustomViewHolder {
        // Adapter에 연결된 parent.context -> 모든 정보들을 가져온다
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_friends, parent, false)
        val binding = ListFriendsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        listFriendsBinding = binding
        return CustomViewHolder(listFriendsBinding)
    }

    override fun onBindViewHolder(holder: UserAdapter.CustomViewHolder, position: Int) {
        // 현재 바인드 되고있는 position의 값을 가져온다
        holder.apply {
            userName.text = profileList[position].userName
        }

    }

    // 총 리스트의 개수
    override fun getItemCount(): Int {
        return profileList.size
    }
}
