package com.otaku.fetch.base.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
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
import com.maxkeppeler.sheets.info.InfoSheet
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import com.otaku.fetch.base.TAG
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

    fun getThemeColor(theme: Resources.Theme, id: Int = android.R.attr.colorPrimary): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(id, typedValue, true)
        return typedValue.data
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

    fun showError(loadingError: Throwable?, activity: Activity, text: String = "ok", onPositive: () -> Unit = {activity.finish()}) {
        showError(loadingError?.message, activity, onPositive, text)
    }

    fun showError(message: String?, activity: Activity, onPositive: () -> Unit = {activity.finish()}, text: String = "ok"){
        val errorIcon = IconicsDrawable(activity, FontAwesome.Icon.faw_bug).apply {
            colorInt = Color.RED
            sizeDp = 24
        }
        Log.e(TAG, "showError: $message")
        InfoSheet().show(activity) {
            title("Oops, we got an error")
            content(message ?: "Something went wrong")
            onPositive(text, errorIcon) {
                dismiss()
                onPositive()
            }
        }
    }
}