package com.otaku.fetch.base.ui

import android.os.SystemClock
import android.view.View
import android.view.View.OnClickListener


private var lastClick = 0L
private const val TIMEOUT = 500

class DebounceClickEvent(val event: () -> Unit) : OnClickListener {
    @Synchronized
    override fun onClick(v: View?) {
        val currentTime = SystemClock.elapsedRealtime()
        if(currentTime-lastClick > TIMEOUT) {
            event()
            lastClick = SystemClock.elapsedRealtime()
        }
    }
}

fun View.setOnClick(debounceClickEvent: () -> Unit) = this.setOnClickListener(DebounceClickEvent(debounceClickEvent))