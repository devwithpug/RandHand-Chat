package com.kyonggi.randhand_chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.kyonggi.randhand_chat.databinding.ActivityProgressBinding
import kotlin.concurrent.thread

class ProgressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProgressBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // <- 메인쓰레드
        showProgress(true)
        thread(start = true) { // -> 서브쓰레드
            /**
             * 매칭하는중에는 계속 실행 -> 시간설정
             */
            Thread.sleep(3000)
            // 화면에 영향을 미치는 코드는
            // 메인쓰레드로 다시 보내줘야한다
            runOnUiThread {
                showProgress(false)
                showProgressComplete()
            }
        } // 서브 쓰레드
        // 메인 쓰레드
    }

    fun showProgress(show: Boolean) {
        binding.progressLayout.visibility = if (show) View.VISIBLE else View.GONE
    }
    fun showProgressComplete() {
        binding.progressCompleteLayout.visibility = View.VISIBLE
        /**
         * 매칭이 성공시 2초후 채팅방으로 이동
         */
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }

}