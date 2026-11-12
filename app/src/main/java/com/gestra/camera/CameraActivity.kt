package com.gestra.camera

import android.content.ContentValues
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Size
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : ComponentActivity() {
    private lateinit var root: FrameLayout
    private lateinit var previewView: PreviewView
    private lateinit var gridOverlay: GridOverlay
    private lateinit var gestureStatus: TextView
    private lateinit var shutterButton: TextView
    private lateinit var flashButton: TextView
    private lateinit var cameraExecutor: ExecutorService
    private val mainHandler = Handler(Looper.getMainLooper())

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var analyzer: HandGestureAnalyzer? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var torchEnabled = false
    private var captureInProgress = false
    private var cameraReady = false
    private var lastErrorToastAt = 0L
    private var appliedSensitivity = -1
    private var appliedStableFrames = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = GestraUi.BG
        cameraExecutor = Executors.newSingleThreadExecutor()
        buildCameraUi()
        if (!hasCameraPermission()) {
            Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        startCamera()
    }

    private fun buildCameraUi() {
        root = FrameLayout(this).apply { setBackgroundColor(GestraUi.BG) }
        previewView = PreviewView(this).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
            setBackgroundColor(0xFF05070A.toInt())
        }
        root.addView(previewView, FrameLayout.LayoutParams(-1, -1))

        gridOverlay = GridOverlay(this).apply { visibility = View.GONE }
        root.addView(gridOverlay, FrameLayout.LayoutParams(-1, -1))

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(GestraUi.dp(this@CameraActivity, 16f), GestraUi.dp(this@CameraActivity, 12f),
                GestraUi.dp(this@CameraActivity, 16f), GestraUi.dp(this@CameraActivity, 12f))
            background = GestraUi.card(this@CameraActivity, 0xDD080B10.toInt(), 16f)
        }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brand.addView(GestraUi.text(this, "GESTRA", 14f, GestraUi.TEXT, true).apply { letterSpacing = 0.18f })
        brand.addView(GestraUi.text(this, "LIVE CAMERA  /  DEMO TESTING", 8f, GestraUi.MUTED, true).apply {
            letterSpacing = 0.08f
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = GestraUi.dp(this@CameraActivity, 5f) })
        top.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(GestraUi.button(this, "SETTINGS".uppercase()).apply {
            setPadding(GestraUi.dp(this@CameraActivity, 13f), GestraUi.dp(this@CameraActivity, 12f),
                GestraUi.dp(this@CameraActivity, 13f), GestraUi.dp(this@CameraActivity, 12f))
            setOnClickListener { startActivity(Intent(this@CameraActivity, SettingsActivity::class.java)) }
        })
        root.addView(top, FrameLayout.LayoutParams(-1, -2, Gravity.TOP).apply {
            setMargins(GestraUi.dp(this@CameraActivity, 12f), GestraUi.dp(this@CameraActivity, 15f),
                GestraUi.dp(this@CameraActivity, 12f), 0)
        })

        gestureStatus = GestraUi.text(this, "ENGINE  /  STARTING", 9f, GestraUi.TEXT, true).apply {
            letterSpacing = 0.08f
            gravity = Gravity.CENTER
            setPadding(GestraUi.dp(this@CameraActivity, 13f), GestraUi.dp(this@CameraActivity, 9f),
                GestraUi.dp(this@CameraActivity, 13f), GestraUi.dp(this@CameraActivity, 9f))
            background = GestraUi.card(this@CameraActivity, 0xDD080B10.toInt(), 11f)
        }
        root.addView(gestureStatus, FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.CENTER_HORIZONTAL).apply {
            topMargin = GestraUi.dp(this@CameraActivity, 94f)
        })

        val bottomPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(GestraUi.dp(this@CameraActivity, 16f), GestraUi.dp(this@CameraActivity, 15f),
                GestraUi.dp(this@CameraActivity, 16f), GestraUi.dp(this@CameraActivity, 22f))
            background = GestraUi.card(this@CameraActivity, 0xE9080B10.toInt(), 20f)
        }
        bottomPanel.addView(GestraUi.text(this,
            "V SIGN  /  BLUR     •     PALM  /  PHOTO     •     THUMBS UP  /  APP     •     SHAKA  /  FLASH",
            8f, GestraUi.MUTED, true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.02f
        })
        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val flip = GestraUi.button(this, "FLIP").apply {
            setOnClickListener {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                    CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                bindUseCases()
            }
        }
        controls.addView(flip, LinearLayout.LayoutParams(0, GestraUi.dp(this, 44f), 1f))
        shutterButton = TextView(this).apply {
            text = ""
            gravity = Gravity.CENTER
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(GestraUi.ACCENT)
                setStroke(GestraUi.dp(this@CameraActivity, 5f), 0x66FFFFFF)
            }
            isClickable = true
            isFocusable = true
            setOnClickListener { capturePhoto("MANUAL") }
        }
        controls.addView(shutterButton, LinearLayout.LayoutParams(GestraUi.dp(this, 72f), GestraUi.dp(this, 72f)).apply {
            leftMargin = GestraUi.dp(this@CameraActivity, 20f)
            rightMargin = GestraUi.dp(this@CameraActivity, 20f)
        })
        flashButton = GestraUi.button(this, "FLASH OFF").apply {
            setOnClickListener { toggleTorch() }
        }
        controls.addView(flashButton, LinearLayout.LayoutParams(0, GestraUi.dp(this, 44f), 1f))
        bottomPanel.addView(controls, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = GestraUi.dp(this@CameraActivity, 15f)
        })
        bottomPanel.addView(GestraUi.text(this, "HD AUTO  /  ON-DEVICE PROCESSING", 8f, GestraUi.MUTED, true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.1f
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = GestraUi.dp(this@CameraActivity, 12f) })
        root.addView(bottomPanel, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM).apply {
            setMargins(GestraUi.dp(this@CameraActivity, 12f), 0, GestraUi.dp(this@CameraActivity, 12f),
                GestraUi.dp(this@CameraActivity, 12f))
        })
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            (top.layoutParams as FrameLayout.LayoutParams).apply {
                topMargin = GestraUi.dp(this@CameraActivity, 15f) + systemBars.top
            }.also { top.layoutParams = it }
            (gestureStatus.layoutParams as FrameLayout.LayoutParams).apply {
                topMargin = GestraUi.dp(this@CameraActivity, 94f) + systemBars.top
            }.also { gestureStatus.layoutParams = it }
            (bottomPanel.layoutParams as FrameLayout.LayoutParams).apply {
                bottomMargin = GestraUi.dp(this@CameraActivity, 12f) + systemBars.bottom
            }.also { bottomPanel.layoutParams = it }
            insets
        }
        setContentView(root)
        ViewCompat.requestApplyInsets(root)
        refreshSettings()
    }

    private fun refreshSettings() {
        gridOverlay.visibility = if (GestraPrefs.bool(this, GestraPrefs.GRID, false)) View.VISIBLE else View.GONE
        if (cameraReady) {
            gestureStatus.text = if (GestraPrefs.bool(this, GestraPrefs.DEBUG_STATUS, true))
                "ENGINE  /  READY" else "GESTRA  /  ACTIVE"
        }
    }

    private fun startCamera() {
        if (cameraReady) return
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            try {
                cameraProvider = future.get()
                bindUseCases()
                cameraReady = camera != null && analyzer != null
                if (cameraReady) gestureStatus.text = "ENGINE  /  READY"
            } catch (error: Exception) {
                showEngineError("Camera could not start: ${error.message ?: "unknown error"}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindUseCases() {
        val provider = cameraProvider ?: return
        if (!hasCameraPermission()) return
        val oldAnalyzer = analyzer
        analyzer = null
        provider.unbindAll()
        if (oldAnalyzer != null && !cameraExecutor.isShutdown) {
            cameraExecutor.execute { oldAnalyzer.close() }
        }

        val hdSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
            .setResolutionStrategy(
                ResolutionStrategy(Size(1920, 1080), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
            )
            .build()
        val analysisSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
            .setResolutionStrategy(
                ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER)
            )
            .build()

        val preview = Preview.Builder()
            .setResolutionSelector(hdSelector)
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        val sensitivityValue = GestraPrefs.int(this, GestraPrefs.SENSITIVITY, 55)
        val stableFrames = GestraPrefs.int(this, GestraPrefs.STABLE_FRAMES, 3)
        val sensitivity = sensitivityValue / 100f
        appliedSensitivity = sensitivityValue
        appliedStableFrames = stableFrames
        try {
            analyzer = HandGestureAnalyzer(
                this,
                sensitivity,
                stableFrames,
                onTransition = { previous, current -> runOnUiThread { onGestureTransition(previous, current) } },
                onFailure = { message ->
                    val now = System.currentTimeMillis()
                    if (now - lastErrorToastAt > 5000L) {
                        lastErrorToastAt = now
                        runOnUiThread { gestureStatus.text = "ENGINE  /  CHECK MODEL" }
                    }
                    android.util.Log.w("GESTRA", message)
                }
            )
        } catch (error: Exception) {
            showEngineError("Hand model could not load: ${error.message ?: "missing model"}")
            return
        }

        val analysis = ImageAnalysis.Builder()
            .setResolutionSelector(analysisSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        analysis.setAnalyzer(cameraExecutor, analyzer!!)

        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        try {
            camera = provider.bindToLifecycle(this, selector, preview, imageCapture, analysis)
            if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                torchEnabled = false
                updateFlashButton()
            }
            if (torchEnabled && lensFacing == CameraSelector.LENS_FACING_BACK && camera?.cameraInfo?.hasFlashUnit() == true) {
                camera?.cameraControl?.enableTorch(true)
            }
            refreshSettings()
        } catch (error: Exception) {
            showEngineError("Could not bind camera: ${error.message ?: "unsupported camera"}")
        }
    }

    private fun onGestureTransition(previous: HandGesture, current: HandGesture) {
        if (previous == HandGesture.PEACE && current != HandGesture.PEACE) setPreviewBlur(false)
        when (current) {
            HandGesture.NONE -> {
                if (previous == HandGesture.PEACE) setPreviewBlur(false)
                setStatus("ENGINE  /  READY")
            }
            HandGesture.PEACE -> {
                setStatus("V SIGN  /  BLUR ACTIVE")
                if (GestraPrefs.bool(this, GestraPrefs.PEACE_BLUR, true)) setPreviewBlur(true)
                feedback()
            }
            HandGesture.OPEN_PALM -> {
                setStatus("OPEN PALM  /  CAPTURE")
                if (GestraPrefs.bool(this, GestraPrefs.PALM_CAPTURE, true)) capturePhoto("GESTURE")
                feedback()
            }
            HandGesture.THUMBS_UP -> {
                setStatus("THUMBS UP  /  APP LAUNCH")
                if (GestraPrefs.bool(this, GestraPrefs.THUMB_LAUNCH, true)) launchSelectedApp()
                feedback()
            }
            HandGesture.SHAKA -> {
                setStatus("SHAKA  /  FLASH ON")
                if (GestraPrefs.bool(this, GestraPrefs.SHAKA_FLASH, true)) setTorch(true)
                feedback()
            }
        }
    }

    private fun setStatus(message: String) {
        if (GestraPrefs.bool(this, GestraPrefs.DEBUG_STATUS, true)) gestureStatus.text = message
    }

    private fun setPreviewBlur(enabled: Boolean) {
        val duration = 180L
        val startRadius = if (enabled) 0f else GestraPrefs.int(this, GestraPrefs.BLUR_RADIUS, 18).toFloat()
        val endRadius = if (enabled) GestraPrefs.int(this, GestraPrefs.BLUR_RADIUS, 18).toFloat() else 0f
        android.animation.ValueAnimator.ofFloat(startRadius, endRadius).apply {
            this.duration = duration
            addUpdateListener { animator ->
                val radius = animator.animatedValue as Float
                previewView.setRenderEffect(
                    if (radius < 0.5f) null else RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
                )
            }
            start()
        }
    }

    private fun capturePhoto(source: String) {
        if (captureInProgress) return
        val capture = imageCapture
        if (capture == null) {
            showToast("Camera is not ready yet.")
            return
        }
        val delaySeconds = if (source == "GESTURE") GestraPrefs.int(this, GestraPrefs.SHUTTER_DELAY, 0) else 0
        if (delaySeconds > 0) {
            captureInProgress = true
            runGestureCountdown(capture, delaySeconds)
        } else {
            performCapture(capture)
        }
    }

    private fun runGestureCountdown(capture: ImageCapture, secondsLeft: Int) {
        if (secondsLeft <= 0) {
            captureInProgress = false
            performCapture(capture)
            return
        }
        gestureStatus.text = "CAPTURE IN  /  $secondsLeft"
        mainHandler.postDelayed({ runGestureCountdown(capture, secondsLeft - 1) }, 1000L)
    }

    private fun performCapture(capture: ImageCapture) {
        if (captureInProgress) return
        captureInProgress = true
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "GESTRA_$timestamp.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/GESTRA")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val output = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ).build()

        capture.takePicture(output, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                captureInProgress = false
                result.savedUri?.let { uri ->
                    val complete = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
                    try { contentResolver.update(uri, complete, null, null) } catch (_: Exception) { }
                }
                gestureStatus.text = "PHOTO SAVED  /  GALLERY"
                showToast("Photo saved to Pictures/GESTRA")
                playShutterCue()
                feedback()
            }

            override fun onError(exception: ImageCaptureException) {
                captureInProgress = false
                gestureStatus.text = "CAPTURE FAILED  /  RETRY"
                showToast("Photo capture failed: ${exception.message ?: "camera error"}")
                android.util.Log.e("GESTRA", "Photo capture failed", exception)
            }
        })
    }

    private fun launchSelectedApp() {
        val target = GestraPrefs.string(this, GestraPrefs.TARGET_PACKAGE)
        if (target.isBlank()) {
            showToast("Choose an app first in Settings.")
            startActivity(Intent(this, SettingsActivity::class.java))
            return
        }
        val launch = packageManager.getLaunchIntentForPackage(target)
        if (launch == null) {
            showToast("The selected app is not available. Choose another app.")
            return
        }
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(launch)
        } catch (error: Exception) {
            showToast("Could not open the selected app.")
            android.util.Log.e("GESTRA", "Launch app failed", error)
        }
    }

    private fun toggleTorch() = setTorch(!torchEnabled)

    private fun setTorch(enabled: Boolean) {
        val activeCamera = camera
        if (activeCamera == null || lensFacing != CameraSelector.LENS_FACING_BACK || !activeCamera.cameraInfo.hasFlashUnit()) {
            if (enabled) showToast("Flash is not available on this camera.")
            torchEnabled = false
            updateFlashButton()
            return
        }
        try {
            activeCamera.cameraControl.enableTorch(enabled)
            torchEnabled = enabled
            updateFlashButton()
        } catch (error: Exception) {
            showToast("Could not control flash on this device.")
            android.util.Log.w("GESTRA", "Torch change failed", error)
        }
    }

    private fun updateFlashButton() {
        flashButton.text = if (torchEnabled) "FLASH ON" else "FLASH OFF"
        flashButton.setTextColor(if (torchEnabled) GestraUi.ACCENT else GestraUi.TEXT)
    }

    private fun playShutterCue() {
        if (GestraPrefs.bool(this, GestraPrefs.SOUND, true)) {
            try {
                val generator = android.media.ToneGenerator(android.media.AudioManager.STREAM_SYSTEM, 55)
                generator.startTone(android.media.ToneGenerator.TONE_PROP_ACK, 95)
                mainHandler.postDelayed({ generator.release() }, 160L)
            } catch (_: Exception) {
            }
        }
    }

    private fun feedback() {
        if (GestraPrefs.bool(this, GestraPrefs.HAPTICS, true)) {
            try { root.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK) } catch (_: Exception) { }
        }
    }

    private fun showEngineError(message: String) {
        gestureStatus.text = "ENGINE  /  ERROR"
        showToast(message)
        android.util.Log.e("GESTRA", message)
    }

    private fun showToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED

    override fun onResume() {
        super.onResume()
        if (::root.isInitialized) {
            refreshSettings()
            val sensitivity = GestraPrefs.int(this, GestraPrefs.SENSITIVITY, 55)
            val stableFrames = GestraPrefs.int(this, GestraPrefs.STABLE_FRAMES, 3)
            if (cameraReady && cameraProvider != null &&
                (sensitivity != appliedSensitivity || stableFrames != appliedStableFrames)) {
                bindUseCases()
            }
        }
    }

    override fun onPause() {
        try { camera?.cameraControl?.enableTorch(false) } catch (_: Exception) { }
        if (torchEnabled) {
            torchEnabled = false
            if (::flashButton.isInitialized) updateFlashButton()
        }
        super.onPause()
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        try { cameraProvider?.unbindAll() } catch (_: Exception) { }
        val oldAnalyzer = analyzer
        analyzer = null
        if (::cameraExecutor.isInitialized) {
            if (oldAnalyzer != null && !cameraExecutor.isShutdown) {
                cameraExecutor.execute { oldAnalyzer.close() }
            }
            cameraExecutor.shutdown()
        } else {
            oldAnalyzer?.close()
        }
        super.onDestroy()
    }
}
