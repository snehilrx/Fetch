package com.otaku.fetch

import androidx.hilt.work.HiltWorkerFactory
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import androidx.work.WorkManager
import com.otaku.fetch.work.AnimeNotifier
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidApp
class FetchApplication : MultiDexApplication(), Configuration.Provider {

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
        AnimeNotifier().schedulePeriodicWork(WorkManager.getInstance(applicationContext))
    }
}