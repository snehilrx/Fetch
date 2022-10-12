package com.otaku.fetch.base.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.AsyncTask
import android.util.AttributeSet
import android.util.TypedValue
import androidx.palette.graphics.Palette
import com.google.android.material.imageview.ShapeableImageView
import com.otaku.fetch.base.utils.UiUtils


class GradientFromImage : ShapeableImageView {

    @Suppress("deprecation")
    private var task: AsyncTask<Bitmap, Void, Palette>? = null
    private var mFadeSide: FadeSide = FadeSide.NONE

    enum class FadeSide {
        RIGHT_SIDE, LEFT_SIDE, NONE, BOTTOM_SIDE, TOP_SIDE, ALL
    }

    fun setFadeDirection(side: FadeSide) {
        mFadeSide = side
    }

    fun setEdgeLength(length: Int) {
        setFadingEdgeLength(getPixels(length))
    }

    override fun getLeftFadingEdgeStrength(): Float {
        return if (mFadeSide == FadeSide.LEFT_SIDE || mFadeSide == FadeSide.ALL) 1.0f else 0.0f
    }

    override fun getRightFadingEdgeStrength(): Float {
        return if (mFadeSide == FadeSide.RIGHT_SIDE || mFadeSide == FadeSide.ALL) 1.0f else 0.0f
    }

    override fun getBottomFadingEdgeStrength(): Float {
        return if (mFadeSide == FadeSide.BOTTOM_SIDE || mFadeSide == FadeSide.ALL) 1.0f else 0.0f
    }

    override fun getTopFadingEdgeStrength(): Float {
        return if (mFadeSide == FadeSide.TOP_SIDE || mFadeSide == FadeSide.ALL) 1.0f else 0.0f
    }

    override fun hasOverlappingRendering(): Boolean {
        return true
    }

    override fun onSetAlpha(alpha: Int): Boolean {
        return false
    }

    private fun getPixels(dipValue: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dipValue.toFloat(), resources.displayMetrics
        ).toInt()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    @Suppress("unused")
    private fun init() {
        // Enable horizontal fading
        this.isHorizontalFadingEdgeEnabled = true
        // Apply default fading length
        setEdgeLength(0)
        // Apply default side
        setFadeDirection(FadeSide.RIGHT_SIDE)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        if (drawable == null) return
        if (drawable is BitmapDrawable) {
            task = Palette.from(drawable.bitmap).generate {
                it?.let { palette ->
                    val layerDrawable = LayerDrawable(
                        arrayOf(
                            drawable, GradientDrawable(
                                GradientDrawable.Orientation.TL_BR,
                                intArrayOf(
                                    UiUtils.adjustAlpha(
                                        palette.getDominantColor(Color.TRANSPARENT),
                                        0.2f
                                    ),
                                    UiUtils.adjustAlpha(
                                        palette.getVibrantColor(Color.TRANSPARENT),
                                        0.6f
                                    )
                                )
                            )
                        )
                    )
                    super.setImageDrawable(layerDrawable)
                }
            }
        }
    }

    @Suppress("deprecation")
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        task?.cancel(true)
    }
}