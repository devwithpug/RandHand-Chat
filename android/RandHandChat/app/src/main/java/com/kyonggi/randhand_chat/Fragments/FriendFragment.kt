package com.kyonggi.randhand_chat.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.kyonggi.randhand_chat.Adapter.UserAdapter
import com.kyonggi.randhand_chat.Activity.BlockedFriendActivity
import com.kyonggi.randhand_chat.Domain.User.ResponseUser
import com.kyonggi.randhand_chat.Activity.ProfileActivity
import com.kyonggi.randhand_chat.R
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitUser
import com.kyonggi.randhand_chat.Retrofit.ServiceURL
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.FragmentFriendsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class FriendFragment : Fragment() {
    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: IRetrofitUser

    private lateinit var friendsBinding: FragmentFriendsBinding
    private var profileList: MutableList<ResponseUser> = mutableListOf()

    companion object {
        const val TAG : String = "로그"
        // 친구 정보들에 대한 Fragment 를 가져온다
        fun newInstance(): FriendFragment {
            return FriendFragment()
        }
    }

    // 메모리에 올라갔을때
    override fun onCreate(savedInstanceState: Bundle?) {
        initRetrofit()
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
        Log.d(TAG, "FriendFragment -onCreate() called")
    }

    override fun onResume() {
        val userId = AppUtil.prefs.getString("userId", null)
        val token = AppUtil.prefs.getString("token", null)
        friendList(supplementService, token, userId)
        // 로그인 사용자 정보 가져오기
        getMyInfo(supplementService, userId)
        super.onResume()
    }

    // 액션바 붙여주기
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_toolbar_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    // 메뉴 아이템 선택시 불리는 메소드
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.blockedList -> startActivity(Intent(activity, BlockedFriendActivity::class.java ))
        }
        return super.onOptionsItemSelected(item)
    }

    // Fragment 를 안고 있는 Activity 에 붙였을때
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "FriendFragment -onAttach() called")
    }


    // View 가 생성되었을때
    // Fragment 와 레이아웃을 연결시켜준다
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "FriendFragment -onCreateView() called")
        friendsBinding = FragmentFriendsBinding.inflate(inflater, container, false)

        return friendsBinding.root
    }

    // onCreateView 의 리턴값은 View를 가지고 있다.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "FriendFragment -onViewCreated() called")

            with(friendsBinding) {
                // 리사이클러뷰 설정
                friendsList.apply {
                    // 세로로 만들어주기
                    layoutManager = LinearLayoutManager(activity)
                    // 어뎁터 성능을 위해서 추가
                    setHasFixedSize(true)
            }
        }
    }

    private fun getMyInfo(supplementService: IRetrofitUser, userId: String) {
        val token = AppUtil.prefs.getString("token",null)
        val userId = AppUtil.prefs.getString("userId", null)
        supplementService.getUserInfo(token, userId, userId).enqueue(object : Callback<ResponseUser> {
            override fun onResponse(call: Call<ResponseUser>, response: Response<ResponseUser>) {
                val info = response.body()
                // 로그인한 회원정보 넣어주기
                with(friendsBinding) {
                    myProfileName.text = info?.name
                    myStatusMessage.text = info?.message

                    Glide.with(this@FriendFragment)
                        .load(info?.picture)
                        .error(
                            Glide.with(this@FriendFragment)
                            .load(R.drawable.no_image)
                        )
                        .into(myProfileImage)


                    // 내 정보 클릭시
                    myProfile.setOnClickListener {
                        val intent = Intent(activity, ProfileActivity::class.java)
                        intent.putExtra("userId", AppUtil.prefs.getString("userId", null))
                        intent.putExtra("myProfile","myProfile")
                        startActivity(intent)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseUser>, t: Throwable) {
                Log.d("ERROR", "오류: FriendFragment.getMyInfo")
            }

        })
    }

    private fun friendList(supplementService: IRetrofitUser, token: String, userId: String) {
        supplementService.getUserFriendsList(token, userId).enqueue(object: Callback<List<ResponseUser>>{
            override fun onResponse(call: Call<List<ResponseUser>>, response: Response<List<ResponseUser>>) {
                val list = response.body()
                Log.d("리스트", list.toString())
                profileList = list as MutableList<ResponseUser>
                // 어댑터 설정
                friendsBinding.friendsList.adapter = UserAdapter(profileList)
            }

            override fun onFailure(call: Call<List<ResponseUser>>, t: Throwable) {
                Toast.makeText(activity, "친구목록 조회 실패.", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun initRetrofit() {
        retrofit = ServiceURL.getInstance()
        supplementService = retrofit.create(IRetrofitUser::class.java)
    }
}
