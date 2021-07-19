package com.kyonggi.randhand_chat.Fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kyonggi.randhand_chat.Adapter.UserAdapter
import com.kyonggi.randhand_chat.Model.User
import com.kyonggi.randhand_chat.R
import com.kyonggi.randhand_chat.databinding.FragmentFriendsBinding

class FriendFragment : Fragment() {
    private lateinit var friendsBinding: FragmentFriendsBinding
    private lateinit var adapter: UserAdapter
    var profileList: ArrayList<User> = ArrayList()
    companion object {
        const val TAG : String = "로그"
        // 친구 정보들에 대한 Fragment 를 가져온다
        fun newInstance(): FriendFragment {
            return FriendFragment()
        }
    }

    // 메모리에 올라갔을때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "FriendFragment -onCreate() called")
        addData()
    }

    // Fragment 를 안고 있는 Activity 에 붙였을때
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "FriendFragment -onAttach() called")
    }

    private fun addData() {
        profileList.add(User("1","박준후","http://t1.daumcdn.net/friends/prod/editor/dc8b3d02-a15a-4afa-a88b-989cf2a50476.jpg"))
        profileList.add(User("1","김기용","1"))
        profileList.add(User("1","김기현","1"))
        profileList.add(User("1","한동현","1"))
        profileList.add(User("1","최준규","1"))
        profileList.add(User("1","김기용","1"))
        profileList.add(User("1","김기현","1"))
        profileList.add(User("1","한동현","1"))
        profileList.add(User("1","최준규","1"))
        profileList.add(User("1","김기용","1"))
        profileList.add(User("1","김기현","1"))
        profileList.add(User("1","한동현","1"))
        profileList.add(User("1","최준규","1"))
        profileList.add(User("1","김기용","1"))
        profileList.add(User("1","김기현","1"))
        profileList.add(User("1","한동현","1"))
        profileList.add(User("1","최준규","1"))
    }

    // onCreateView 의 리턴값은 View를 가지고 있다.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        friendsBinding.friendsList.apply {
            layoutManager = LinearLayoutManager(activity)
            // 어댑터 설정
            adapter = UserAdapter(profileList)
            // 어댑터 연결

        }
    }

    // View 가 생성되었을때
    // Fragment 와 레이아웃을 연결시켜준다
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "FriendFragment -onCreateView() called")
//        val view = inflater.inflate(R.layout.fragment_friends, container, false )
        friendsBinding = FragmentFriendsBinding.inflate(inflater, container, false)
        return friendsBinding.root
    }
}
