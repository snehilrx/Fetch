package com.otaku.fetch.bindings

import android.graphics.drawable.Drawable
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.mikepenz.iconics.IconicsColor
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.utils.color
import com.mikepenz.iconics.utils.icon
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.iconics.view.IconicsButton
import com.mikepenz.iconics.view.IconicsCompoundButton

object MaterialButtonBinding {

    @JvmStatic
    @BindingAdapter("endIcon")
    fun endIcon(view: MaterialButton, icon: String) {
        if (icon.isNotEmpty()) {
            val drawable = IconicsDrawable(view.context).icon(icon).apply {
                sizeDp = 24
                color = IconicsColor.colorList(view.iconTint)
            } as Drawable
            view.icon = drawable
            view.iconGravity = MaterialButton.ICON_GRAVITY_END
        }
    }

    @JvmStatic
    @BindingAdapter("startIcon")
    fun startIcon(view: MaterialButton, icon: String) {
        if (icon.isNotEmpty()) {
            val drawable = IconicsDrawable(view.context).icon(icon).apply {
                sizeDp = 24
                color = IconicsColor.colorList(view.iconTint)
            } as Drawable
            view.icon = drawable
            view.iconGravity = MaterialButton.ICON_GRAVITY_START
        }
    }
}