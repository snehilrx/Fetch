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

import com.fetch.cloudflarebypass.uam.UAMSettings
import com.fetch.cloudflarebypass.util.DefaultJavascriptEvaluator
import com.fetch.cloudflarebypass.util.JavascriptEvaluator
import com.fetch.cloudflarebypass.util.VolatileCookieJar
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion

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
    log: Log,
    uamSettingsBlock: (UAMSettings.() -> Unit)? = null
) {

    init {
        JavascriptEvaluator.set(DefaultJavascriptEvaluator())
    }

    /**
     * Http client used by the bypasser.
     * It can be customized using the class constructor block
     */
    var okHttpClient: OkHttpClient.Builder

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
            .addInterceptor(CloudflareInterceptor(log, uamSettings))
    }

}