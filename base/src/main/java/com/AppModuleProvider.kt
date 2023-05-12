package com

import com.otaku.fetch.AppModule

interface AppModuleProvider {
    val currentModule: AppModule?
}