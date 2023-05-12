package com.otaku.fetch.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.otaku.fetch.AppModule
import com.otaku.fetch.ModuleRegistry
import com.otaku.fetch.base.TAG
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Named

@HiltWorker
class FeedFetcher @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {


    override suspend fun doWork(): Result {
        return try {
            ModuleRegistry.getModulesList().forEach {module ->
                module.appModule?.triggerNotification(context)
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed while getting updates", e)
            Result.failure()
        }
    }
}