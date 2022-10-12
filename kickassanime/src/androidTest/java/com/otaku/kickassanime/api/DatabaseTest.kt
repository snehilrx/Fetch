package com.otaku.kickassanime.api

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.utils.asAnimeEntity
import dagger.hilt.android.testing.CustomTestApplication
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import javax.inject.Inject

@HiltAndroidTest
@CustomTestApplication(value = Application::class)
class DatabaseTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var kickassAnimeService: KickassAnimeService

    private lateinit var db: KickassAnimeDb

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, KickassAnimeDb::class.java
        ).build()
        hiltRule.inject()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() {
        runBlocking {
            val result = kickassAnimeService.getAllAnimeEntries() ?: return@runBlocking
            db.runInTransaction {
//                db.animeEntityDao().insertAll(result.map { it.asAnimeEntity() })
//                db.animeEntityDao().getAll()
            }
        }
    }
}