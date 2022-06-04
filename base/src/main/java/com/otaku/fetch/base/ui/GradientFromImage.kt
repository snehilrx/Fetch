package com.otaku.fetch.base.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import androidx.palette.graphics.Palette
import com.google.android.material.imageview.ShapeableImageView


class GradientFromImage : ShapeableImageView {

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    @Suppress("unused")
    private fun init(attrs: AttributeSet?, defStyle: Int) {
    }

    override fun setImageDrawable(drawable: Drawable?) {
        if (drawable == null) return
        if (drawable is BitmapDrawable) {
            Palette.from(drawable.bitmap).generate {
                it?.let { palette ->
                    val layerDrawable = LayerDrawable(
                        arrayOf(
                            drawable, GradientDrawable(
                                GradientDrawable.Orientation.TL_BR,
                                intArrayOf(Color.TRANSPARENT, palette.getVibrantColor(Color.TRANSPARENT))
                            )
                        )
                    )
                    super.setImageDrawable(layerDrawable)
                }
            }
        }
    }
}