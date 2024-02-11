package com.appspell.shaderview.log

import android.util.Log

object LibLog {
    var isEnabled: Boolean = false

    fun i(tag: String, message: String) {
        if (!isEnabled) return
        Log.i(tag, message)
    }

    fun d(tag: String, message: String) {
        if (!isEnabled) return
        Log.d(tag, message)
    }

    fun w(tag: String, message: String) {
        if (!isEnabled) return
        Log.w(tag, message)
    }

    fun e(tag: String, message: String) {
        if (!isEnabled) return
        Log.e(tag, message)
    }

    fun v(tag: String, message: String) {
        if (!isEnabled) return
        Log.v(tag, message)
    }

    fun e(tag: String, message: String, e: Exception) {
        if (!isEnabled) return
        Log.e(tag, message, e)
    }
}