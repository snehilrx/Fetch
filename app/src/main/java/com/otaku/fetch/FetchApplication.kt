package com.otaku.fetch

import androidx.hilt.work.HiltWorkerFactory
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import androidx.work.WorkManager
import com.otaku.fetch.work.AnimeNotifier
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class FetchApplication() : MultiDexApplication(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }


    override fun onCreate() {
        super.onCreate()
        findModules()
        AnimeNotifier().schedulePeriodicWork(WorkManager.getInstance(this.applicationContext))
    }

    private fun findModules() {
        var count = 0
        while (true) {
            try {
                val loadClass = classLoader.loadClass("$packageName.ModuleRegistration$count")
                val newInstance = loadClass.newInstance()
                if (newInstance is ModuleLoaders) {
                    newInstance.load(this)
                }
                count++
            } catch (e: ClassNotFoundException) {
                break
            }
        }
    }
}