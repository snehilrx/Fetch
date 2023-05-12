package com.otaku.fetch.work

import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestDriver
import androidx.work.testing.WorkManagerTestInitHelper
import com.otaku.kickassanime.page.adapters.AnimeTileAdapter.Companion.TAG
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltAndroidTest
internal class FeedFetcherTest {

    @Inject
    @ApplicationContext
    lateinit var context: Context
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() = hiltRule.inject()

    @Test
    @Throws(Exception::class)
    fun testPeriodicWork() {

        // Create periodic work request
        val request = PeriodicWorkRequest.Builder(FeedFetcher::class.java, 15, TimeUnit.MINUTES)
            .build()
        val synchronousExecutor = SynchronousExecutor()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(synchronousExecutor)
            .setWorkerFactory(workerFactory)
            .build())
        // Enqueue periodic request
        val instance = WorkManager.getInstance(context)
        instance
            .enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.UPDATE, request)

        // Initialize testDriver
        val testDriver: TestDriver? = WorkManagerTestInitHelper.getTestDriver(context)
        if(testDriver == null) {
            assert(false) {
                "Test Driver was null"
            }
            return
        }

        // Tells the testing framework the period delay is met, this will execute your code in doWork() in MyWorker class
        testDriver.setPeriodDelayMet(request.id)
        testDriver.setAllConstraintsMet(request.id)
        testDriver.setInitialDelayMet(request.id)

        val workInfoById = instance.getWorkInfoByIdLiveData(request.id)
        var flag = false
        Handler(context.mainLooper).postAtFrontOfQueue {
            workInfoById.observeForever {
                flag = it.state == WorkInfo.State.ENQUEUED
            }
        }
        while (!flag) Thread.sleep(200)
    }
}