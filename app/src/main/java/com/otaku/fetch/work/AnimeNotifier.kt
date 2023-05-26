package com.otaku.fetch.work

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.util.concurrent.TimeUnit

class AnimeNotifier {

    fun schedulePeriodicWork(workManager: WorkManager) {
        workManager.enqueueUniquePeriodicWork(
            "fetch_anime_updates",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequest.Builder(FeedFetcher::class.java, 1, TimeUnit.HOURS)
                .setInitialDelay(25, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                ).setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        )
    }

}