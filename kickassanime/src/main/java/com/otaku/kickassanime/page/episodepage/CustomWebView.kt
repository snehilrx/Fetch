package com.otaku.kickassanime.page.episodepage

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.webkit.*
import com.otaku.kickassanime.Strings.ADD_KAA
import com.otaku.kickassanime.Strings.KAA2_URL
import com.otaku.kickassanime.Strings.KAAST1
import com.otaku.kickassanime.Strings.KAA_URL
import com.otaku.kickassanime.Strings.MAVERICKKI_URL


class CustomWebView : WebView {

    var onPageFinished: (() -> Unit)? = null
    var videoLinksCallback: ((url: Uri) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @Suppress("UNUSED")
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    @Suppress("UNUSED", "DEPRECATION")
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        privateBrowsing: Boolean
    ) : super(context, attrs, defStyleAttr, privateBrowsing)

    init {
        setWebContentsDebuggingEnabled(true)
        settings.databaseEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        @SuppressLint("SetJavaScriptEnabled")
        settings.javaScriptEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.domStorageEnabled = true
        settings.useWideViewPort = false
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        settings.userAgentString = "Android"

        webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                //Get the request and assign it to a string
                val requestUrl = request.url
                val urlString = requestUrl.toString()
                val extension = MimeTypeMap.getFileExtensionFromUrl(urlString)
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                requestUrl?.let {
                    when (it.host) {
                        MAVERICKKI_URL -> {
                            if (it.pathSegments?.get(0) == "api" && it.pathSegments?.get(1) == "source") {
                                videoLinksCallback?.invoke(requestUrl)
                                return null
                            }
                        }
                        KAA_URL, KAA2_URL -> {
                            if (mimeType != null && (mimeType.startsWith("video/") || mimeType.startsWith(
                                    "audio/"
                                ))
                            ) videoLinksCallback?.invoke(requestUrl)
                        }
                        ADD_KAA, KAAST1 -> {
                            if (it.pathSegments?.get(0) == "Sapphire-Duck" && it.pathSegments?.get(1) == "player.php" && it.getQueryParameter("action") != null) {
                                videoLinksCallback?.invoke(requestUrl)
                                return WebResourceResponse(
                                    mimeType,
                                    "UTF-8",
                                    null
                                )
                            }
                        }
                    }
                }

                if (mimeType != null && extension != "html") {
                    // check if any of the requestUrls contain the url of a video file
                    if (mimeType.startsWith("video/") || mimeType.startsWith("audio/")) {
                        return WebResourceResponse(
                            mimeType,
                            "UTF-8",
                            null
                        )
                    } else if (mimeType.startsWith("text/")) {
                        return WebResourceResponse(
                            mimeType,
                            "UTF-8",
                            null
                        )
                    }

                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if(request?.url?.toString()?.equals("about:blank") == true)
                {
                    val randomTimeOut = Math.random() * 500 + Math.random() * 2000 + Math.random() * 500
                    Log.i("about:blank", "random timeout = $randomTimeOut")
                    Thread.sleep(randomTimeOut.toLong())
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                onPageFinished?.invoke()
            }
        }


        webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
                return true
            }
        }
        settings.loadsImagesAutomatically = false
        settings.blockNetworkImage = true
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = Bundle()
        val webState = Bundle()
        state.putParcelable("superState", super.onSaveInstanceState())
        saveState(webState)
        state.putBundle("webState", webState)
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        state?.let { newState ->
            if (newState is Bundle) {
                val superState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    newState.getParcelable("superState", Parcelable::class.java)
                } else {
                    newState.getParcelable("superState")
                }
                super.onRestoreInstanceState(superState)
                val webState = newState.getBundle("webState")
                webState?.let {
                    restoreState(it)
                }
            }
        }
    }
}