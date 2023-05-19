package io.github.snehilrx.shinebar

import android.annotation.SuppressLint
import android.content.res.Resources

object Utils {
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    @JvmStatic
    fun getStatusBarHeight(resources: Resources): Float {
        return if (mStatusBarHeight < 0) {
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                resources.getDimensionPixelSize(resourceId).toFloat()
            } else {
                0f
            }
        } else {
            mStatusBarHeight
        }
    }

    @JvmStatic
    private var mStatusBarHeight = -1.0f
}