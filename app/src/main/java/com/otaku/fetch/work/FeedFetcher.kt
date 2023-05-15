package com.otaku.fetch.work

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.TaskStackBuilder
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.otaku.fetch.ModuleActivity
import com.otaku.fetch.ModuleRegistry
import com.otaku.fetch.base.TAG
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FeedFetcher @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {


    override suspend fun doWork(): Result {
        return try {
            ModuleRegistry.getModulesList().forEach { module ->
                val defaultIntent = TaskStackBuilder.create(context)
                    .addNextIntent(Intent().apply {
                        setClass(context, ModuleActivity::class.java)
                        putExtra(ModuleActivity.ARG_MODULE_NAME, module.displayName)
                        putExtra(
                            ModuleActivity.ARG_MODULE_DEEPLINK,
                            module.appModule?.notificationDeeplink
                        )
                    })
                module.appModule?.triggerNotification(context, defaultIntent)
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed while getting updates", e)
            Result.failure()
        }
    }
}