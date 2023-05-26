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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anggrayudi.materialpreference.util.openWebsite
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
import com.otaku.fetch.base.settings.dataStore
import com.otaku.fetch.base.ui.BindingActivity.Companion.REPO_LINK
import kotlinx.coroutines.flow.collectLatest
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
        Log.e("ERROR_SHOWN", "Something happened during network call", loadingError)
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
            title("What the fuck just happened?")
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


    fun AppCompatActivity.statusBarHeight(delayedUpdate: (Int) -> Unit): Int {
        val rootWindowInsets = window?.decorView?.rootWindowInsets
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dataStore.data.collectLatest {
                    it[PREF_STATUS_BAR_HEIGHT]?.let { it1 -> delayedUpdate(it1) }
                }
            }
        }
        return rootWindowInsets?.let {
            val insets = WindowInsetsCompat.toWindowInsetsCompat(rootWindowInsets)
                .getInsets(WindowInsetsCompat.Type.statusBars())
            StatusBarHeight.value = insets.top
            lifecycleScope.launch {
                dataStore.edit {
                    it[PREF_STATUS_BAR_HEIGHT] = insets.top
                }
            }
            StatusBarHeight.value
        } ?: StatusBarHeight.value
    }

    fun showUpdate(activity: AppCompatActivity) {
        InfoSheet().show(activity) {
            title(activity.getString(R.string.new_update_avalaible))
            content(activity.getString(R.string.new_update_avalaible_desc))
            displayPositiveButton(true)
            onPositive("Open") {
                activity.openWebsite(REPO_LINK)
                dismiss()
            }
        }
    }

    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()

    private object StatusBarHeight {
        var value: Int = 0
    }


    @JvmStatic
    val PREF_STATUS_BAR_HEIGHT = intPreferencesKey("status_bar_height")
}