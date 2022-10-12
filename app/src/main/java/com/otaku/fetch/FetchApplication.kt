package com.otaku.fetch

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidApp
class FetchApplication : MultiDexApplication() {

    @Inject
    @Named("kickassanime")
    lateinit var kissanimeModule: AppModule

    override fun onCreate() {
        super.onCreate()
        kissanimeModule.initialize(this)
    }

}