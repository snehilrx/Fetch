package com.otaku.fetch.work

import androidx.work.*
import java.util.concurrent.TimeUnit

class AnimeNotifier {

    fun schedulePeriodicWork(workManager: WorkManager) {
        val work_id = "fetch_anime_updates"
        workManager.enqueueUniquePeriodicWork(
            work_id,
            ExistingPeriodicWorkPolicy.REPLACE,
            PeriodicWorkRequest.Builder(FeedFetcher::class.java, 20, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .build()
        )
    }

}