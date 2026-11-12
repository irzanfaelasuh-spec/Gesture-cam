package com.gestra.camera

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.activity.ComponentActivity

class SettingsActivity : ComponentActivity() {
    private lateinit var body: LinearLayout
    private var appTargetDescription: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = GestraUi.BG
        window.navigationBarColor = GestraUi.BG
        buildUi()
    }

    override fun onResume() {
        super.onResume()
        appTargetDescription?.text = GestraPrefs.string(this, GestraPrefs.TARGET_LABEL, "No app selected")
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(GestraUi.BG)
            setPadding(GestraUi.dp(this@SettingsActivity, 18f), GestraUi.dp(this@SettingsActivity, 24f),
                GestraUi.dp(this@SettingsActivity, 18f), 0)
        }
        val header = GestraUi.horizontal(this)
        header.addView(GestraUi.button(this, "← BACK").apply { setOnClickListener { finish() } })
        header.addView(GestraUi.text(this, "SETTINGS", 12f, GestraUi.TEXT, true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.18f
        }, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(GestraUi.text(this, "DEMO", 9f, GestraUi.WARNING, true).apply { gravity = Gravity.CENTER })
        root.addView(header)

        val scroll = ScrollView(this).apply { clipToPadding = false }
        body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, GestraUi.dp(this@SettingsActivity, 20f), 0, GestraUi.dp(this@SettingsActivity, 30f))
        }
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        val demo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(GestraUi.dp(this@SettingsActivity, 15f), GestraUi.dp(this@SettingsActivity, 14f),
                GestraUi.dp(this@SettingsActivity, 15f), GestraUi.dp(this@SettingsActivity, 14f))
            background = GestraUi.card(this@SettingsActivity, 0xFF211B10.toInt(), 14f)
        }
        demo.addView(GestraUi.text(this, "DEMO TESTING  /  0.1.0", 11f, GestraUi.WARNING, true))
        demo.addView(GestraUi.text(this,
            "This build is an early test. Gesture accuracy, camera behaviour and device compatibility still need real-device testing.",
            12f, 0xFFD0C5AC.toInt()), LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = GestraUi.dp(this@SettingsActivity, 8f)
        })
        body.addView(demo)

        section("GESTURE ACTIONS", "Switch each action on or off. Each gesture triggers once per stable pose.")
        addSwitch("Peace sign (V)", "Blur the camera preview while the pose is held.", GestraPrefs.PEACE_BLUR, true)
        addSwitch("Open palm / hand", "Capture a full-resolution photo and save it to Gallery.", GestraPrefs.PALM_CAPTURE, true)
        addSwitch("Thumbs up", "Launch the application selected below.", GestraPrefs.THUMB_LAUNCH, true)
        addSwitch("Shaka", "Turn on the phone LED flash.", GestraPrefs.SHAKA_FLASH, true)

        section("GESTURE ENGINE", "Tune recognition stability and response speed.")
        addSeek("Recognition sensitivity", "Lower is more responsive; higher is more selective.",
            GestraPrefs.SENSITIVITY, 25, 85, 55, "%")
        addSeek("Stable frame count", "Frames required before a pose is accepted.",
            GestraPrefs.STABLE_FRAMES, 2, 7, 3, " frames")
        addSeek("Preview blur strength", "Controls the real-time Android preview blur.",
            GestraPrefs.BLUR_RADIUS, 6, 32, 18, " px")

        section("CAMERA & CAPTURE", "Camera output uses the highest supported 16:9 HD stream for preview and quality capture mode for photos.")
        addSwitch("Composition grid", "Show a simple thirds grid over the preview.", GestraPrefs.GRID, false)
        addSwitch("Sound cues", "Play short system tones for startup and actions.", GestraPrefs.SOUND, true)
        addSwitch("Haptic feedback", "Use a light vibration when an action is accepted.", GestraPrefs.HAPTICS, true)
        addSwitch("Status diagnostics", "Show live gesture state and inference status.", GestraPrefs.DEBUG_STATUS, true)
        addDelaySelector()
        addActionRow("App for thumbs up", "${GestraPrefs.string(this, GestraPrefs.TARGET_LABEL, "No app selected")}", "CHOOSE") {
            startActivity(Intent(this, AppPickerActivity::class.java))
        }

        section("SYSTEM", "The first build intentionally uses standard Android permissions only.")
        addInfoRow("Shizuku bridge", "Optional — not integrated or required in this build.")
        addInfoRow("Photo storage", "Uses MediaStore. No broad storage permission is requested.")
        addInfoRow("Hand model", "MediaPipe model runs locally; no cloud AI key or backend.")
        addActionRow("Permission checklist", "Review camera access", "OPEN") {
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }

        section("LAB / FEATURE CATALOG", "67 feature ideas. Items marked PLANNED are concepts, not active switches.")
        val roadmap = GestraUi.button(this, "OPEN FEATURE CATALOG  /  67 IDEAS  →", true).apply {
            setOnClickListener { startActivity(Intent(this@SettingsActivity, RoadmapActivity::class.java)) }
        }
        body.addView(roadmap, LinearLayout.LayoutParams(-1, -2).apply { topMargin = GestraUi.dp(this@SettingsActivity, 8f) })

        body.addView(GestraUi.text(this, "GESTRA  •  GESTURE CAMERA SYSTEM  •  DEMO TESTING", 9f, GestraUi.MUTED, true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.1f
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = GestraUi.dp(this@SettingsActivity, 28f) })
    }

    private fun section(title: String, description: String) {
        body.addView(GestraUi.label(this, title), LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = GestraUi.dp(this@SettingsActivity, 24f)
        })
        body.addView(GestraUi.text(this, description, 11f, GestraUi.MUTED), LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = GestraUi.dp(this@SettingsActivity, 6f)
            bottomMargin = GestraUi.dp(this@SettingsActivity, 10f)
        })
    }

    private fun addSwitch(title: String, description: String, key: String, default: Boolean) {
        val row = GestraUi.horizontal(this).apply {
            setPadding(GestraUi.dp(this@SettingsActivity, 14f), GestraUi.dp(this@SettingsActivity, 13f),
                GestraUi.dp(this@SettingsActivity, 12f), GestraUi.dp(this@SettingsActivity, 13f))
            background = GestraUi.card(this@SettingsActivity)
        }
        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        texts.addView(GestraUi.text(this, title, 13f, GestraUi.TEXT, true))
        texts.addView(GestraUi.text(this, description, 10f, GestraUi.MUTED), LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = GestraUi.dp(this@SettingsActivity, 5f)
        })
        row.addView(texts, LinearLayout.LayoutParams(0, -2, 1f))
        val toggle = Switch(this).apply {
            isChecked = GestraPrefs.bool(this@SettingsActivity, key, default)
            setOnCheckedChangeListener { _, checked -> GestraPrefs.putBool(this@SettingsActivity, key, checked) }
        }
        row.addView(toggle)
        body.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = GestraUi.dp(this@SettingsActivity, 7f) })
    }

    private fun addSeek(title: String, description: String, key: String, minValue: Int, maxValue: Int,
                        default: Int, suffix: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(GestraUi.dp(this@SettingsActivity, 14f), GestraUi.dp(this@SettingsActivity, 13f),
                GestraUi.dp(this@SettingsActivity, 14f), GestraUi.dp(this@SettingsActivity, 10f))
            background = GestraUi.card(this@SettingsActivity)
        }
        val head = GestraUi.horizontal(this)
        head.addView(GestraUi.text(this, title, 13f, GestraUi.TEXT, true), LinearLayout.LayoutParams(0, -2, 1f))
        val value = GestraUi.text(this, "", 11f, GestraUi.ACCENT, true)
        head.addView(value)
        box.addView(head)
        box.addView(GestraUi.text(this, description, 10f, GestraUi.MUTED), LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = GestraUi.dp(this@SettingsActivity, 5f)
        })
        val seek = SeekBar(this).apply {
            this.max = maxValue - minValue
            progress = (GestraPrefs.int(this@SettingsActivity, key, default) - minValue).coerceIn(0, this.max)
            progressTintList = android.content.res.ColorStateList.valueOf(GestraUi.ACCENT)
            thumbTintList = android.content.res.ColorStateList.valueOf(GestraUi.ACCENT)
        }
        fun update(progress: Int, save: Boolean) {
            val amount = progress + minValue
            value.text = "$amount$suffix"
            if (save) GestraPrefs.putInt(this, key, amount)
        }
        update(seek.progress, false)
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = update(progress, fromUser)
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        box.addView(seek)
        body.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = GestraUi.dp(this@SettingsActivity, 7f) })
    }

    private fun addDelaySelector() {
        val delays = intArrayOf(0, 3, 5, 10)
        val labels = arrayOf("Off", "3 seconds", "5 seconds", "10 seconds")
        val row = GestraUi.horizontal(this).apply {
            setPadding(GestraUi.dp(this@SettingsActivity, 14f), GestraUi.dp(this@SettingsActivity, 12f),
                GestraUi.dp(this@SettingsActivity, 14f), GestraUi.dp(this@SettingsActivity, 12f))
            background = GestraUi.card(this@SettingsActivity)
        }
        row.addView(GestraUi.text(this, "Gesture photo timer", 12f, GestraUi.TEXT, true), LinearLayout.LayoutParams(0, -2, 1f))
        val selected = GestraPrefs.int(this, GestraPrefs.SHUTTER_DELAY, 0).coerceIn(0, 10)
        val current = labels[delays.indexOf(selected).takeIf { it >= 0 } ?: 0]
        val control = GestraUi.text(this, "$current  ▾", 11f, GestraUi.ACCENT, true).apply {
            setPadding(GestraUi.dp(this@SettingsActivity, 10f), GestraUi.dp(this@SettingsActivity, 8f),
                GestraUi.dp(this@SettingsActivity, 10f), GestraUi.dp(this@SettingsActivity, 8f))
            setOnClickListener {
                val currentIndex = delays.indexOf(GestraPrefs.int(this@SettingsActivity, GestraPrefs.SHUTTER_DELAY, 0)).let { if (it < 0) 0 else it }
                val nextIndex = (currentIndex + 1) % delays.size
                GestraPrefs.putInt(this@SettingsActivity, GestraPrefs.SHUTTER_DELAY, delays[nextIndex])
                text = "${labels[nextIndex]}  ▾"
            }
        }
        row.addView(control)
        body.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = GestraUi.dp(this@SettingsActivity, 7f) })
    }

    private fun addInfoRow(title: String, description: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(GestraUi.dp(this@SettingsActivity, 14f), GestraUi.dp(this@SettingsActivity, 12f),
                GestraUi.dp(this@SettingsActivity, 14f), GestraUi.dp(this@SettingsActivity, 12f))
            background = GestraUi.card(this@SettingsActivity)
        }
        box.addView(GestraUi.text(this, title, 12f, GestraUi.TEXT, true))
        box.addView(GestraUi.text(this, description, 10f, GestraUi.MUTED), LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = GestraUi.dp(this@SettingsActivity, 5f)
        })
        body.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = GestraUi.dp(this@SettingsActivity, 7f) })
    }

    private fun addActionRow(title: String, description: String, action: String, onClick: () -> Unit) {
        val row = GestraUi.horizontal(this).apply {
            setPadding(GestraUi.dp(this@SettingsActivity, 14f), GestraUi.dp(this@SettingsActivity, 13f),
                GestraUi.dp(this@SettingsActivity, 14f), GestraUi.dp(this@SettingsActivity, 13f))
            background = GestraUi.card(this@SettingsActivity)
            isClickable = true
            setOnClickListener { onClick() }
        }
        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        texts.addView(GestraUi.text(this, title, 12f, GestraUi.TEXT, true))
        val descriptionView = GestraUi.text(this, description, 10f, GestraUi.MUTED)
        if (title == "App for thumbs up") appTargetDescription = descriptionView
        texts.addView(descriptionView, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = GestraUi.dp(this@SettingsActivity, 5f)
        })
        row.addView(texts, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(GestraUi.text(this, action, 9f, GestraUi.ACCENT, true))
        body.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = GestraUi.dp(this@SettingsActivity, 7f) })
    }
}
