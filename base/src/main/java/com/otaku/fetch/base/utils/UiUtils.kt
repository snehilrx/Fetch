package com.otaku.fetch.base.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import kotlin.math.roundToInt

object UiUtils {

    @ColorInt
    fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val fl = (Color.alpha(color) * factor)
        val alpha = (if (fl.isNaN()) 0 else fl.roundToInt() + 50).coerceAtMost(255)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    fun getColor(icon: Drawable?, after: (Int) -> Unit) {
        if (icon is BitmapDrawable) {
            Palette.from(icon.bitmap).generate {
                it?.let {
                    after(it.getVibrantColor(Color.RED))
                }
            }
        }
    }

    fun loadBitmapFromUrl(image: String?, context: Context, vararg  transformation: Transformation<Bitmap>, after: (Bitmap?) -> Unit) {
        if (image == null){
            after(null)
            return
        }
        Glide.with(context).load(image)
            .transform(*transformation)
            .addListener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                after(null)
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                resource?.let {
                    after(it.toBitmap())
                }
                return false
            }
        }).submit()
    }

    fun loadBitmapFromUrlWithRoundedCorners(image: String?, context: Context, radiusInDp: Int, after: (Bitmap?) -> Unit) {
        return loadBitmapFromUrl(image, context, RoundedCorners(radiusInDp.toPxInt)) {after(it)}
    }

    inline val Number.toPx
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )

    inline val Number.toPxInt
        get() = toPx.toInt()
}