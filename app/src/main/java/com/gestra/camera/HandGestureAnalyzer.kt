package com.gestra.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.io.Closeable
import kotlin.math.abs
import kotlin.math.hypot

internal enum class HandGesture {
    NONE, PEACE, OPEN_PALM, THUMBS_UP, SHAKA
}

internal class GestureStabilizer(private var requiredFrames: Int) {
    private var candidate = HandGesture.NONE
    private var candidateFrames = 0
    private var stable = HandGesture.NONE

    fun update(detected: HandGesture): Pair<HandGesture, HandGesture>? {
        if (detected == candidate) {
            candidateFrames++
        } else {
            candidate = detected
            candidateFrames = 1
        }
        if (candidateFrames >= requiredFrames && stable != candidate) {
            val previous = stable
            stable = candidate
            return previous to stable
        }
        return null
    }
}

internal class HandGestureAnalyzer(
    context: Context,
    private val sensitivity: Float,
    stableFrames: Int,
    private val onTransition: (HandGesture, HandGesture) -> Unit,
    private val onFailure: (String) -> Unit
) : ImageAnalysis.Analyzer, Closeable {
    private val appContext = context.applicationContext
    private val stabilizer = GestureStabilizer(stableFrames.coerceIn(2, 7))
    private val landmarker: HandLandmarker
    private var lastInferenceAt = 0L
    private var lastTimestamp = 0L
    private var closed = false

    init {
        val base = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()
        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(base)
            .setRunningMode(RunningMode.VIDEO)
            .setNumHands(1)
            .setMinHandDetectionConfidence(sensitivity.coerceIn(0.25f, 0.9f))
            .setMinHandPresenceConfidence(sensitivity.coerceIn(0.25f, 0.9f))
            .setMinTrackingConfidence(0.45f)
            .build()
        landmarker = HandLandmarker.createFromOptions(appContext, options)
    }

    override fun analyze(imageProxy: ImageProxy) {
        var source: Bitmap? = null
        var corrected: Bitmap? = null
        try {
            if (closed) return
            val now = SystemClock.elapsedRealtime()
            // A 12–15 FPS inference ceiling saves CPU while the camera preview remains smooth.
            if (now - lastInferenceAt < 75L) return
            lastInferenceAt = now

            source = imageProxy.toBitmap()
            corrected = rotate(source!!, imageProxy.imageInfo.rotationDegrees)
            val mpImage = BitmapImageBuilder(corrected!!).build()
            val timestamp = maxOf(SystemClock.uptimeMillis(), lastTimestamp + 1L)
            lastTimestamp = timestamp
            val result = landmarker.detectForVideo(mpImage, timestamp)
            val gesture = classify(result)
            stabilizer.update(gesture)?.let { (previous, current) ->
                onTransition(previous, current)
            }
        } catch (error: Throwable) {
            Log.e("GESTRA", "Hand analysis failed", error)
            onFailure(error.message ?: "Hand detection failed")
        } finally {
            if (corrected != null && corrected !== source) corrected?.recycle()
            source?.recycle()
            imageProxy.close()
        }
    }

    private fun rotate(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees % 360 == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun classify(result: HandLandmarkerResult): HandGesture {
        val hand = result.landmarks().firstOrNull() ?: return HandGesture.NONE
        if (hand.size < 21) return HandGesture.NONE

        fun x(index: Int) = hand[index].x()
        fun y(index: Int) = hand[index].y()
        fun distance(a: Int, b: Int): Double = hypot((x(a) - x(b)).toDouble(), (y(a) - y(b)).toDouble())

        // The camera frame is rotated to portrait before classification. The rules are
        // deliberately conservative; a stable gesture must survive several frames.
        val indexUp = y(8) < y(6) - 0.025f
        val middleUp = y(12) < y(10) - 0.025f
        val ringUp = y(16) < y(14) - 0.025f
        val pinkyUp = y(20) < y(18) - 0.025f
        val thumbOut = distance(4, 5) > 0.16 || abs(x(4) - x(2)) > 0.12f

        val indexFolded = y(8) > y(6) - 0.005f
        val middleFolded = y(12) > y(10) - 0.005f
        val ringFolded = y(16) > y(14) - 0.005f
        val pinkyFolded = y(20) > y(18) - 0.005f
        val thumbUp = y(4) < y(3) - 0.035f && y(4) < y(0) - 0.08f

        return when {
            thumbOut && pinkyUp && indexFolded && middleFolded && ringFolded -> HandGesture.SHAKA
            thumbUp && indexFolded && middleFolded && ringFolded && pinkyFolded -> HandGesture.THUMBS_UP
            indexUp && middleUp && !ringUp && !pinkyUp -> HandGesture.PEACE
            indexUp && middleUp && ringUp && pinkyUp && thumbOut -> HandGesture.OPEN_PALM
            else -> HandGesture.NONE
        }
    }

    override fun close() {
        closed = true
        try {
            landmarker.close()
        } catch (_: Exception) {
        }
    }
}
