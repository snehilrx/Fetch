package com.fetch.cloudflarebypass

import com.fetch.cloudflarebypass.exceptions.UnsupportedChallengeException
import com.fetch.cloudflarebypass.uam.UAMPageAtributes
import com.fetch.cloudflarebypass.uam.UAMSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl

class CloudflareInterceptor(private val log: Log, private val uamSettings: UAMSettings) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest: Request = chain.request()
        val request = oldRequest.newBuilder()
            .headers(headers)
            .get()
            .build()

        val response = chain.proceed(request)
        log.i(TAG, "intercept: $response")
        if (response.code == 200 || response.code == 404 || response.code == 403) return response

        val page = response.body.string()
        return when {
            isIUAMChallenge(response, page) -> runBlocking {
                log.i(TAG, "intercept: got IUAMChallenge")
                withContext(Dispatchers.IO) {
                    chain.proceed(
                        solveCFChallenge(
                            response,
                            page
                        )
                    )
                }
            }
            isCaptchaChallenge(response, page) -> {
                log.e(TAG, "Unsupported challenge $page")
                throw UnsupportedChallengeException()
            }
            else -> {
                log.i(TAG, "intercept: got normal request")
                response
            }
        }
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
     * Solve the IUAM challenge by evaluating parts of javascripts inside the page.
     * This method is asynchronous and uses Rhino mozilla engine.
     *
     * @param response OkHttp response of the IUAM page
     * @param page Body content of the response
     *
     * @return OkHttp response with bypassed content
     */
    private suspend fun solveCFChallenge(
        response: Response,
        page: String
    ): Request {
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

        //This should return the real website with cf_clearance cookie
        //used to automatically skip the countdown the next times

        return Request.Builder()
            .url(httpUrl)
            .headers(headers)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .post(formBody)
            .build()
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

    companion object {
        private const val TAG = "CloudflareInterceptor"
    }
}
