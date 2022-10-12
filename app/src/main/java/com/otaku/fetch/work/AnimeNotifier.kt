package com.otaku.fetch.work

import androidx.work.*
import java.util.concurrent.TimeUnit

class AnimeNotifier {

    fun schedulePeriodicWork(workManager: WorkManager) {
        workManager.enqueueUniquePeriodicWork(
            "fetch_anime_updates",
            ExistingPeriodicWorkPolicy.REPLACE,
            PeriodicWorkRequest.Builder(FeedFetcher::class.java, 15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                ).setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .build()
        )
    }

}