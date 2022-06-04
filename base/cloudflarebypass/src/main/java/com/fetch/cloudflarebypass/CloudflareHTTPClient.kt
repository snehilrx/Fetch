/*
 * Designed and developed by Marco Cimolai (marplex)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fetch.cloudflarebypass

import com.fetch.cloudflarebypass.exceptions.UnsupportedChallengeException
import com.fetch.cloudflarebypass.uam.UAMPageAtributes
import com.fetch.cloudflarebypass.uam.UAMSettings
import com.fetch.cloudflarebypass.util.DefaultJavascriptEvaluator
import com.fetch.cloudflarebypass.util.JavascriptEvaluator
import com.fetch.cloudflarebypass.util.VolatileCookieJar
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import ru.gildor.coroutines.okhttp.await

/**
 * Use this class to perform request to cloudflare-protected websites and bypass
 * the controls.
 *
 * Example usage
 * <code>
 * val client = CloudflareHTTPClient()
 * runBlocking {
 *  val response = client.request("https://www.example.com") //OkHttp Response object
 *  println(response.code)
 * }
 * </code>
 */
@Suppress("BlockingMethodInNonBlockingContext")
open class CloudflareHTTPClient(
    logInterceptor: Interceptor,
    uamSettingsBlock: (UAMSettings.() -> Unit)? = null
) {

    init {
        JavascriptEvaluator.set(DefaultJavascriptEvaluator())
    }

    /**
     * Http client used by the bypasser.
     * It can be customized using the class constructor block
     */
    var okHttpClient: OkHttpClient

    /**
     * Contains custom user settings
     * Es: delay, http client blocks
     */
    private var uamSettings: UAMSettings

    /**
     * List of supported chipers.
     * NOTE: The list is small to avoid cloudflare redirecting to captcha challenges
     */
    private val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
        .tlsVersions(TlsVersion.TLS_1_2)
        .cipherSuites(
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256
        )
        .build()

    init {
        uamSettings = UAMSettings().apply {
            uamSettingsBlock?.invoke(this)
        }

        val specs = listOf(spec, ConnectionSpec.CLEARTEXT)

        //Setup http client
        okHttpClient = OkHttpClient.Builder()
            .cookieJar(VolatileCookieJar())
            .connectionSpecs(specs)
            .apply { uamSettings.httpClient?.invoke(this) }
            .addInterceptor(logInterceptor)
            .addInterceptor interceptor@{ chain ->
                val oldRequest: Request = chain.request()
                val request = oldRequest.newBuilder()
                    .headers(headers)
                    .get()
                    .build()

                val response = chain.proceed(request)
                if (response.code == 200 || response.code == 404 || response.code == 403) return@interceptor response

                val page = response.body.string()
                return@interceptor when {
                    isIUAMChallenge(response, page) -> runBlocking {
                        solveCFChallenge(
                            response,
                            page
                        )
                    }
                    isCaptchaChallenge(response, page) -> throw UnsupportedChallengeException()
                    else -> response
                }
            }
            .build()
    }

    /**
     * Default headers.
     * These headers must be all set in order to bypass the IUAM page
     */
    private val headers = Headers.headersOf(
        "User-Agent",
        "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:73.0) Gecko/20100101 Firefox/73.0",
        "Upgrade-Insecure-Requests",
        "1",
        "Accept-Language",
        "en-US,en;q=0.5",
        "Accept",
        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
    )

    /**
     * Start a get request asynchronously.
     * If the cookies are stored it should automatically return the real page if __cf_clearance is set
     *
     * @param url Where to request
     * @param block Optional block param to customize the request
     *
     * @return OkHttp response with bypassed content
     */
    suspend fun get(url: HttpUrl, block: (Request.Builder.() -> Unit)? = null): Response {
        val request = Request.Builder()
            .url(url)
            .headers(headers)
            .apply { block?.invoke(this) }
            .get()
            .build()

        val response = okHttpClient.newCall(request).await()
        if (response.code == 200 || response.code == 404 || response.code == 403) return response

        val page = response.body.string()
        return when {
            isIUAMChallenge(response, page) -> solveCFChallenge(response, page)
            isCaptchaChallenge(response, page) -> throw UnsupportedChallengeException()
            else -> response
        }
    }

    /**
     * Start a post request asynchronously
     * If the cookies are stored it should automatically return the real page if "__cf_clearance" is set
     *
     * @param url Where to request
     * @param data Data sent to the web-server
     * @param block Optional block param to customize the request
     *
     * @return OkHttp response with bypassed content
     */
    suspend fun post(
        url: HttpUrl,
        data: FormBody,
        block: (Request.Builder.() -> Unit)? = null
    ): Response {
        val request = Request.Builder()
            .url(url)
            .headers(headers)
            .apply { block?.invoke(this) }
            .post(data)
            .build()

        val response = okHttpClient.newCall(request).await()
        if (response.code == 200 || response.code == 404 || response.code == 403) return response

        val page = response.body.string()

        return when {
            isIUAMChallenge(response, page) -> solveCFChallenge(response, page)
            isCaptchaChallenge(response, page) -> throw UnsupportedChallengeException()
            else -> response
        }
    }


    /**
     * Solve the IUAM challenge by evaluating parts of javascripts inside the page.
     * This method is asynchronous and uses Rhino mozilla engine.
     *
     * @param response OkHttp response of the IUAM page
     * @param page Body content of the response
     *
     * @return OkHttp response with bypassed content
     */
    private suspend fun solveCFChallenge(response: Response, page: String): Response {
        val urlTemplate = "%s://%s"
        val scheme = response.request.url.scheme
        val host = response.request.url.host

        kotlinx.coroutines.delay(uamSettings.delay)

        //By accessing "formParams" the JS is automatically resolved and the challenge completed
        val attributes = UAMPageAtributes(scheme, host, page)
        val formParams = attributes.formParams

        //Build the new url
        val urlToConnect = String.format(
            urlTemplate,
            scheme,
            host
        ) + formParams.action.first + formParams.action.second
        val httpUrl = urlToConnect.toHttpUrl().newBuilder()
            .addQueryParameter(
                formParams.action.first.substringAfter("?"),
                formParams.action.second
            )
            .addQueryParameter("jschl_vc", formParams.jschlVc)
            .addQueryParameter("pass", formParams.pass)
            .addQueryParameter("jschl_answer", formParams.jschlAnswer)
            .build()

        //Build the body with challenge answer
        val formBody = FormBody.Builder()
            .add("r", formParams.r)
            .add("jschl_vc", formParams.jschlVc)
            .add("pass", formParams.pass)
            .add("jschl_answer", formParams.jschlAnswer)
            .build()

        //Build the post request
        val request = Request.Builder()
            .url(httpUrl)
            .headers(headers)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .post(formBody)
            .build()

        //This should return the real website with cf_clearance cookie
        //used to automatically skip the countdown the next times
        return okHttpClient.newCall(request).await()
    }


    /**
     * Check if the page is cloudflare's IUAM page
     *
     * @param response OkHttp response of the initially requested page (the one that could be IUAM)
     * @param page Body content of the response
     *
     * @return true if the page is IUAM
     */
    private fun isIUAMChallenge(response: Response, page: String) =
        response.code in 503 downTo 429
                && response.headers["Server"]!!.startsWith("cloudflare")
                && page.contains("jschl_answer")

    /**
     * Check if the page contains a Captcha challenge
     *
     * @param response OkHttp response of the initially requested page
     * @param page Body content of the response
     *
     * @return true if the page contains a Captcha challenge
     */
    private fun isCaptchaChallenge(response: Response, page: String) =
        response.code == 403
                && response.headers["Server"]!!.startsWith("cloudflare")
                && page.contains("/cdn-cgi/l/chk_captcha")

}