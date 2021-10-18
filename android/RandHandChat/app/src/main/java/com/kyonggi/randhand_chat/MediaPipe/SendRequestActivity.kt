package com.kyonggi.randhand_chat.MediaPipe

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.mediapipe.solutions.hands.HandLandmark
import com.google.mediapipe.solutions.hands.Hands
import com.google.mediapipe.solutions.hands.HandsOptions
import com.google.mediapipe.solutions.hands.HandsResult
import com.kyonggi.randhand_chat.Activity.ProgressActivity
import com.kyonggi.randhand_chat.Retrofit.GestureServiceURL
import com.kyonggi.randhand_chat.Retrofit.IRetrofit.IRetrofitGesture
import com.kyonggi.randhand_chat.Util.AppUtil
import com.kyonggi.randhand_chat.databinding.ActivitySendRequestBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream


class SendRequestActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SendRequestActivity"
    }

    private lateinit var retrofit: Retrofit
    private lateinit var supplementServiceGesture: IRetrofitGesture

    private var hands: Hands? = null
    private var imageView: HandsResultImageView? = null

    val binding by lazy { ActivitySendRequestBinding.inflate(layoutInflater) }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        imageView = HandsResultImageView(this)
        // initialize MediaPipe
        setupStaticImageModePipeline()
        initGestureRetrofit()

        // get byteArrayImage from MediaPipeActivity
        val byteArrayImage = intent.getByteArrayExtra("ByteArrayImage")

        // byteArray to Bitmap & send to MediaPipe
        val bitmap = BitmapFactory.decodeByteArray(byteArrayImage, 0, byteArrayImage?.size!!)

        hands?.send(bitmap)

        // 1. SEND TO SERVER
        binding.imageSend.setOnClickListener {
            val gesture = bitmap.toBase64String()

            // SEND MATCH REQUEST TO SERVER
            sendGestureMatching(supplementServiceGesture, gesture)

            /**
             * Progress bar에서 매칭까지 대기
             * 매칭 취소도 progres bar에서 서버로 요청
             * 매칭이 되면 지금은 바로 채팅방으로 이동
             */
        }

        // 2. CANCEL
        binding.cancel.setOnClickListener {
            startActivity(Intent(this, MediaPipeActivity::class.java))
            finish()
        }
    }

    private fun sendGestureMatching(supplementServiceGesture: IRetrofitGesture, gesture: String) {
        val token = AppUtil.prefs.getString("token", null)
        val userId = AppUtil.prefs.getString("userId", null)
        val body = mapOf("gesture" to gesture)
        supplementServiceGesture.sendGestureMatching(token, userId, body).enqueue(object :
            Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                when (response.code()) {
                    200 -> {
                        val intent = Intent(this@SendRequestActivity, ProgressActivity::class.java)
                        startActivity(intent)
                        stopCurrentPipeline()
                        finish()
                        Log.d("GESTURE RESPONSE", "RESPONSE")
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("GESTURE ERROR", "ERROR")
            }

        })
    }

    /** The core MediaPipe Hands setup workflow for its static image mode.  */
    private fun setupStaticImageModePipeline() {
        // Initializes a new MediaPipe Hands instance in the static image mode.
        hands = Hands(
            this,
            HandsOptions.builder()
                .setStaticImageMode(true)
                .setMaxNumHands(1)
                .setRunOnGpu(false)
                .build()
        )

        // Connects MediaPipe Hands to the user-defined HandsResultImageView.
        hands!!.setResultListener { handsResult ->
            logWristLandmark(handsResult,  /*showPixelValues=*/true)
            imageView?.setHandsResult(handsResult)
            runOnUiThread { imageView?.update() }
        }
        hands!!.setErrorListener { message, e ->
            Log.e(
                SendRequestActivity.TAG,
                "MediaPipe Hands error:$message"
            )
        }


        // Updates the preview layout. HandsResultImageView
        val frameLayout = binding.previewDisplayLayout
        frameLayout.removeAllViewsInLayout()
        imageView?.setImageDrawable(null)
        frameLayout.addView(imageView)
        imageView?.visibility = View.VISIBLE
    }

    private fun logWristLandmark(result: HandsResult, showPixelValues: Boolean) {
        val wristLandmark = Hands.getHandLandmark(result, 0, HandLandmark.WRIST)
        // For Bitmaps, show the pixel values. For texture inputs, show the normalized coordinates.
        if (showPixelValues) {
            val width: Int = result.inputBitmap().width
            val height: Int = result.inputBitmap().height
            Log.i(
                SendRequestActivity.TAG,
                String.format(
                    "MediaPipe Hand wrist coordinates (pixel values): x=%f, y=%f",
                    wristLandmark.x * width, wristLandmark.y * height
                )
            )
        } else {
            Log.i(
                SendRequestActivity.TAG,
                String.format(
                    "MediaPipe Hand wrist normalized coordinates (value range: [0, 1]): x=%f, y=%f, z=%f",
                    wristLandmark.x, wristLandmark.y, wristLandmark.z
                )
            )
        }
    }

    // extension function to convert bitmap to base64 string
    private fun Bitmap.toBase64String():String {
        ByteArrayOutputStream().apply {
            compress(Bitmap.CompressFormat.JPEG, 70, this)
            return Base64.encodeToString(toByteArray(), Base64.DEFAULT)
        }
    }

    private fun initGestureRetrofit() {
        retrofit = GestureServiceURL.getInstance()
        supplementServiceGesture = retrofit.create(IRetrofitGesture::class.java)
    }

    private fun stopCurrentPipeline() {
        hands?.close()
    }
}