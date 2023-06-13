package com.otaku.kickassanime.utils

import android.app.Activity
import android.util.Log
import androidx.paging.LoadState
import androidx.room.withTransaction
import com.google.gson.Gson
import com.otaku.fetch.base.TAG
import com.otaku.fetch.base.utils.UiUtils
import com.otaku.kickassanime.api.model.EpisodesResponse
import com.otaku.kickassanime.api.model.Maverickki
import com.otaku.kickassanime.api.model.Recent
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.db.models.entity.EpisodePageEntity
import com.otaku.kickassanime.db.models.entity.RecentEntity
import com.otaku.kickassanime.utils.Constraints.patternDateTime
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

object Utils {

    @JvmStatic
    val formatterDateTime: DateTimeFormatter = DateTimeFormatter.ofPattern(patternDateTime)

    fun parseDateTime(dateTime: String): LocalDateTime {
        return try {
            return LocalDateTime.parse(dateTime, formatterDateTime)
        } catch (e: Exception) {
            Log.e(TAG, "parseDateTime: ", e)
            LocalDateTime.now()
        }
    }

    @JvmStatic
    suspend fun saveRecent(response: List<Recent>, database: KickassAnimeDb, pageNo: Int) {
        val data = response.map {
            val animeEntity = it.asAnimeEntity()
            return@map Pair(animeEntity, it.asEpisodeEntity())
        }
        val fpe = data.map { (anime, episode) ->
            RecentEntity(
                animeSlug = anime.animeSlug,
                episodeSlug = episode.episodeSlug,
                pageNo = pageNo
            )
        }
        database.withTransaction {
            database.animeEntityDao().insertAll(data.map { it.first })
            database.episodeEntityDao().insertAll(data.map { it.second })
            database.recentDao().insertAll(fpe)
        }
    }

    fun showError(loadingError: LoadState.Error, activity: Activity) {
        showError(loadingError.error, activity)
    }

    fun showError(loadingError: Throwable?, activity: Activity) {
        UiUtils.showError(loadingError, activity)
    }

    fun parseMaverickkiLink(link: String, gson: Gson): Maverickki? {
        return link.toHttpUrlOrNull()?.let {
            // read text from url
            val jsonText = it.toUrl().readText()
            gson.fromJson(jsonText, Maverickki::class.java)
        }
    }

    fun <T> List<T>.binarySearchGreater(
        fromIndex: Int = 0,
        toIndex: Int = size,
        comparison: (T) -> Int
    ): T? {

        var low = fromIndex
        var high = toIndex - 1
        var ans = -1


        while (low <= high) {
            val mid = (low + high).ushr(1) // safe from overflows
            val midVal = get(mid)
            val cmp = comparison(midVal)

            if (cmp <= 0) {
                low = mid + 1
            } else {
                high = mid - 1
                ans = mid
            }
        }
        return if (ans in fromIndex..toIndex) {
            this[ans]
        } else {
            null
        }
    }

    suspend fun saveEpisodePage(
        animeSlug: String,
        language: String,
        response: EpisodesResponse,
        database: KickassAnimeDb,
        pageNo: Int
    ) {
        val result = response.result ?: return
        val episodeEntity = List(result.size) { index ->
            result[index].asEpisodeEntity(
                animeSlug,
                language,
                result.getOrNull(index - 1)?.slug(),
                result.getOrNull(index + 1)?.slug()
            )
        }
        val pages = result.map {
            EpisodePageEntity(it.slug(), pageNo)
        }
        database.withTransaction {
            database.episodeEntityDao().insertAll(episodeEntity)
            database.episodePageDao().insertAllReplace(pages)
        }
    }

}