package com.otaku.kickassanime.utils

import android.app.Activity
import android.util.Log
import androidx.paging.LoadState
import androidx.room.withTransaction
import com.otaku.fetch.base.TAG
import com.otaku.fetch.base.utils.UiUtils
import com.otaku.kickassanime.api.model.AnimeListFrontPageResponse
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.db.models.entity.FrontPageEpisodes
import com.otaku.kickassanime.utils.Constraints.patternDate
import com.otaku.kickassanime.utils.Constraints.patternDateTime
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
    suspend fun saveResponse(response: AnimeListFrontPageResponse?, database: KickassAnimeDb) {
        val anime = response?.anime?.map { it.asAnimeEntity() }
        val episode = response?.anime?.map { it.asEpisodeEntity() }
        val fpe = anime?.mapIndexed { index, animeEntity ->
            episode?.get(index)?.let {
                FrontPageEpisodes(
                    animeSlugId = animeEntity.animeSlugId,
                    episodeSlugId = it.episodeSlugId,
                    pageNo = response.page
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

}