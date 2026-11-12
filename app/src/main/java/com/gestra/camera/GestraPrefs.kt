package com.gestra.camera

import android.content.Context

object GestraPrefs {
    private const val FILE = "gestra_settings"
    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    const val PEACE_BLUR = "peace_blur"
    const val PALM_CAPTURE = "palm_capture"
    const val THUMB_LAUNCH = "thumb_launch"
    const val SHAKA_FLASH = "shaka_flash"
    const val SOUND = "sound"
    const val HAPTICS = "haptics"
    const val GRID = "grid"
    const val DEBUG_STATUS = "debug_status"
    const val SENSITIVITY = "sensitivity"
    const val STABLE_FRAMES = "stable_frames"
    const val BLUR_RADIUS = "blur_radius"
    const val SHUTTER_DELAY = "shutter_delay"
    const val TARGET_PACKAGE = "target_package"
    const val TARGET_LABEL = "target_label"
    const val FIRST_RUN_DONE = "first_run_done"

    fun bool(context: Context, key: String, default: Boolean = true): Boolean =
        prefs(context).getBoolean(key, default)

    fun int(context: Context, key: String, default: Int): Int =
        prefs(context).getInt(key, default)

    fun string(context: Context, key: String, default: String = ""): String =
        prefs(context).getString(key, default) ?: default

    fun putBool(context: Context, key: String, value: Boolean) {
        prefs(context).edit().putBoolean(key, value).apply()
    }

    fun putInt(context: Context, key: String, value: Int) {
        prefs(context).edit().putInt(key, value).apply()
    }

    fun putString(context: Context, key: String, value: String) {
        prefs(context).edit().putString(key, value).apply()
    }
}
