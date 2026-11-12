package com.gestra.camera

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView

internal object GestraUi {
    const val BG = 0xFF080B10.toInt()
    const val PANEL = 0xFF111820.toInt()
    const val PANEL_ALT = 0xFF18222C.toInt()
    const val ACCENT = 0xFFB7F36B.toInt()
    const val TEXT = 0xFFEEF3EB.toInt()
    const val MUTED = 0xFF89959F.toInt()
    const val WARNING = 0xFFF0BD58.toInt()
    const val STROKE = 0xFF293541.toInt()

    fun dp(context: Context, value: Float): Int =
        (value * context.resources.displayMetrics.density + 0.5f).toInt()

    fun card(context: Context, color: Int = PANEL, radius: Float = 18f): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(context, radius).toFloat()
            setStroke(dp(context, 1f), STROKE)
        }

    fun text(
        context: Context,
        value: String,
        size: Float = 14f,
        color: Int = TEXT,
        bold: Boolean = false
    ): TextView = TextView(context).apply {
        text = value
        textSize = size
        setTextColor(color)
        typeface = if (bold) Typeface.create("sans-serif", Typeface.BOLD)
        else Typeface.create("sans-serif", Typeface.NORMAL)
        includeFontPadding = false
    }

    fun label(context: Context, value: String): TextView = text(context, value.uppercase(), 10f, MUTED, true).apply {
        letterSpacing = 0.12f
    }

    fun button(context: Context, value: String, primary: Boolean = false): TextView = text(
        context, value, 12f, if (primary) BG else TEXT, true
    ).apply {
        gravity = Gravity.CENTER
        letterSpacing = 0.08f
        setPadding(dp(context, 18f), dp(context, 15f), dp(context, 18f), dp(context, 15f))
        background = card(context, if (primary) ACCENT else PANEL_ALT, 14f)
        isClickable = true
        isFocusable = true
        setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> view.alpha = 0.72f
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> view.alpha = 1f
            }
            false
        }
    }

    fun vertical(context: Context, spacingDp: Int = 0) = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        if (spacingDp > 0) showDividers(LinearLayout.SHOW_DIVIDER_NONE)
    }

    fun horizontal(context: Context) = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    fun addGap(context: Context, parent: LinearLayout, heightDp: Int) {
        parent.addView(View(context), LinearLayout.LayoutParams(1, dp(context, heightDp.toFloat())))
    }
}

internal class BrandMarkView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var angle = 0f
    private var pulse = 0f
    private val animator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 6000L
        repeatCount = ValueAnimator.INFINITE
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            angle = it.animatedValue as Float
            pulse = (0.5f + 0.5f * kotlin.math.sin(Math.toRadians(angle.toDouble()))).toFloat()
            invalidate()
        }
    }

    fun start() = animator.start()
    fun stop() = animator.cancel()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val outer = minOf(width, height) * 0.39f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        paint.color = GestraUi.STROKE
        canvas.drawCircle(cx, cy, outer, paint)
        paint.color = GestraUi.ACCENT
        paint.strokeWidth = 4f
        canvas.save()
        canvas.rotate(angle, cx, cy)
        val rect = RectF(cx - outer, cy - outer, cx + outer, cy + outer)
        canvas.drawArc(rect, -28f, 82f, false, paint)
        canvas.drawArc(rect, 152f, 34f, false, paint)
        canvas.restore()
        paint.style = Paint.Style.FILL
        paint.color = GestraUi.ACCENT
        canvas.drawCircle(cx, cy, outer * (0.34f + pulse * 0.025f), paint)
        paint.color = GestraUi.BG
        canvas.drawCircle(cx, cy, outer * 0.18f, paint)
        paint.color = GestraUi.ACCENT
        canvas.drawCircle(cx, cy - outer - 7f, 3f + pulse * 1.5f, paint)
    }
}

internal class GridOverlay(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x55FFFFFF
        strokeWidth = 1f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in 1..2) {
            val x = width * i / 3f
            val y = height * i / 3f
            canvas.drawLine(x, 0f, x, height.toFloat(), paint)
            canvas.drawLine(0f, y, width.toFloat(), y, paint)
        }
    }
}
