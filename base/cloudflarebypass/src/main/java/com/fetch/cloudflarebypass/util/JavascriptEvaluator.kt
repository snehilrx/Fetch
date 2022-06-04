package com.fetch.cloudflarebypass.util


/**
 * Interface defining the javascript evaluator methods.
 * It also contains a static reference to the current evaluator
 */
interface JavascriptEvaluator {

    /**
     * Evaluates javascript
     *
     * @param javascript Code to evaluate
     * @return Evaluated string result
     */
    fun evaluateString(javascript: String): String

    companion object {
        private var EVALUATOR: JavascriptEvaluator? = null

        fun get() =
            EVALUATOR ?: throw Exception("Set a JS evaluator to bypass Cloudflare IUAM page!")

        fun set(evaluator: JavascriptEvaluator) {
            EVALUATOR = evaluator
        }
    }

}