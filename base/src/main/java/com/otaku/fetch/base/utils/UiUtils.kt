package com.otaku.fetch.base.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.maxkeppeler.sheets.core.ButtonStyle
import com.maxkeppeler.sheets.info.InfoSheet
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.otaku.fetch.base.R
import com.otaku.fetch.base.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    fun getThemeColor(theme: Resources.Theme, id: Int = android.R.attr.colorPrimary): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(id, typedValue, true)
        return typedValue.data
    }

    fun loadBitmapFromUrl(
        image: String?,
        context: Context,
        vararg transformation: Transformation<Bitmap>,
        after: (Bitmap?) -> Unit
    ) {
        if (image == null) {
            after(null)
            return
        }
        Glide.with(context).load(image)
            .transform(*transformation)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    after(null)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
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

    inline val Number.toPx
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )

    inline val Number.toPxInt
        get() = toPx.toInt()

    fun showError(
        loadingError: Throwable?,
        activity: Activity,
        text: String = "ok",
        onPositive: () -> Unit = { }
    ) {
        showError(loadingError?.message, activity, onPositive, text)
    }

    fun showError(
        message: String?,
        activity: Activity,
        onPositive: () -> Unit = { activity.finish() },
        text: String = "ok"
    ) {

        val errorIcon = IconicsDrawable(activity, FontAwesome.Icon.faw_bug)
        Log.e(TAG, "showError: $message")
        InfoSheet().show(activity) {
            title("Oops, we got an error 🥵")
            content(message ?: "Something went wrong")
            displayNegativeButton(false)
            positiveButtonStyle(
                ButtonStyle.OUTLINED
            )
            onPositive(text, errorIcon) {
                dismiss()
                onPositive()
            }
        }
    }

    fun <T> throttleLatest(
        intervalMs: Long = 600L,
        coroutineScope: CoroutineScope,
        destinationFunction: (T) -> Unit
    ): (T) -> Unit {
        var throttleJob: Job? = null
        var latestParam: T? = null
        return { param: T ->
            if (latestParam != param) {
                latestParam = param
                if (throttleJob?.isCompleted != false) {
                    throttleJob = coroutineScope.launch {
                        delay(intervalMs)
                        latestParam?.let(destinationFunction)
                    }
                }
            }
        }
    }

    fun showNotificationInfo(
        activity: Activity,
        string: String,
        onPermissionRequest: (() -> Unit)? = null
    ) {
        InfoSheet().show(activity) {
            title(activity.getString(R.string.permission_required))
            content(string)
            displayPositiveButton(true)
            positiveButtonStyle(
                ButtonStyle.OUTLINED
            )
            onPermissionRequest?.let {
                displayNegativeButton(true)
                onNegative("ALLOW") {
                    onPermissionRequest()
                }
            }
            onPositive("OK") {
                dismiss()
            }
        }
    }

    val Activity.statusBarHeight
        get() = run {
            val rootWindowInsets = window?.decorView?.rootWindowInsets

            return@run rootWindowInsets?.let {
                val insets = WindowInsetsCompat.toWindowInsetsCompat(rootWindowInsets)
                    .getInsets(WindowInsetsCompat.Type.statusBars())
                StatusBarHeight.value = insets.top
                StatusBarHeight.value
            } ?: StatusBarHeight.value
        }

    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()

    private object StatusBarHeight {
        var value: Int = 0
    }
}