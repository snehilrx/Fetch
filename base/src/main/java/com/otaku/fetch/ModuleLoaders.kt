package com.otaku.fetch

import android.content.Context

fun interface ModuleLoaders {
    fun load(applicationContext: Context)
}