package com.otaku.kickassanime

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.TaskStackBuilder
import androidx.test.platform.app.InstrumentationRegistry
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.db.KickassAnimeDb
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
internal class KickassAppModuleTest {

    @JvmField
    @Rule
    val hiltTestRule = HiltAndroidRule(this)


    @ApplicationContext
    @Inject
    lateinit var context: Context


    @Inject
    lateinit var api: KickassAnimeService


    @Inject
    lateinit var db: KickassAnimeDb

    @Before
    fun setUp() {
        hiltTestRule.inject()
    }

    @After
    fun tearDown() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }

    @Test
    fun triggerNotification() {
        val kickassAppModule = KickassAppModule(api, db, context)
        runBlocking {
            kickassAppModule.triggerNotification(context, TaskStackBuilder.create(context))
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Wait until the active notification list has a new one
        InstrumentationRegistry.getInstrumentation()
            .waitForIdle { manager.activeNotifications.isNotEmpty() }

        // Validate the notification info
        with(manager.activeNotifications.first()) {
            // write test
        }
    }
}