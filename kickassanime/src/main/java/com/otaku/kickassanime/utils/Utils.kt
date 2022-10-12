package com.otaku.kickassanime.utils

import android.app.Activity
import android.graphics.Color
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.paging.LoadState
import androidx.room.withTransaction
import com.maxkeppeler.sheets.info.InfoSheet
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
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
        database.withTransaction {
            val anime = response?.anime?.map { it.asAnimeEntity() }
            val episode = response?.anime?.map { it.asEpisodeEntities() }
            val fpe = anime?.mapIndexed { index, animeEntity ->
                episode?.get(index)?.let {
                    FrontPageEpisodes(
                        animeSlugId = animeEntity.animeSlugId,
                        episodeSlugId = it.episodeSlugId,
                        pageNo = response.page
                    )
                }
            }?.filterNotNull()
            if (anime != null && episode != null) {
                database.animeEntityDao().insertAll(anime)
                database.episodeEntityDao().insertAll(episode)
            }
            if (fpe != null) {
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