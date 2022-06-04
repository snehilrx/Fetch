package com.fetch.cloudflarebypass.util

import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import java.util.concurrent.Callable
import java.util.concurrent.Executors


/**
 * Class containing all methods used to evaluate and run javascript.
 * Implements JavascriptEvaluator
 */
class DefaultJavascriptEvaluator : JavascriptEvaluator {

    private val executionThread = Executors.newSingleThreadExecutor()

    override fun evaluateString(javascript: String): String {
        return executionThread.submit(Callable {
            val context = Context.enter().apply {
                optimizationLevel = -1
            }

            val scope: Scriptable = context.initSafeStandardObjects()
            context.evaluateString(scope, javascript, "cloudflare", 1, null).toString()
        }).get()
    }
}