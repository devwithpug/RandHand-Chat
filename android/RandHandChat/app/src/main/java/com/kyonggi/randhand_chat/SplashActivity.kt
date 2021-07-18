package com.kyonggi.randhand_chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import androidx.constraintlayout.motion.widget.MotionLayout
import com.kyonggi.randhand_chat.databinding.ActivitySplashBinding
import java.util.logging.Handler

class SplashActivity : AppCompatActivity() {
//    private val SPLASH_VIEW_TIME: Long by lazy { 1500 }
    private lateinit var splashBinding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(splashBinding.root)

        val motionLayout: MotionLayout = findViewById<MotionLayout>(R.id.motionLayout)
        motionLayout.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                finish()
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
            }

        })

//        Handler(Looper.getMainLooper()).postDelayed({
//            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
//            startActivity(intent)
//            finish()
//        }, SPLASH_VIEW_TIME)
    }
}