package com.otaku.fetch.bindings

import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ImageView
import androidx.annotation.RequiresPermission
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.otaku.fetch.base.TAG
import java.lang.RuntimeException


object ImageViewBindings {
    @JvmStatic
    @BindingAdapter("imageUrl")
    fun imageUrl(view: ImageView, url: String?) {
        if (url?.isNotEmpty() == true) {
            val animatedVectorDrawable =
                ResourcesCompat.getDrawable(
                    view.resources,
                    com.otaku.fetch.base.R.drawable.loading,
                    view.context.theme
                ) as AnimatedVectorDrawable
            animatedVectorDrawable.start()
            try {
                Glide.with(view.context)
                    .load(url)
                    .centerCrop()
                    .listener(object : RequestListener<Drawable>{
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            animatedVectorDrawable.stop()
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            animatedVectorDrawable.stop()
                            return false
                        }
                    })
                    .placeholder(
                        animatedVectorDrawable
                    ).into(view)
            } catch (e: RuntimeException) {
                Log.e(TAG, "imageUrl: Glide is fucked up.", e)
            }
        }
    }
}