package com.otaku.fetch

import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout

interface AppbarController {
    fun getAppbar(): AppBarLayout
    fun getCollapsingToolbar(): CollapsingToolbarLayout
    fun getToolbar(): Toolbar
    fun getAppBarImage(): ImageView
    fun getAppBarEpisodeChip(): TextView
    fun saveAppbar(): Int?
    fun getFullScreenVideoView(): FrameLayout
    fun restoreAppbar(offset: Int)
}