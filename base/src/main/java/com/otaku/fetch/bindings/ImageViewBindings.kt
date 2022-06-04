package com.otaku.fetch.bindings

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide

object ImageViewBindings {

    @JvmStatic
    @BindingAdapter("imageUrl")
    fun imageUrl(view: ImageView, url: String?) {
        if (url?.isNotEmpty() == true) {
            val circularProgressDrawable = CircularProgressDrawable(view.context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            Glide.with(view)
                .load(url)
                .centerCrop()
                .placeholder(circularProgressDrawable)
                .into(view)
        }
    }
}