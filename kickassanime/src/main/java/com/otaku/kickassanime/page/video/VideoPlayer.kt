package com.otaku.kickassanime.page.video

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.webkit.WebChromeClient
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.navArgs
import com.otaku.kickassanime.R
import com.otaku.kickassanime.databinding.ActivityVideoPlayerBinding

class VideoPlayer : AppCompatActivity() {

    private val args by navArgs<VideoPlayerArgs>()
    lateinit var binding: ActivityVideoPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_player)
        initWebView()
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun initWebView() {
        binding.webView.webChromeClient = object : WebChromeClient(){
            override fun getVideoLoadingProgressView(): View {
                return binding.progressIndicator
            }
        }
        binding.webView.settings.setSupportMultipleWindows(false)
        binding.webView.settings.javaScriptCanOpenWindowsAutomatically = false
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.loadUrl(args.videoEmbededLink)
    }
}