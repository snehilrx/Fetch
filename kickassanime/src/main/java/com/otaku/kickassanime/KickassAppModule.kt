package com.otaku.kickassanime

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.bundleOf
import androidx.media3.common.util.UnstableApi
import com.mikepenz.iconics.Iconics
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
import com.otaku.kickassanime.db.models.AnimeTile
import com.otaku.kickassanime.page.episodepage.EpisodeActivity
import com.otaku.kickassanime.page.episodepage.EpisodeActivityArgs
import com.otaku.kickassanime.ui.theme.KickassAnimeTheme
import com.otaku.kickassanime.utils.HashUtils
import com.otaku.kickassanime.utils.Utils
import com.otaku.kickassanime.utils.asAnimeEntity
import com.otaku.kickassanime.utils.asEpisodeEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

const val PREF_KEY = "KickassAnime/lastUpdateHash"

@UnstableApi
class KickassAppModule @Inject constructor(
    val kickassAnimeService: KickassAnimeService,
    private val kickassAnimeDb: KickassAnimeDb,
    @ApplicationContext context: Context,
) : AppModule {

    init {
        Iconics.init(context)
        Iconics.registerFont(FontAwesome)
    }

    override val name: String
        get() = "Kickass Anime"
    override val notificationDeeplink: String
        get() = "http://kaa.sm/recent"

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

    override suspend fun triggerNotification(
        context: Context,
        defaultIntent: TaskStackBuilder,
    ) {
        Log.d(TAG, "Started notification fetch")
        createNotificationChannel(context)
        val newHash = showNotification(
            kickassAnimeService.getFrontPageAnimeList(1).result,
            kickassAnimeDb.recentDao().getRecentPageZero().first(),
            context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
                .getLong(PREF_KEY, -1),
            showNotification = { recent ->
                showNotification(recent, context, defaultIntent)
            },
            showGroupedNotification = { all ->
                basicNotification(
                    context,
                    defaultIntent.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT,
                    ),
                    0,
                    context.getString(R.string.group_notification_title),
                    String.format(
                        context.getString(R.string.new_kickassanime_notification_body),
                        all.take(2).map {
                            it.title + "\n"
                        }
                    )
                )
            }
        )
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            .edit().putLong(PREF_KEY, newHash).apply()
    }

    suspend fun showNotification(
        newEpisodes: List<Recent>,
        oldEpisodes: List<AnimeTile>,
        oldHash: Long,
        showNotification: (firstEpisodeOnFirstPage: Recent) -> Unit,
        showGroupedNotification: (allEpisodesOnFirstPage: List<Recent>) -> Unit,
        saveNewAnimes: suspend (List<Recent>) -> Unit = {
            Utils.saveRecent(it, kickassAnimeDb, 0)
        }
    ): Long {
        val newHash = HashUtils.hash64(newEpisodes)
        if (oldHash != newHash) {
            val dbSet = oldEpisodes.map { it.episodeSlug }.toHashSet()
            val filtered = newEpisodes.filterIndexed { _, it ->
                !dbSet.contains(it.getEpisodeSlug())
            }
            saveNewAnimes(filtered)
            Log.d(TAG, "New Anime ${filtered.size} found.")
            filtered.take(1).forEach {
                showNotification(it)
            }
            showGroupedNotification(filtered)
        }
        return newHash
    }

    private fun showNotification(anime: Recent, context: Context, defaultIntent: TaskStackBuilder) {
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
        val notifyPendingIntent = defaultIntent.addNextIntent(notifyIntent)
            .getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT,
            )
        val title = context.getString(R.string.notification_title).format(anime.title)
        val body = context.getString(R.string.episode).format(anime.episodeNumber?.toInt())

        basicNotification(
            context,
            notifyPendingIntent,
            HashUtils.sha256(animeEntity.animeSlug),
            title,
            body
        )
    }

    private fun basicNotification(
        context: Context,
        notifyPendingIntent: PendingIntent?,
        id: Int,
        title: String,
        body: String
    ) {
        val icon = IconicsDrawable(context, FontAwesome.Icon.faw_crown).apply {
            color = IconicsColor.colorInt(
                UiUtils.getThemeColor(
                    context.theme
                )
            )
        }

        val builder = NotificationCompat.Builder(context, context.getString(R.string.module_name))
            .setSmallIcon(IconCompat.createWithBitmap(icon.toBitmap()))
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(notifyPendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(id, builder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        val name = context.getString(R.string.module_name)
        val descriptionText = context.getString(R.string.notification_channel_description)
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