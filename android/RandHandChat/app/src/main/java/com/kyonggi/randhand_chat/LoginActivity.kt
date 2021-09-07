package com.kyonggi.randhand_chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.kyonggi.randhand_chat.Domain.User.Client
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitUser
import com.kyonggi.randhand_chat.Retrofit.ServiceURL
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class LoginActivity : AppCompatActivity() {
    private lateinit var loginBinding: ActivityLoginBinding
    private val RC_SIGN_IN = 9001
    lateinit var mGoogleSignInClient: GoogleSignInClient

    // Retrofit 관련 변수 생성
    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: IRetrofitUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)
        // Retrofit init
        initRetrofit()

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        // 구글 버튼 와드하게 설정
        loginBinding.googleLoginButton.setSize(SignInButton.SIZE_WIDE)

        loginBinding.googleLoginButton.setOnClickListener {
            signIn()
        }

    }

    // 로그인 페이지가 시작하였을때
    override fun onStart() {
        super.onStart()
        // 기존에 로그인 했던 계정을 확인한다
        val gsa = GoogleSignIn.getLastSignedInAccount(this)

        // 기존에 로그인 여부
        if (gsa != null) {
            getUserIdRequest(supplementService, "google", gsa.email!!)
            finish()
        }
    } // onStart End

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(
            signInIntent, RC_SIGN_IN
        )
    }

    // Deprecate 되었다
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    // 사용자 정보 가져오기
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>?) {
        try {
            val account = completedTask?.getResult(ApiException::class.java)
            // Signed in successfully
            // 구글에서 가져온 정보로 백앤드 서버에 회원가입 요청

            val auth = "google"
            val email = account?.email
            val googleFirstName = account?.givenName ?: ""
            val googleLastName = account?.familyName ?: ""
            val name = googleLastName+googleFirstName
            val picture = account?.photoUrl
            val photo : String?
            if (picture == null) {
                photo = null
            } else {
                photo = picture.toString()
            }
            val client = Client(null, auth, email, name, null, photo)

            // 이메일 저장
            email?.let { AppUtil.prefs.setString("email", it) }

            // 백앤드 서버에 회원가입
            signInClient(supplementService, client)

        } catch (e: ApiException) {
            // Sign in was unsuccessful

        }
    }
    private fun getUserIdRequest(supplementService: IRetrofitUser, auth: String, email: String) {
        supplementService.requestUserId(auth, email).enqueue(object : Callback<Client> {
            override fun onResponse(call: Call<Client>, response: Response<Client>) {
                val body = response.body()
                body?.userId?.let { AppUtil.prefs.setString("userId", it) }
                body?.name?.let { AppUtil.prefs.setString("userName", it) }
                body?.picture?.let { AppUtil.prefs.setString("image", it) }
                body?.message?.let { AppUtil.prefs.setString("status", it) }
                val userId = AppUtil.prefs.getString("userId", null)
                val client = Client(userId, null, email, null, null, null)
                loginClient(supplementService, client)
            }

            override fun onFailure(call: Call<Client>, t: Throwable) {
                Log.d("ERROR", "LoginActivity().getUserIdRequest 에러")
            }

        })
    }

    private fun signInClient(supplementService: IRetrofitUser, client: Client) {
        supplementService.signupClient(client).enqueue(object : Callback<Client>{
            override fun onResponse(call: Call<Client>, response: Response<Client>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.userId?.let { AppUtil.prefs.setString("userId", it) }
                    body?.name?.let { AppUtil.prefs.setString("userName", it) }
                    body?.picture?.let { AppUtil.prefs.setString("image", it) }
                    body?.message?.let { AppUtil.prefs.setString("status", it) }
                    val client = Client(body?.userId, null, body?.email, null, null, null)
                    loginClient(supplementService, client)
                } else {
                    val email = AppUtil.prefs.getString("email", null)
                    // 이미 회원으로 등록되어 있으면 로그인
                    // 아이디 얻은후 로그인
                    getUserIdRequest(supplementService, "google", email)
                }
            }

            override fun onFailure(call: Call<Client>, t: Throwable) {
            }
        })
    }

    // 로그인 후 토근 + ID 저장해두고 Header 값으로 사용
    private fun loginClient(supplementService: IRetrofitUser, client: Client) {
        supplementService.requestLogin(client).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                val token = response.headers()["token"]
                AppUtil.prefs.setString("token", "Bearer $token")
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "로그인에 실패하였습니다.",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initRetrofit() {
        retrofit = ServiceURL.getInstance()
        supplementService = retrofit.create(IRetrofitUser::class.java)
    }
}
