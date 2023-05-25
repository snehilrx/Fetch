package com.otaku.fetch

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.hilt.work.HiltWorkerFactory
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import androidx.work.WorkManager
import com.AppModuleProvider
import com.otaku.fetch.base.settings.Settings
import com.otaku.fetch.base.settings.dataStore
import com.otaku.fetch.base.ui.BindingActivity.Companion.REPO_LINK
import com.otaku.fetch.work.AnimeNotifier
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import org.acra.config.mailSenderConfiguration
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
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
        initAcra {
            reportFormat = StringFormat.JSON
        }
        mailSenderConfiguration {
            mailTo = "analytics4anime@proton.me"
            reportAsFile = true
            reportFileName = "Crash.txt"
            subject = ""
            body = ""
        }

        findModules()
        AnimeNotifier().schedulePeriodicWork(WorkManager.getInstance(this.applicationContext))
        createNotificationChannel()
        val updater = ApkUpdater(this, REPO_LINK)
        kotlinx.coroutines.MainScope().launch {
            dataStore.edit {
                it[Settings.PREF_NEW_UPDATE_FOUND] = updater.isNewUpdateAvailable() ?: false
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
            } catch (e: ClassNotFoundException) {
                break
            }
        }
    }
}