package com.kyonggi.randhand_chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kyonggi.randhand_chat.Fragments.ChatFragment
import com.kyonggi.randhand_chat.Fragments.FriendFragment
import com.kyonggi.randhand_chat.Fragments.SettingFragment
import com.kyonggi.randhand_chat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener{
    // View Binding 설정
    private lateinit var mainBinding: ActivityMainBinding
    // 멤버 변수들 선언
    // private var friendFragment: FriendFragment? = null
    private lateinit var friendFragment: FriendFragment
    // 의미가 비슷하다
    private lateinit var chatFragment: ChatFragment
    private lateinit var settingFragment: SettingFragment

    companion object {
        const val TAG: String = "로그"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 바인딩 연결
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        Log.d(TAG, "MainActivity -onCreate() called")
        // 네비게이션바 연결
        mainBinding.bottomNavigationView.setOnNavigationItemSelectedListener(this)

        // 처음은 friend fragment 를 넣어줌
        friendFragment = FriendFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainerView,
            friendFragment
        ).commit()
    }

    // 바텀 네비게이션 아이템 클릭 리스너 설정
//    private val onNavigationItemReselectedListener = BottomNavigationView.OnNavigationItemSelectedListener {
//        when (it.itemId) {
//            R.id.menu_friends -> {
//                Log.d(TAG,"친구창 버튼")
//            }
//            R.id.menu_chattings -> {
//                Log.d(TAG,"채팅창 버튼")
//            }
//            R.id.menu_settings -> {
//                Log.d(TAG,"세팅 버튼")
//            }
//        }
//       true
//    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "MainActivity -OnNavigationItemSelected() called")
        when (item.itemId) {
            R.id.menu_friends -> {
                Log.d(TAG,"친구창 버튼")
                friendFragment = FriendFragment.newInstance()
                // Fragment 관리해준다
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView,
                    friendFragment
                ).commit()
            }
            R.id.menu_chattings -> {
                Log.d(TAG,"채팅창 버튼")
                chatFragment = ChatFragment.newInstance()
                // Fragment 관리해준다
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView,
                    chatFragment).commit()
            }
            R.id.menu_settings -> {
                Log.d(TAG,"세팅 버튼")
                settingFragment = SettingFragment.newInstance()
                // Fragment 관리해준다
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView,
                    settingFragment).commit()
            }
        }
        return true
    }
}