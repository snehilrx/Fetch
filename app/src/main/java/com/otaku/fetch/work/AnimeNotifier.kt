package com.otaku.fetch.work

import androidx.work.*
import java.util.concurrent.TimeUnit

class AnimeNotifier {

    fun schedulePeriodicWork(workManager: WorkManager) {
        workManager.enqueueUniquePeriodicWork(
            "fetch_anime_updates",
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequest.Builder(FeedFetcher::class.java, 1, TimeUnit.HOURS)
                .setInitialDelay(2, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                ).setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .build()
        )
    }

}