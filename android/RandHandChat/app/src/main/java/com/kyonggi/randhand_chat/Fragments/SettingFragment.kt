package com.kyonggi.randhand_chat.Fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kyonggi.randhand_chat.R
import com.kyonggi.randhand_chat.databinding.FragmentSettingsBinding

class SettingFragment : Fragment() {
    companion object {
        const val TAG : String = "로그"
        // 세팅정보 Fragment 를 가져온다
        fun newInstance(): SettingFragment {
            return SettingFragment()
        }
    }

    // 메모리에 올라갔을때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "SettingFragment -onCreate() called")
    }
    // Fragment 를 안고 있는 Activity 에 붙였을때
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "SettingFragment -onAttach() called")
    }

    // View 가 생성되었을때
    // Fragment 와 레이아웃을 연결시켜준다
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "SettingFragment -onCreateView() called")
        // 기존 방식
        // val view = inflater.inflate(R.layout.fragment_settings, container, false )
        // 뷰 바인딩 가져오기
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

}