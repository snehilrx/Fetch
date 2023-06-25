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
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.*


class CustomWebView : WebView {

    var onPageFinished: (() -> Unit)? = null
    var videoLinksCallback: ((url: Uri) -> Unit)? = null
    var crunchyRollCallback: ((json: String) -> Unit)? = null

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
        addJavascriptInterface(KAACrunchyInterface {
            crunchyRollCallback?.invoke(it)
        }, "android")

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
                if (blockedLinks.contains(urlString)
                    || mimeType?.startsWith("font") == true
                    || mimeType?.startsWith("image") == true
                ) {
                    return WebResourceResponse(
                        mimeType,
                        "UTF-8",
                        null
                    )
                }
                if (mimeType != null && extension != "html") {
                    // check if any of the requestUrls contain the url of a video file
                    if (mimeType.startsWith("video/") || mimeType.startsWith("audio/")) {
                        videoLinksCallback?.invoke(requestUrl)
                        return WebResourceResponse(
                            mimeType,
                            "UTF-8",
                            null
                        )
                    }
                }
                if (urlString.contains("player.php", true)) {
                    return injectToIframe(urlString)
                }
                return super.shouldInterceptRequest(view, request)
            }

            private fun injectToIframe(url: String): WebResourceResponse? {
                val httpURL = url.toHttpUrlOrNull() ?: return null
                val content = httpURL.toUrl().readText()

                val scriptToInject = "\n<script>\n" +
                        "   (function() {\n" +
                        "     setTimeout(function send(){try{var item = player.getPlaylistItem(); if(item == undefined) {setTimeout(send, 200)} else { android.inject(JSON.stringify(item))}}catch(e){setTimeout(send, 200)}},500);\n" +
                        "   })()\n" +
                        "</script>\n"
                val split = content.split("</head>")
                val newContent = "${split[0]}${scriptToInject}</head>${split[1]}"
                val inStream = newContent.byteInputStream()
                val statusCode = 200
                val reasonPhase = "OK"
                val responseHeaders: MutableMap<String, String> = HashMap()
                return WebResourceResponse(
                    "text/html",
                    "utf-8",
                    statusCode,
                    reasonPhase,
                    responseHeaders,
                    inStream
                )
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if (request?.url?.toString()?.equals("about:blank") == true) {
                    val randomTimeOut =
                        Math.random() * 500 + Math.random() * 2000 + Math.random() * 500
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

    class KAACrunchyInterface(
        private val callback: (String) -> Unit
    ) {
        @JavascriptInterface
        fun inject(jsonData: String) {
            callback(jsonData)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = Bundle()
        val webState = Bundle()
        state.putParcelable("superState", super.onSaveInstanceState())
        saveState(webState)
        state.putBundle("webState", webState)
        return state
    }

    @Suppress("deprecation")
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

    companion object {
        @JvmStatic
        private val blockedLinks = setOf(
            "doctorenticeflashlights.com",
            "kickassanime.disqus.com",
            "simplewebanalysis",
            "google",
            "manifest",
            "/api/show/"
        )
    }
}

