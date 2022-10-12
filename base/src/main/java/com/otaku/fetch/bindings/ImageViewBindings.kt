package com.otaku.fetch.bindings

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.otaku.fetch.base.R

object ImageViewBindings {
    @JvmStatic
    @BindingAdapter("imageUrl")
    fun imageUrl(view: ImageView, url: String?) {
        if (url?.isNotEmpty() == true) {
            Glide.with(view)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.loading)
                .into(view)
        }
    }
}