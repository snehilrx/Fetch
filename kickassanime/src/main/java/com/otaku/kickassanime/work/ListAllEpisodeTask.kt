package com.otaku.kickassanime.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.utils.Utils
import com.otaku.kickassanime.utils.slug
import com.otaku.kickassanime.utils.slugToEpisodeLink
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ListAllEpisodeTask @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParameters: WorkerParameters,
    private val api: KickassAnimeService,
    private val db: KickassAnimeDb
) : CoroutineWorker(context, workerParameters) {

    companion object {
        fun createNewInput(animeSlug: String, language: String): Data {
            return Data.Builder()
                .putString(ANIME_SLUG, animeSlug)
                .putString(ANIME_LANGUAGE, language)
                .build()
        }

        private const val ANIME_SLUG = "anime_slug"
        private const val ANIME_LANGUAGE = "anime_language"
    }

    override suspend fun doWork(): Result {
        val animeSlug = workerParameters.inputData.getString(ANIME_SLUG)
            ?: return Result.failure()
        val animeLanguage = workerParameters.inputData.getString(ANIME_LANGUAGE)
            ?: return Result.failure()
        val episodeSlugs = ArrayList<String>()

        var response = api.getEpisodes(animeSlug, animeLanguage, 1) ?: return Result.failure()
        response.result?.mapNotNull { it.slug() }?.let {
            episodeSlugs.addAll(it)
        }
        Utils.saveEpisodePage(animeSlug, animeLanguage, response, db, 1)
        val pageCount = response.pages?.size ?: return Result.failure()
        for (i in 2..pageCount) {
            response = api.getEpisodes(animeSlug, animeLanguage, 1) ?: break
            response.result?.mapNotNull { it.slug() }?.let {
                episodeSlugs.addAll(it)
            }
            Utils.saveEpisodePage(animeSlug, animeLanguage, response, db, i)
        }
        return Result.success(
            DownloadAllEpisodeTask.createNewInput(
                episodeSlugs.map { it.slugToEpisodeLink(animeSlug) }.toTypedArray(),
                episodeSlugs.toTypedArray(),
                animeSlug
            )
        )
    }

}