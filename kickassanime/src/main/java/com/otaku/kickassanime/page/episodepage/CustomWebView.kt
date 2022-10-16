package com.otaku.kickassanime.page.episodepage

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.webkit.*
import com.otaku.kickassanime.Strings.KAA2_URL
import com.otaku.kickassanime.Strings.KAA_URL
import com.otaku.kickassanime.Strings.MAVERICKKI_URL


class CustomWebView : WebView {

    var onPageFinished: (() -> Unit)? = null
    var onProgressChanged: ((Int) -> Unit)? = null
    var videoLinksCallback: ((url: Uri) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @SuppressWarnings("unused")
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    @SuppressWarnings("unused")
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
                    }
                }

                if (mimeType != null) {
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

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                onPageFinished?.invoke()
            }
        }


        webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
                return true
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onProgressChanged?.invoke(newProgress)
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
                val superState = newState.getParcelable<Parcelable>("superState")
                super.onRestoreInstanceState(superState)
                val webState = newState.getBundle("webState")
                webState?.let {
                    restoreState(it)
                }
            }
        }
    }
}