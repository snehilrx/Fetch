package com.otaku.fetch.base.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.otaku.fetch.base.R
import com.otaku.fetch.base.databinding.ComposeBinding

abstract class ComposeFragment : BindingFragment<ComposeBinding>(R.layout.compose) {

    override fun onBind(binding: ComposeBinding, savedInstanceState: Bundle?) {
        super.onBind(binding, savedInstanceState)
        binding.compose.setContent {
            SetContent()
        }
        initializeRefresh(binding.refresh)
    }

    @Composable
    abstract fun SetContent()

    abstract fun initializeRefresh(swipeRefreshLayout: SwipeRefreshLayout)
}