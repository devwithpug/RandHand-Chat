package com.kyonggi.randhand_chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kyonggi.randhand_chat.Fragments.ChatListFragment
import com.kyonggi.randhand_chat.Fragments.FriendFragment
import com.kyonggi.randhand_chat.Fragments.SettingFragment
import com.kyonggi.randhand_chat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    // View Binding 설정
    private lateinit var mainBinding: ActivityMainBinding

    private lateinit var friendFragment: FriendFragment
    private lateinit var chatListFragment: ChatListFragment
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
        mainBinding.toolbar.title = "친구목록"
        friendFragment = FriendFragment.newInstance()
        supportFragmentManager.beginTransaction().add(
            R.id.fragmentContainerView,
            friendFragment
        ).commit()

        // toolbar 붙여주기
        setSupportActionBar(mainBinding.toolbar)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "MainActivity -OnNavigationItemSelected() called")
        when (item.itemId) {
            R.id.menu_friends -> {
                Log.d(TAG, "친구창 버튼")
                mainBinding.toolbar.title = "친구목록"
                friendFragment = FriendFragment.newInstance()
                // Fragment 관리해준다
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragmentContainerView,
                    friendFragment
                ).commit()
            }
            R.id.menu_chattings -> {
                Log.d(TAG, "채팅창 버튼")
                mainBinding.toolbar.title = "채팅목록"
                chatListFragment = ChatListFragment.newInstance()
                // Fragment 관리해준다
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragmentContainerView,
                    chatListFragment
                ).commit()
            }
            R.id.menu_settings -> {
                Log.d(TAG, "세팅 버튼")
                mainBinding.toolbar.title = "설정"
                settingFragment = SettingFragment.newInstance()
                // Fragment 관리해준다
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragmentContainerView,
                    settingFragment
                ).commit()
            }
        }
        return true
    }
}