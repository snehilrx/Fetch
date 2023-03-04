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
import com.otaku.kickassanime.api.model.Anime
import com.otaku.kickassanime.api.model.Maverickki
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.db.models.entity.FrontPageEpisodes
import com.otaku.kickassanime.utils.Constraints.patternDate
import com.otaku.kickassanime.utils.Constraints.patternDateTime
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

object Utils {

    @JvmStatic
    val formatterDateTime: DateTimeFormatter = DateTimeFormatter.ofPattern(patternDateTime)

    @JvmStatic
    val formatterDate: DateTimeFormatter = DateTimeFormatter.ofPattern(patternDate)


    fun parseDateTime(dateTime: String): LocalDateTime {
        return try {
            return LocalDateTime.parse(dateTime, formatterDateTime)
        } catch (e: Exception) {
            Log.e(TAG, "parseDateTime: ", e)
            LocalDateTime.now()
        }
    }

    fun parseDate(date: String): LocalDateTime {
        return try {
            LocalDate.parse(date, formatterDate).atStartOfDay()
        } catch (e: Exception) {
            Log.e(TAG, "parseDate: ", e)
            return parseDateTime(date)
        }
    }

    @JvmStatic
    suspend fun saveResponse(response: List<Anime>?, database: KickassAnimeDb, pageNo: Int) {
        val anime = response?.map { it.asAnimeEntity() }
        val episode = response?.map { it.asEpisodeEntity() }
        val fpe = anime?.mapIndexed { index, animeEntity ->
            episode?.get(index)?.let {
                FrontPageEpisodes(
                    animeSlugId = animeEntity.animeSlugId,
                    episodeSlugId = it.episodeSlugId,
                    pageNo = pageNo
                )
            }
        }?.filterNotNull()
        database.withTransaction {
            if (anime != null && episode != null && fpe != null) {
                database.animeEntityDao().insertAll(anime)
                database.episodeEntityDao().insertAll(episode)
                database.frontPageEpisodesDao().insertAll(fpe)
            }
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
                for (i in 1..9 ) {
                    json = String(Base64Utils.decode(json))
                }
            } catch (e: IllegalArgumentException) {
                // no-op
            }
            return@let gson.fromJson(json, AddKaa::class.java)
        }
    }

}