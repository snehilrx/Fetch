package com.otaku.fetch.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.otaku.fetch.AppModule
import com.otaku.fetch.base.TAG
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject
import javax.inject.Named

@HiltWorker
class FeedFetcher @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParameters: WorkerParameters,
    @Named("kickassanime")
    private val module: AppModule
) : CoroutineWorker(context, workerParameters) {


    override suspend fun doWork(): Result {
        return try {
            module.triggerNotification(context)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed while getting updates", e)
            Result.failure()
        }
    }
}