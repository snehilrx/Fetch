package com.otaku.kickassanime

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.bundleOf
import androidx.media3.common.util.UnstableApi
import com.mikepenz.iconics.IconicsColor
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.color
import com.otaku.fetch.AppModule
import com.otaku.fetch.base.TAG
import com.otaku.fetch.base.download.DownloadItem
import com.otaku.fetch.base.utils.UiUtils
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.api.model.Recent
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.page.episodepage.EpisodeActivity
import com.otaku.kickassanime.page.episodepage.EpisodeActivityArgs
import com.otaku.kickassanime.ui.theme.KickassAnimeTheme
import com.otaku.kickassanime.utils.HashUtils
import com.otaku.kickassanime.utils.Utils
import com.otaku.kickassanime.utils.asAnimeEntity
import com.otaku.kickassanime.utils.asEpisodeEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject

const val PREF_KEY = "KickassAnime/lastUpdateHash"

@UnstableApi
class KickassAppModule @Inject constructor(
    val kickassAnimeService: KickassAnimeService,
    private val kickassAnimeDb: KickassAnimeDb
) : AppModule {

    override val name: String
        get() = "Kickass Anime"

    override fun onSearch(query: String) {
        TODO("Not yet implemented")
    }

    override fun initialize(query: String?, link: String) {
    }

    override fun getNavigationGraph(): Int {
        return R.navigation.navigation_kickassanime
    }

    override fun getBottomNavigationMenu(): Int {
        return R.menu.menu_front_page
    }

    @Composable
    override fun ComposeTheme(
        content: @Composable() () -> Unit
    ) {
        return KickassAnimeTheme(content = content)
    }

    override suspend fun findEpisode(
        mediaId: String,
        mediaLink: String,
        mediaType: String
    ): DownloadItem? {
        val episode =
            kickassAnimeDb.episodeEntityDao().getAnimeIdAndEpisodeNumber(mediaId) ?: return null
        val animeTitle =
            kickassAnimeDb.animeEntityDao().getAnimeName(episode.animeSlug)
                ?: return null
        val animeKey = episode.animeSlug ?: "1"
        return DownloadItem(
            animeTitle = animeTitle,
            episodeNumber = episode.episodeNumber ?: 0f,
            episodeKey = episode.episodeSlug,
            animeKey = animeKey,
            launchActivity = EpisodeActivity::class.java,
            launchBundle = EpisodeActivityArgs(
                title = animeTitle,
                episodeSlug = episode.episodeSlug,
                animeSlug = animeKey
            ).toBundle()
        )
    }

    override suspend fun triggerNotification(context: Context) {
        Log.d(TAG, "Started notification fetch")
        val newEpisodes = kickassAnimeService.getFrontPageAnimeList(1).result
        val newHash = HashUtils.hash64(newEpisodes)
        val oldHash = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            .getLong(PREF_KEY, -1)
        if (oldHash != newHash) {
            val dbEpisode = kickassAnimeDb.recentDao().getRecentPageZero()
                .first()
            val dbSet = dbEpisode.map { it.episodeSlug }.toHashSet()
            val filtered = newEpisodes.filterIndexed { _, it ->
                !dbSet.contains(it.slug)
            }
            Log.d(TAG, "New Anime ${filtered.size} found.")
            Utils.saveRecent(filtered, kickassAnimeDb, 0)
            createNotificationChannel(context)
            filtered.forEach {
                showNotification(it, context)
            }
            context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
                .edit().putLong(PREF_KEY, newHash).apply()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(anime: Recent, context: Context) {
        val notifyIntent = Intent(context, EpisodeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val animeEntity = anime.asAnimeEntity()
        val episodeEntity = anime.asEpisodeEntity()
        val episodeSlug = episodeEntity.episodeSlug
        notifyIntent.putExtras(
            bundleOf(
                "title" to anime.title,
                "episodeSlug" to episodeSlug,
                "animeSlug" to animeEntity.animeSlug
            )
        )
        val notifyPendingIntent = PendingIntent.getActivity(
            context,
            0,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val icon = IconicsDrawable(context, FontAwesome.Icon.faw_crown).apply {
            color = IconicsColor.colorInt(
                UiUtils.getThemeColor(
                    context.theme
                )
            )
        }

        val builder = NotificationCompat
            .Builder(context, context.getString(R.string.module_name))
            .setSmallIcon(IconCompat.createWithBitmap(icon.toBitmap()))
            .setContentTitle("New Anime Released ${anime.title}")
            .setContentText("Episode ${anime.episodeNumber.toString()}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(notifyPendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(HashUtils.sha256(animeEntity.animeSlug), builder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        val name = context.getString(R.string.module_name)
        val descriptionText = "Kickass anime new release feeds"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(name, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}