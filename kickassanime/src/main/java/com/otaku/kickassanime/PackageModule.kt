package com.otaku.kickassanime

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.bundleOf
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.NavHostFragment
import com.mikepenz.iconics.IconicsColor
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.color
import com.otaku.fetch.AppModule
import com.otaku.fetch.base.utils.UiUtils
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.api.model.Anime
import com.otaku.kickassanime.api.model.AnimeListFrontPageResponse
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.page.MainFragment
import com.otaku.kickassanime.page.episodepage.EpisodeActivity
import com.otaku.kickassanime.utils.HashUtils
import com.otaku.kickassanime.utils.Utils
import com.otaku.kickassanime.utils.asAnimeEntity
import com.otaku.kickassanime.utils.asEpisodeEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject

const val PREF_KEY = "KickassAnime/lastUpdateHash"

@UnstableApi
class PackageModule @Inject constructor(
    val kickassAnimeService: KickassAnimeService,
    val kickassAnimeDb: KickassAnimeDb
) : AppModule {

    private var mainFragment: MainFragment = MainFragment()

    override val name: String
        get() = "Kickass Anime"

    override fun onSearch(query: String) {
        TODO("Not yet implemented")
    }

    override fun initialize(query: String?, link: String) {
    }

    override fun getMainFragment(): NavHostFragment {
        return mainFragment
    }

    override suspend fun triggerNotification(context: Context) {
        val newEpisodes = kickassAnimeService.getFrontPageAnimeList(1)
        val newHash = HashUtils.hash64(newEpisodes.anime)
        val oldHash = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            .getLong(PREF_KEY, -1)
        if(oldHash != newHash){
            val dbEpisode =
                kickassAnimeDb.frontPageEpisodesDao().getFirstFrontPageEpisodes()
                    .first()
            val dbSet = dbEpisode.map { it.episodeSlug }.toHashSet()
            val filtered = AnimeListFrontPageResponse(newEpisodes.anime.filterIndexed { index, it ->
                !dbSet.contains(it.slug) && kickassAnimeDb.favouritesDao()
                    .isFavourite(it.asAnimeEntity().animeSlugId)
            }, newEpisodes.page)
            Utils.saveResponse(filtered, kickassAnimeDb)
            createNotificationChannel(context)
            filtered.anime.forEach {
                showNotification(it, context)
            }
            context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
                .edit().putLong(PREF_KEY, newHash).apply()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(anime: Anime, context: Context) {
        val notifyIntent = Intent(context, EpisodeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val episodeSlugId = anime.asEpisodeEntity().episodeSlugId
        notifyIntent.putExtras(
            bundleOf(
                "title" to anime.name,
                "episodeSlugId" to episodeSlugId,
                "animeSlugId" to anime.asAnimeEntity().animeSlugId
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
            .setContentTitle("New Anime Released ${anime.name}")
            .setContentText("Episode ${anime.episode} ${anime.type}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(notifyPendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(episodeSlugId, builder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

}