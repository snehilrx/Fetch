package com.otaku.fetch.work

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class AnimeNotifier {

    fun schedulePeriodicWork(workManager: WorkManager) {
        workManager.enqueueUniquePeriodicWork(
            "fetch_anime_updates",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            PeriodicWorkRequest.Builder(FeedFetcher::class.java, 2, TimeUnit.HOURS)
                .setInitialDelay(25, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresDeviceIdle(true)
                        .setRequiredNetworkType(NetworkType.CONNECTED).build()
                ).build()
        )
    }

}