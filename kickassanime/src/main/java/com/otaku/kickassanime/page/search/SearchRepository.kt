package com.otaku.kickassanime.page.search

import android.content.Context
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.api.model.AnimeSearchResponse
import com.otaku.kickassanime.api.model.Filters
import com.otaku.kickassanime.api.model.SearchRequest
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.utils.asAnimeEntity
import com.otaku.kickassanime.utils.asAnimeGenreEntity
import com.otaku.kickassanime.utils.asLanguageEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


const val PREF_SEARCH = "searches"

class SearchRepository @Inject constructor(
    private val kickassAnimeService: KickassAnimeService,
    private val db: KickassAnimeDb,
    @ApplicationContext private val context: Context
) {

    private val searchHistoryPref =
        context.getSharedPreferences("search_history", Context.MODE_PRIVATE)

    private val remoteMediator = SearchRemoteMediator(
        this,
        db
    )

    @OptIn(ExperimentalPagingApi::class)
    val pager = Pager(
        config = PagingConfig(
            pageSize = 30,
            enablePlaceholders = false
        ),
        remoteMediator = remoteMediator,
    ) {
        db.searchDao().find(remoteMediator.hashCode())
    }

    init {
        searchHistoryPref.getString(PREF_SEARCH, "")?.let { list ->
            val stringList = list.split("\n").filter { it.isNotBlank() }
            searches.addAll(stringList)
        }
    }

    fun search(
        query: String,
        genre: List<String>? = null,
        year: Int? = null,
        type: String? = null
    ) {
        remoteMediator.query = query
        remoteMediator.type = type
        remoteMediator.year = year
        remoteMediator.genre = genre
    }

    suspend fun getFilters(): Filters {
        return kickassAnimeService.getFilters()
    }

    suspend fun search(query: String): List<AnimeEntity> {
        val search = kickassAnimeService.searchHints(SearchRequest(query))
        val animeEntities = search.map { it.asAnimeEntity() }
        db.animeEntityDao().insertAll(animeEntities)
        db.animeLanguageDao().insertAll(search.flatMap { it.asLanguageEntity() })
        db.animeGenreDao().insertAll(search.flatMap { it.asAnimeGenreEntity() })
        return animeEntities
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun search(
        query: String,
        page: Int,
        genre: List<String>? = null,
        year: Int? = null,
        status: String? = null,
        type: String? = null
    ): AnimeSearchResponse {
        return kickassAnimeService.search(SearchRequest(
            query,
            page,
            Base64.encode(
                buildJsonObject {
                    year?.let { put("year", it) }
                    status?.let { put("status", status) }
                    type?.let { put("type", type) }
                    genre?.let {
                        putJsonArray("genre") {
                            genre.map { add(it) }
                        }
                    }
                }.toString().apply {
                    Log.d("JSON", this)
                }.encodeToByteArray()
            )
        )
        )
    }


    fun addToSearchHistory(query: String) {
        val trimmed = query.trim()
        searches.remove(trimmed)
        searches.add(trimmed)
        var save = ""
        searches.forEach {
            save += it + "\n"
        }
        searchHistoryPref.edit().putString(PREF_SEARCH, save).apply()
    }

    fun getSearchHistory(): List<String> {
        return searches.toList().reversed()
    }

    companion object {
        @JvmStatic
        private val searches = LinkedHashSet<String>()
    }
}
