package com.otaku.fetch

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.hilt.work.HiltWorkerFactory
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import androidx.work.WorkManager
import com.otaku.fetch.base.settings.Settings
import com.otaku.fetch.base.settings.dataStore
import com.otaku.fetch.base.ui.BindingActivity.Companion.REPO_LINK
import com.otaku.fetch.work.AnimeNotifier
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltAndroidApp
class FetchApplication : MultiDexApplication(), Configuration.Provider, AppModuleProvider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override var currentModule: AppModule? = null

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        findModules()
        AnimeNotifier().schedulePeriodicWork(WorkManager.getInstance(this.applicationContext))
        createNotificationChannel()
        checkForUpdates()
    }

    private fun checkForUpdates() {
        kotlinx.coroutines.MainScope().launch {
            dataStore.data.collectLatest {
                try {
                    dataStore.edit { editable ->
                        editable[Settings.PREF_NEW_UPDATE_FOUND] =
                            ApkUpdater(this@FetchApplication, REPO_LINK).isNewUpdateAvailable()
                                ?: false
                    }
                } catch (_: Exception) {
                    // internet not connected
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val name = "Downloads"
        val descriptionText = "Downloads"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("download_channel", name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }

    private fun findModules() {
        var count = 0
        while (true) {
            try {
                val loadClass = classLoader.loadClass("$packageName.ModuleRegistration$count")
                val newInstance = loadClass.getDeclaredConstructor().newInstance()
                if (newInstance is ModuleLoaders) {
                    newInstance.load(this)
                }
                count++
            } catch (_: ClassNotFoundException) {
                break
            }
        }
    }
}