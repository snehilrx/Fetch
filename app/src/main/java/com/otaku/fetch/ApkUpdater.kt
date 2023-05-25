package com.otaku.fetch


import android.content.Context
import java.net.URL


class ApkUpdater(private val context: Context, url: String) {
    private var response: String = URL(url).readText()

    fun isNewUpdateAvailable(): Boolean? {
        val latestVersion =
            Regex("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+").find(response)?.value
        val currentVersionName =
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        if (latestVersion != null) return latestVersion != currentVersionName
        return null
    }


}