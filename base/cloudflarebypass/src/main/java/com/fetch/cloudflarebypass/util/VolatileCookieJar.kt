package com.fetch.cloudflarebypass.util

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * A cookie jar that stores and loads cookies from an in-memory saved HashMap
 */
class VolatileCookieJar : CookieJar {
    private val cookieStore = HashMap<String, List<Cookie>>()
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url.host] = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> = cookieStore[url.host] ?: arrayListOf()
}