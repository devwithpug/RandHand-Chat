package com.kyonggi.randhand_chat.Fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kyonggi.randhand_chat.databinding.FragmentChattingsBinding

class ChatFragment : Fragment() {
    companion object {
        const val TAG : String = "로그"
        // 채팅 정보에대한 Fragment 생성
        fun newInstance(): ChatFragment {
            return ChatFragment()
        }
    }

    // 메모리에 올라갔을때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "ChatFragment -onCreate() called")
    }
    // Fragment 를 안고 있는 Activity 에 붙였을때
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "ChatFragment -onAttach() called")
    }

    // View 가 생성되었을때
    // Fragment 와 레이아웃을 연결시켜준다
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "ChatFragment -onCreateView() called")
//        val view = inflater.inflate(R.layout.fragment_chattings, container, false )
        val binding = FragmentChattingsBinding.inflate(inflater, container, false)
        return binding.root
    }

}