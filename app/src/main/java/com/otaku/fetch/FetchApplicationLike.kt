package com.otaku.fetch

import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorkerFactory
import androidx.multidex.MultiDex
import androidx.work.Configuration
import androidx.work.WorkManager
import com.otaku.fetch.tinker.TinkerManager
import com.otaku.fetch.work.AnimeNotifier
import com.tencent.tinker.anno.DefaultLifeCycle
import com.tencent.tinker.entry.DefaultApplicationLike
import com.tencent.tinker.lib.tinker.Tinker
import com.tencent.tinker.loader.shareutil.ShareConstants
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import javax.inject.Named


@HiltAndroidApp
class FetchApplication() : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    @Inject
    @Named("kickassanime")
    lateinit var kissanimeModule: AppModule


    override fun onCreate() {
        super.onCreate()
        AnimeNotifier().schedulePeriodicWork(WorkManager.getInstance(this.applicationContext))
    }
}