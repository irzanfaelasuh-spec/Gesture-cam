package com.gestra.camera

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.activity.ComponentActivity

data class FeatureIdea(val name: String, val detail: String, val active: Boolean = false)

class RoadmapActivity : ComponentActivity() {
    private val ideas = listOf(
        FeatureIdea("Four live gesture actions", "Peace, open palm, thumbs up and shaka.", true),
        FeatureIdea("Local hand-landmark detection", "MediaPipe runs on the phone without a cloud key.", true),
        FeatureIdea("Real camera preview", "CameraX feeds the live view.", true),
        FeatureIdea("Peace preview blur", "Android RenderEffect blurs the camera preview.", true),
        FeatureIdea("Open-palm photo capture", "A stable palm pose captures a real still image.", true),
        FeatureIdea("Gallery save", "Captured JPEG files are published through MediaStore.", true),
        FeatureIdea("Thumbs-up app launcher", "Choose an installed app in Settings.", true),
        FeatureIdea("Shaka flash control", "Uses CameraX torch control when supported.", true),
        FeatureIdea("Four-second animated intro", "Canvas-based motion with no large video asset.", true),
        FeatureIdea("Startup sound cue", "Short synthesized system tone.", true),
        FeatureIdea("Permission checklist", "Required camera permission is separated from optional items.", true),
        FeatureIdea("Three-two-one entry sequence", "A short transition before opening the camera.", true),
        FeatureIdea("Vector-only launcher icon", "The icon is drawn from XML paths, not a PNG.", true),
        FeatureIdea("Demo-testing label", "The current build is clearly identified as experimental.", true),
        FeatureIdea("Gesture enable switches", "Each gesture action can be turned off independently.", true),
        FeatureIdea("Recognition sensitivity", "Adjust the hand detector confidence threshold.", true),
        FeatureIdea("Stable-frame filter", "Require repeated frames to reduce accidental triggers.", true),
        FeatureIdea("Adjustable blur amount", "Change blur radius from the settings screen.", true),
        FeatureIdea("Composition grid", "Optional thirds grid on the camera preview.", true),
        FeatureIdea("Shutter delay", "Off, three, five or ten second delay.", true),
        FeatureIdea("Action sound toggle", "Disable startup and feedback tones.", true),
        FeatureIdea("Haptic feedback toggle", "Optional touch and action feedback.", true),
        FeatureIdea("Live state diagnostics", "Show the latest accepted gesture state.", true),
        FeatureIdea("Front and rear lens switch", "Swap camera lenses from the preview.", true),
        FeatureIdea("HD-first camera stream", "Requests an HD 16:9 stream and uses device-supported fallback.", true),
        FeatureIdea("Quality-priority still capture", "CameraX uses maximum-quality capture mode.", true),
        FeatureIdea("One-shot gesture trigger", "Actions fire once per stable pose, not every frame.", true),
        FeatureIdea("Torch manual override", "A manual flash control remains available on the camera screen.", true),
        FeatureIdea("No broad storage permission", "The app uses MediaStore instead of legacy storage access.", true),
        FeatureIdea("No Shizuku dependency", "Core actions use supported Android APIs.", true),
        FeatureIdea("Dual-hand detection", "Track two hands and assign different controls."),
        FeatureIdea("Custom gesture mapping", "Map any supported pose to a chosen action."),
        FeatureIdea("Gesture profile slots", "Save separate gesture layouts for different use cases."),
        FeatureIdea("User-trained gesture", "Create a custom pose from guided samples."),
        FeatureIdea("Gesture confidence meter", "Show the classifier confidence live."),
        FeatureIdea("Landmark skeleton overlay", "Draw the 21 hand points on the preview."),
        FeatureIdea("Gesture history", "Keep a private local list of recent actions."),
        FeatureIdea("Per-gesture cooldown", "Set separate repeat delays for each action."),
        FeatureIdea("Hold-to-confirm mode", "Require a pose to remain visible for a chosen duration."),
        FeatureIdea("Quick-release mode", "Trigger as soon as a stable pose is confirmed."),
        FeatureIdea("Burst photo mode", "Capture a short burst while an assigned pose is held."),
        FeatureIdea("Timed photo presets", "Create custom shutter countdown presets."),
        FeatureIdea("Volume-key shutter", "Use hardware volume keys as a secondary shutter."),
        FeatureIdea("Capture review panel", "Preview the photo before opening the gallery."),
        FeatureIdea("In-app photo strip", "Show recent captures without leaving the camera."),
        FeatureIdea("Custom gallery folder", "Choose a destination album name."),
        FeatureIdea("Filename templates", "Customize date, time and sequence naming."),
        FeatureIdea("HEIF capture option", "Use HEIF when the camera device supports it."),
        FeatureIdea("RAW capture mode", "Expose DNG capture on supported camera devices."),
        FeatureIdea("Manual exposure controls", "Adjust exposure compensation from the preview."),
        FeatureIdea("Manual focus control", "Tap-to-focus and optional focus distance control."),
        FeatureIdea("White-balance presets", "Offer daylight, cloudy, tungsten and auto presets."),
        FeatureIdea("Zoom slider", "Control optical or digital zoom when available."),
        FeatureIdea("Low-light guidance", "Show a hint when the scene is too dark for reliable tracking."),
        FeatureIdea("Hand distance guide", "Indicate when the hand is too close or too far."),
        FeatureIdea("Orientation-independent poses", "Improve classification when the hand is rotated."),
        FeatureIdea("Left-hand calibration", "Tune tracking for left-handed gestures."),
        FeatureIdea("Right-hand calibration", "Tune tracking for right-handed gestures."),
        FeatureIdea("Background session mode", "Explore safe foreground-service operation with visible controls."),
        FeatureIdea("Optional Shizuku module", "Add only if a future feature genuinely needs elevated access."),
        FeatureIdea("Accessibility-safe controls", "Keep touch controls available when gesture tracking is paused."),
        FeatureIdea("Battery-aware inference", "Reduce analysis frequency on low battery."),
        FeatureIdea("Thermal-aware inference", "Adapt inference rate when the device gets hot."),
        FeatureIdea("Export settings profile", "Share a small text profile without camera data."),
        FeatureIdea("Import settings profile", "Restore a previously exported configuration."),
        FeatureIdea("On-device diagnostics report", "Export technical logs with user approval and no images."),
        FeatureIdea("Multilingual interface", "Add Indonesian and English language selection.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = GestraUi.BG
        window.navigationBarColor = GestraUi.BG
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(GestraUi.dp(this@RoadmapActivity, 18f), GestraUi.dp(this@RoadmapActivity, 24f),
                GestraUi.dp(this@RoadmapActivity, 18f), 0)
            setBackgroundColor(GestraUi.BG)
        }
        val header = GestraUi.horizontal(this)
        header.addView(GestraUi.button(this, "← BACK").apply { setOnClickListener { finish() } })
        header.addView(GestraUi.text(this, "FEATURE CATALOG", 11f, GestraUi.TEXT, true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.12f
        }, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(GestraUi.text(this, "${ideas.size}", 11f, GestraUi.ACCENT, true))
        root.addView(header)
        root.addView(GestraUi.text(this,
            "Active items are included in this demo. Planned items are ideas for later versions and are not presented as working features.",
            11f, GestraUi.MUTED), LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = GestraUi.dp(this@RoadmapActivity, 14f)
        })
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, GestraUi.dp(this@RoadmapActivity, 16f), 0, GestraUi.dp(this@RoadmapActivity, 28f))
        }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        ideas.forEachIndexed { index, idea ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(GestraUi.dp(this@RoadmapActivity, 14f), GestraUi.dp(this@RoadmapActivity, 12f),
                    GestraUi.dp(this@RoadmapActivity, 14f), GestraUi.dp(this@RoadmapActivity, 12f))
                background = GestraUi.card(this@RoadmapActivity)
            }
            val top = GestraUi.horizontal(this)
            top.addView(GestraUi.text(this, "%02d".format(index + 1), 10f, GestraUi.MUTED, true))
            top.addView(GestraUi.text(this, idea.name, 12f, GestraUi.TEXT, true).apply {
                setPadding(GestraUi.dp(this@RoadmapActivity, 10f), 0, 0, 0)
            }, LinearLayout.LayoutParams(0, -2, 1f))
            top.addView(GestraUi.text(this, if (idea.active) "ACTIVE" else "PLANNED", 8f,
                if (idea.active) GestraUi.ACCENT else GestraUi.WARNING, true))
            row.addView(top)
            row.addView(GestraUi.text(this, idea.detail, 10f, GestraUi.MUTED), LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = GestraUi.dp(this@RoadmapActivity, 7f)
            })
            list.addView(row, LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = GestraUi.dp(this@RoadmapActivity, 6f)
            })
        }
    }
}
