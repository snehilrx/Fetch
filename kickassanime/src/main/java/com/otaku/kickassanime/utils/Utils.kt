package com.otaku.kickassanime.utils

import android.app.Activity
import android.graphics.Color
import android.util.Log
import androidx.paging.LoadState
import androidx.room.withTransaction
import com.maxkeppeler.sheets.info.InfoSheet
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
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

    fun parseDateTime(dateTime: String): LocalDateTime = LocalDateTime.parse(dateTime, formatterDateTime)

    fun parseDate(dateTime: String): LocalDateTime = LocalDate.parse(dateTime, formatterDate).atTime(0, 0)

    @JvmStatic
    suspend fun saveResponse(response: AnimeListFrontPageResponse?, database: KickassAnimeDb) {
        database.withTransaction {
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

    fun showError(loadingError: Throwable?, activity: Activity, onPositive: () -> Unit = {}) {
        val errorIcon = IconicsDrawable(activity, FontAwesome.Icon.faw_bug).apply {
            colorInt = Color.RED
            sizeDp = 24
        }
        Log.e(TAG, "showError: ", loadingError)
        InfoSheet().show(activity) {
            title("Oops, we got an error")
            loadingError?.localizedMessage?.let { content(it) }
            onPositive("ok", errorIcon) {
                dismiss()
                onPositive()
            }
        }
    }

    private const val TAG = "Utils"
}