package com.otaku.kickassanime.utils

import android.app.Activity
import android.util.Base64
import android.util.Base64InputStream
import android.util.Log
import androidx.paging.LoadState
import androidx.room.withTransaction
import com.google.android.gms.common.util.Base64Utils
import com.google.gson.Gson
import com.otaku.fetch.base.TAG
import com.otaku.fetch.base.utils.UiUtils
import com.otaku.kickassanime.api.model.AddKaa
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

    fun parseAddKaaLink(link: String, gson: Gson): AddKaa? {
        return link.toHttpUrlOrNull()?.let { url ->
            // read text from url
            val inputStream = Base64InputStream(url.toUrl().openStream(), Base64.DEFAULT)

            var json = inputStream.bufferedReader().readText()
            try {
                for (i in 1..9) {
                    json = String(Base64Utils.decode(json))
                }
            } catch (e: IllegalArgumentException) {
                // no-op
            }
            return@let gson.fromJson(json, AddKaa::class.java)
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
            } else if (cmp > 0) {
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
        val episodeEntity = List(response.result.size) { index ->
            response.result[index].asEpisodeEntity(
                animeSlug,
                language,
                response.result.getOrNull(index - 1)?.slug(),
                response.result.getOrNull(index + 1)?.slug()
            )
        }
        val pages = response.result.map {
            EpisodePageEntity(it.slug(), pageNo)
        }
        database.withTransaction {
            database.episodeEntityDao().insertAllReplace(episodeEntity)
            database.episodePageDao().insertAllReplace(pages)
        }
    }

}