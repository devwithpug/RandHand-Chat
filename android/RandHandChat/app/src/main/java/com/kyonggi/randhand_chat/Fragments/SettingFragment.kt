package com.kyonggi.randhand_chat.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.kyonggi.randhand_chat.LoginActivity
import com.kyonggi.randhand_chat.Retrofit.IRetrofit
import com.kyonggi.randhand_chat.Retrofit.Service.ServiceUser
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.FragmentSettingsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class SettingFragment : Fragment() {
    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: IRetrofit

    private lateinit var settingBinding: FragmentSettingsBinding
    lateinit var mGoogleSignInClient: GoogleSignInClient

    companion object {
        const val TAG: String = "로그"

        // 세팅정보 Fragment 를 가져온다
        fun newInstance(): SettingFragment {
            return SettingFragment()
        }
    }

    // 메모리에 올라갔을때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "SettingFragment -onCreate() called")

        initRetrofit()

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
        settingBinding = FragmentSettingsBinding.inflate(inflater, container, false)
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        with(settingBinding) {
            btnSignOut.setOnClickListener {
                signOut()
            }
            btnRevoke.setOnClickListener {
                revokeAccess()
            }
        }
        return settingBinding.root
    }

    // 로그아웃
    private fun signOut() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(requireActivity()) {
                // Update your UI here
                AppUtil.prefs.clear()
                startActivity(Intent(activity, LoginActivity::class.java))
            }
    }

    // 회원 탈퇴
    private fun revokeAccess() {
        mGoogleSignInClient.revokeAccess()
            .addOnCompleteListener(requireActivity()) {
                // Update your UI here
                val userId = AppUtil.prefs.getString("userId",null)
                val token = AppUtil.prefs.getString("token", null)
                userWithdrawal(supplementService, token, userId)
                // SharedPreference 초기화
                AppUtil.prefs.clear()
                Log.d("prefs", "${AppUtil.prefs.getString("userId",null)}")
                startActivity(Intent(activity, LoginActivity::class.java))
            }
    }

    private fun userWithdrawal(supplementService: IRetrofit, token: String, userId: String) {
        supplementService.userWithdrawal(token, userId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("회원탈퇴", "회원탈퇴 성공")

            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
            }

        })
    }


    private fun initRetrofit() {
        retrofit = ServiceUser.getInstance()
        supplementService = retrofit.create(IRetrofit::class.java)
    }

}