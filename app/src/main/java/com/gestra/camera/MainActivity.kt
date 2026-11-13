package com.gestra.camera

import android.view.animation.DecelerateInterpolator
import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val handler = Handler(Looper.getMainLooper())

    private var brandMark: BrandMarkView? = null
    private var tone: ToneGenerator? = null

    private lateinit var root: FrameLayout

    private var cameraPermissionRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = GestraUi.BG
        window.navigationBarColor = GestraUi.BG
        window.decorView.systemUiVisibility = 0

        showSplash()
    }

    private fun showSplash() {
        root = FrameLayout(this).apply {
            setBackgroundColor(GestraUi.BG)
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER

            setPadding(
                GestraUi.dp(this@MainActivity, 28f),
                0,
                GestraUi.dp(this@MainActivity, 28f),
                0
            )
        }

        brandMark = BrandMarkView(this).also { mark ->
            content.addView(
                mark,
                LinearLayout.LayoutParams(
                    GestraUi.dp(this, 184f),
                    GestraUi.dp(this, 184f)
                )
            )

            mark.start()
        }

        content.addView(
            GestraUi.text(this, "GESTRA", 31f, GestraUi.TEXT, true).apply {
                gravity = Gravity.CENTER
                letterSpacing = 0.24f
            },
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = GestraUi.dp(this@MainActivity, 24f)
            }
        )

        content.addView(
            GestraUi.text(
                this,
                "GESTURE CAMERA SYSTEM",
                10f,
                GestraUi.MUTED,
                true
            ).apply {
                gravity = Gravity.CENTER
                letterSpacing = 0.22f
            },
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = GestraUi.dp(this@MainActivity, 9f)
            }
        )

        val line = View(this).apply {
            setBackgroundColor(GestraUi.ACCENT)
        }

        content.addView(
            line,
            LinearLayout.LayoutParams(
                GestraUi.dp(this, 42f),
                GestraUi.dp(this, 2f)
            ).apply {
                topMargin = GestraUi.dp(this@MainActivity, 26f)
            }
        )

        content.addView(
            GestraUi.text(
                this,
                "INITIALIZING CAMERA INTERFACE",
                9f,
                GestraUi.MUTED
            ).apply {
                gravity = Gravity.CENTER
                letterSpacing = 0.13f
            },
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = GestraUi.dp(this@MainActivity, 18f)
            }
        )

        root.addView(content, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)

        val fade = ValueAnimator.ofFloat(0.35f, 1f).apply {
            duration = 950L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = DecelerateInterpolator()

            addUpdateListener {
                content.alpha = it.animatedValue as Float
            }

            start()
        }

        handler.postDelayed({
            fade.cancel()
            brandMark?.stop()

            playCue(ToneGenerator.TONE_PROP_ACK)

            if (cameraGranted()) {
                showCountdown()
            } else {
                showPermissionGate()
            }
        }, 4_000L)
    }

    private fun showPermissionGate() {
        root = FrameLayout(this).apply {
            setBackgroundColor(GestraUi.BG)
        }

        val scroll = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                GestraUi.dp(this@MainActivity, 22f),
                GestraUi.dp(this@MainActivity, 34f),
                GestraUi.dp(this@MainActivity, 22f),
                GestraUi.dp(this@MainActivity, 26f)
            )
        }

        content.addView(
            GestraUi.label(this, "FIRST-RUN CONFIGURATION")
        )

        content.addView(
            GestraUi.text(
                this,
                "Permission\nchecklist",
                32f,
                GestraUi.TEXT,
                true
            ).apply {
                setLineSpacing(
                    GestraUi.dp(this@MainActivity, 1f).toFloat(),
                    1f
                )
            },
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = GestraUi.dp(this@MainActivity, 12f)
            }
        )

        content.addView(
            GestraUi.text(
                this,
                "Camera access is required for live gesture recognition. Optional items never block the next step.",
                14f,
                GestraUi.MUTED
            ),
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = GestraUi.dp(this@MainActivity, 12f)
            }
        )

        val cameraCard = permissionCard(
            "01 / CAMERA",
            "Required for preview, gesture detection and photo capture.",
            "ALLOW CAMERA",
            false
        ) {
            requestCameraPermission()
        }

        content.addView(cameraCard, marginParams(top = 28))

        content.addView(
            permissionCard(
                "02 / SHIZUKU BRIDGE",
                "Optional. This build uses standard Android APIs, so Shizuku is not required.",
                "OPTIONAL — SKIP",
                true
            ),
            marginParams(top = 12)
        )

        content.addView(
            permissionCard(
                "03 / PHOTO STORAGE",
                "No storage permission is needed. Photos are saved through Android MediaStore.",
                "HANDLED BY ANDROID",
                true
            ),
            marginParams(top = 12)
        )

        content.addView(
            permissionCard(
                "04 / NOTIFICATIONS",
                "Optional. No background camera service or notification permission is used in this demo.",
                "OPTIONAL — SKIP",
                true
            ),
            marginParams(top = 12)
        )

        val info = GestraUi.text(
            this,
            "PRIVACY NOTE  /  Hand landmarks are processed on this device. No camera frames are uploaded to a cloud AI service.",
            11f,
            GestraUi.MUTED
        ).apply {
            setPadding(
                GestraUi.dp(this@MainActivity, 14f),
                GestraUi.dp(this@MainActivity, 14f),
                GestraUi.dp(this@MainActivity, 14f),
                GestraUi.dp(this@MainActivity, 14f)
            )

            background = GestraUi.card(
                this@MainActivity,
                GestraUi.PANEL,
                14f
            )
        }

        content.addView(info, marginParams(top = 18))

        val next = GestraUi.button(this, "NEXT  →", true).apply {
            alpha = if (cameraGranted()) 1f else 0.38f
            isEnabled = cameraGranted()

            setOnClickListener {
                if (cameraGranted()) {
                    GestraPrefs.putBool(
                        this@MainActivity,
                        GestraPrefs.FIRST_RUN_DONE,
                        true
                    )

                    showCountdown()
                } else {
                    requestCameraPermission()
                }
            }
        }

        content.addView(
            next,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = GestraUi.dp(this@MainActivity, 22f)
            }
        )

        scroll.addView(content)

        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))

        setContentView(root)
    }

    private fun permissionCard(
        title: String,
        description: String,
        action: String,
        optional: Boolean,
        onClick: (() -> Unit)? = null
    ): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                GestraUi.dp(this@MainActivity, 16f),
                GestraUi.dp(this@MainActivity, 16f),
                GestraUi.dp(this@MainActivity, 16f),
                GestraUi.dp(this@MainActivity, 16f)
            )

            background = GestraUi.card(this@MainActivity)
        }

        val row = GestraUi.horizontal(this)

        row.addView(
            GestraUi.text(this, title, 11f, GestraUi.TEXT, true),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        val status = TextView(this).apply {
            text = if (optional) {
                "READY"
            } else if (cameraGranted()) {
                "GRANTED"
            } else {
                "NEEDED"
            }

            textSize = 9f
            letterSpacing = 0.08f
            typeface = Typeface.DEFAULT_BOLD

            setTextColor(
                if (optional || cameraGranted()) {
                    GestraUi.ACCENT
                } else {
                    GestraUi.WARNING
                }
            )

            gravity = Gravity.CENTER

            setPadding(
                GestraUi.dp(this@MainActivity, 9f),
                GestraUi.dp(this@MainActivity, 7f),
                GestraUi.dp(this@MainActivity, 9f),
                GestraUi.dp(this@MainActivity, 7f)
            )

            background = GestraUi.card(
                this@MainActivity,
                GestraUi.PANEL_ALT,
                8f
            )
        }

        row.addView(status)
        card.addView(row)

        card.addView(
            GestraUi.text(
                this,
                description,
                12f,
                GestraUi.MUTED
            ),
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = GestraUi.dp(this@MainActivity, 10f)
            }
        )

        val actionView = GestraUi.text(
            this,
            action,
            9f,
            if (optional || cameraGranted()) {
                GestraUi.ACCENT
            } else {
                GestraUi.WARNING
            },
            true
        ).apply {
            letterSpacing = 0.1f
        }

        card.addView(
            actionView,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = GestraUi.dp(this@MainActivity, 13f)
            }
        )

        if (onClick != null) {
            card.isClickable = true
            card.setOnClickListener { onClick() }
        }

        return card
    }

    private fun marginParams(
        top: Int = 0
    ): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = GestraUi.dp(
                this@MainActivity,
                top.toFloat()
            )
        }

    private fun requestCameraPermission() {
        if (cameraGranted()) {
            showPermissionGate()
            return
        }

        if (
            Build.VERSION.SDK_INT >= 23 &&
            shouldShowRequestPermissionRationale(
                Manifest.permission.CAMERA
            ).not() &&
            cameraPermissionRequested
        ) {
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
            )
            return
        }

        cameraPermissionRequested = true

        requestPermissions(
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA
        )
    }

    @Deprecated("Use the Activity Result API in a future iteration")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == REQUEST_CAMERA) {
            if (cameraGranted()) {
                playCue(ToneGenerator.TONE_PROP_ACK)
            }

            showPermissionGate()
        }
    }

    private fun cameraGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun showCountdown() {
        root = FrameLayout(this).apply {
            setBackgroundColor(GestraUi.BG)
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER

            setPadding(
                GestraUi.dp(this@MainActivity, 24f),
                0,
                GestraUi.dp(this@MainActivity, 24f),
                0
            )
        }

        content.addView(
            GestraUi.label(
                this,
                "SESSION INITIALIZATION"
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        val title = GestraUi.text(
            this,
            "ENTER THE\nAPPLICATION",
            24f,
            GestraUi.TEXT,
            true
        ).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.08f

            setLineSpacing(
                GestraUi.dp(this@MainActivity, 6f).toFloat(),
                1f
            )
        }

        content.addView(
            title,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = GestraUi.dp(this@MainActivity, 22f)
            }
        )

        val number = GestraUi.text(
            this,
            "3",
            84f,
            GestraUi.ACCENT,
            true
        ).apply {
            gravity = Gravity.CENTER
        }

        content.addView(
            number,
            LinearLayout.LayoutParams(
                -1,
                GestraUi.dp(this, 136f)
            ).apply {
                topMargin = GestraUi.dp(this@MainActivity, 26f)
            }
        )

        content.addView(
            GestraUi.text(
                this,
                "OPEN GESTRA",
                11f,
                GestraUi.MUTED,
                true
            ).apply {
                gravity = Gravity.CENTER
                letterSpacing = 0.18f
            }
        )

        root.addView(content, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)

        val sequence = listOf(
            "3",
            "2",
            "1",
            "OPEN GESTRA"
        )

        var index = 0

        val tick = object : Runnable {
            override fun run() {
                if (index >= sequence.size) {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            CameraActivity::class.java
                        )
                    )

                    finish()
                    return
                }

                number.text = sequence[index]
                number.alpha = 0.2f

                number.animate()
                    .alpha(1f)
                    .setDuration(220L)
                    .start()

                playCue(
                    if (index == sequence.lastIndex) {
                        ToneGenerator.TONE_PROP_ACK
                    } else {
                        ToneGenerator.TONE_PROP_BEEP
                    }
                )

                index++

                handler.postDelayed(
                    this,
                    if (index == sequence.size) 520L else 720L
                )
            }
        }

        handler.post(tick)
    }

    private fun playCue(toneType: Int) {
        if (!GestraPrefs.bool(this, GestraPrefs.SOUND, true)) {
            return
        }

        try {
            if (tone == null) {
                tone = ToneGenerator(
                    AudioManager.STREAM_MUSIC,
                    32
                )
            }

            tone?.startTone(toneType, 115)
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)

        brandMark?.stop()

        tone?.release()
        tone = null

        super.onDestroy()
    }

    companion object {
        private const val REQUEST_CAMERA = 5101
    }
}
