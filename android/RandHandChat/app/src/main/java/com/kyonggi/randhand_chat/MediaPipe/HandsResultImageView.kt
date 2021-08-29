package com.kyonggi.randhand_chat.MediaPipe

import android.content.Context
import android.graphics.*
import androidx.appcompat.widget.AppCompatImageView
import com.google.mediapipe.formats.proto.LandmarkProto
import com.google.mediapipe.solutions.hands.Hands
import com.google.mediapipe.solutions.hands.HandsResult

/** An ImageView implementation for displaying MediaPipe Hands results.  */
class HandsResultImageView(context: Context?) : AppCompatImageView(context!!) {

    companion object {
        private const val TAG = "HandsResultImageView"
        private const val LANDMARK_COLOR = Color.RED
        private const val LANDMARK_RADIUS = 15
        private const val CONNECTION_COLOR = Color.GREEN
        private const val CONNECTION_THICKNESS = 10
    }
    init {
        scaleType = ScaleType.FIT_CENTER
    }

    private var latest: Bitmap? = null

    /**
     * Sets a [HandsResult] to render.
     *
     * @param result a [HandsResult] object that contains the solution outputs and the input
     * [Bitmap].
     */
    fun setHandsResult(result: HandsResult?) {
        if (result == null) {
            return
        }
        val bmInput: Bitmap = result.inputBitmap()
        val width: Int = bmInput.width
        val height: Int = bmInput.height
        latest = Bitmap.createBitmap(width, height, bmInput.config)
        val canvas = Canvas(latest!!)
        canvas.drawBitmap(bmInput, Matrix(), null)
        val numHands: Int = result.multiHandLandmarks().size
        for (i in 0 until numHands) {
            drawLandmarksOnCanvas(
                result.multiHandLandmarks()[i].landmarkList, canvas, width, height
            )
        }
    }

    /** Updates the image view with the latest hands result.  */
    fun update() {
        postInvalidate()
        if (latest != null) {
            setImageBitmap(latest)
        }
    }

    // TODO: Better hand landmark and hand connection drawing.
    private fun drawLandmarksOnCanvas(
        handLandmarkList: List<LandmarkProto.NormalizedLandmark>, canvas: Canvas, width: Int, height: Int
    ) {
        // Draw connections.
        for (c in Hands.HAND_CONNECTIONS) {
            val connectionPaint = Paint()
            connectionPaint.color = CONNECTION_COLOR
            connectionPaint.strokeWidth = CONNECTION_THICKNESS.toFloat()
            val start: LandmarkProto.NormalizedLandmark = handLandmarkList[c.start()]
            val end: LandmarkProto.NormalizedLandmark = handLandmarkList[c.end()]
            canvas.drawLine(
                start.x * width,
                start.y * height,
                end.x * width,
                end.y * height,
                connectionPaint
            )
        }
        val landmarkPaint = Paint()
        landmarkPaint.color = LANDMARK_COLOR
        // Draw landmarks.
        for (landmark in handLandmarkList) {
            canvas.drawCircle(
                landmark.x * width,
                landmark.y * height,
                LANDMARK_RADIUS.toFloat(),
                landmarkPaint
            )
        }
    }
}