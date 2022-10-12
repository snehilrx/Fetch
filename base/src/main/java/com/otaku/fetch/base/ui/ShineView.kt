package com.otaku.fetch.base.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.google.android.material.appbar.AppBarLayout
import com.otaku.fetch.base.R
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


class ShineView : View, AppBarLayout.OnOffsetChangedListener {

    var statusbarHeight: Float = 0f

    private var toolBarHeight: Float = 0f
    private var scrimHeight: Float = 0f
    private var oProgress: Float = -1f
    private var progress: Float = 0f

    @ColorInt
    private var shineColor: Int = Color.TRANSPARENT

    private var mPaint: Paint = Paint().apply {
        this.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
    }


    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true)
        shineColor = typedValue.data
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ShineView, defStyle, 0
        )

        toolBarHeight = a.getDimension(
            R.styleable.ShineView_toolbarHeight,
            scrimHeight
        )

        scrimHeight = a.getDimension(
            R.styleable.ShineView_scrimHeight,
            scrimHeight
        )
        a.recycle()
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        setShader(r)
        super.layout(l, t, r, b)
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        progress = (-verticalOffset / appBarLayout.totalScrollRange.toFloat())
        if (oProgress == progress) return
        if (progress.isFinite() && statusbarHeight > 0) {
            setShader()
            invalidate()
            if (progress >= 1.0f) {
                bringViewToFront()
            } else {
                sendViewToBack()
            }
        }
        oProgress = progress
    }

    private fun setShader(viewWidth: Int = width) {
        val toShow = (toolBarHeight * (1 - progress)).coerceAtLeast(statusbarHeight)
        val radius = viewWidth / 2 * (1 - progress + 0.2)
        val translateX = radius + toShow
        val x1 = radius * cos(toRadians(45 + 45.0 * progress)) + translateX
        val y1 = radius * sin(toRadians(45 + 45.0 * progress))
        val y0 = radius * sin(toRadians(220 + 50.0 * progress))
        val x0 = radius * cos(toRadians(220 + 50.0 * progress)) + translateX
        mPaint.shader = LinearGradient(
            x0.toFloat(),
            y0.toFloat(),
            x1.toFloat(),
            y1.toFloat(),
            getColor(),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
    }

    private fun getColor() = adjustAlpha(shineColor, (progress + 0.5f).coerceAtMost(1f))

    // to fix view flickering
    private var lock = true
    private fun sendViewToBack() {
        if(lock) return
        val parent = parent as ViewGroup
        parent.removeView(this)
        parent.addView(this, 0)
        lock = true
    }
    private fun bringViewToFront() {
        if(!lock) return
        parent.bringChildToFront(this)
        lock = false
    }

    @ColorInt
    fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val alpha = ((Color.alpha(color) * factor).roundToInt()+50).coerceAtMost(255)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat() - (scrimHeight * progress), mPaint)
    }

}