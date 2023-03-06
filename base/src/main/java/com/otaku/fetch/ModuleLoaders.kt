package com.otaku.fetch

import android.content.Context

interface ModuleLoaders {
    fun load(applicationContext: Context)
}