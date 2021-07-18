package com.kyonggi.randhand_chat.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment

class UserProfileFragment: Fragment(){

    companion object {
        const val TAG : String = "로그"
        // 친구정보들에대한 Fragment 를 가져온다
        fun newInstance(): UserProfileFragment {
            return UserProfileFragment()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}