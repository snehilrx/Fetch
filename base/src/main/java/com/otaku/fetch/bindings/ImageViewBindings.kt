package com.otaku.fetch.bindings

import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.otaku.fetch.base.R

object ImageViewBindings {
    @JvmStatic
    @BindingAdapter("imageUrl")
    fun imageUrl(view: ImageView, url: String?) {
        if (url?.isNotEmpty() == true) {
            Glide.with(view.context)
                .load(url)
                .centerCrop()
                .placeholder(
                    ResourcesCompat.getDrawable(
                        view.resources,
                        R.drawable.loading,
                        view.context.theme
                    )
                )
                .into(view)
        }
    }
}