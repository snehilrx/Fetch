package com.otaku.fetch.base

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.otaku.fetch.AppbarController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TestActivity : AppCompatActivity(), AppbarController {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }


    override fun getAppbar(): AppBarLayout {
        return  findViewById(R.id.appbar)
    }

    override fun getCollapsingToolbar(): CollapsingToolbarLayout {
        return  findViewById(R.id.collapsing_toolbar)
    }

    override fun getToolbar(): Toolbar {
        return  findViewById(R.id.toolbar)
    }

    override fun getAppBarImage(): ImageView {
        TODO("Not yet implemented")
    }

    override fun getAppBarEpisodeChip(): TextView {
        TODO("Not yet implemented")
    }

    override fun saveAppbar(): Int? {
        TODO("Not yet implemented")
    }

    override fun restoreAppbar(id: Int) {
    }

    override fun getFullScreenVideoView(): FrameLayout {
        TODO("Not yet implemented")
    }
}