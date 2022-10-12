package com.otaku.fetch.base.ui

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.palette.graphics.Palette

object UiUtils {
    fun getColor(icon: Drawable?, after: (Int) -> Unit) {
        if (icon is BitmapDrawable) {
            Palette.from(icon.bitmap).generate {
                it?.let {
                    after(it.getVibrantColor(Color.RED))
                }
            }
        }
    }

    val Number.toPx
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )

    val Number.toPxInt
        get() = toPx.toInt()
}