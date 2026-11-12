package com.gestra.camera

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity

class AppPickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = GestraUi.BG
        window.navigationBarColor = GestraUi.BG

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(GestraUi.dp(this@AppPickerActivity, 18f), GestraUi.dp(this@AppPickerActivity, 24f),
                GestraUi.dp(this@AppPickerActivity, 18f), GestraUi.dp(this@AppPickerActivity, 18f))
            setBackgroundColor(GestraUi.BG)
        }
        val header = GestraUi.horizontal(this)
        header.addView(GestraUi.button(this, "← BACK").apply { setOnClickListener { finish() } })
        header.addView(GestraUi.text(this, "SELECT AN APP", 12f, GestraUi.TEXT, true).apply {
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(header)
        root.addView(GestraUi.text(this,
            "The selected app opens when the stable thumbs-up gesture is detected.", 12f, GestraUi.MUTED),
            LinearLayout.LayoutParams(-1, -2).apply { topMargin = GestraUi.dp(this, 14f) })

        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, GestraUi.dp(this@AppPickerActivity, 18f), 0, 0)
        }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = packageManager.queryIntentActivities(query, 0)
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase() }

        if (apps.isEmpty()) {
            list.addView(GestraUi.text(this, "No launchable apps were found.", 13f, GestraUi.MUTED))
            return
        }
        apps.forEach { info ->
            val label = info.loadLabel(packageManager).toString()
            val pkg = info.activityInfo.packageName
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(GestraUi.dp(this@AppPickerActivity, 14f), GestraUi.dp(this@AppPickerActivity, 13f),
                    GestraUi.dp(this@AppPickerActivity, 14f), GestraUi.dp(this@AppPickerActivity, 13f))
                background = GestraUi.card(this@AppPickerActivity)
                isClickable = true
                setOnClickListener {
                    GestraPrefs.putString(this@AppPickerActivity, GestraPrefs.TARGET_PACKAGE, pkg)
                    GestraPrefs.putString(this@AppPickerActivity, GestraPrefs.TARGET_LABEL, label)
                    finish()
                }
            }
            row.addView(GestraUi.text(this, label, 13f, GestraUi.TEXT, true))
            row.addView(GestraUi.text(this, pkg, 9f, GestraUi.MUTED), LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = GestraUi.dp(this@AppPickerActivity, 5f)
            })
            list.addView(row, LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = GestraUi.dp(this@AppPickerActivity, 7f)
            })
        }
    }
}
