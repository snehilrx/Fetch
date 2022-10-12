package com.otaku.kickassanime.page.search

import android.content.Context
import android.content.SharedPreferences
import com.otaku.kickassanime.api.KickassAnimeService
import com.otaku.kickassanime.api.model.AnimeSearchResponse
import com.otaku.kickassanime.db.KickassAnimeDb
import com.otaku.kickassanime.utils.asAnimeEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import kotlin.collections.LinkedHashSet


const val PREF_SEARCH = "searches"

class SearchRepository @Inject constructor(
    private val kickassAnimeService: KickassAnimeService,
    private val db: KickassAnimeDb,
    @ApplicationContext private val context: Context
) {

    private val searchHistoryPref = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    private val searches = LinkedHashSet(searchHistoryPref.getStringSet(PREF_SEARCH, emptySet()))

    suspend fun search(query: String): List<AnimeSearchResponse> {
        val search = kickassAnimeService.search(query)
        db.animeEntityDao().insertAll(search.map { it.asAnimeEntity() })
        return search
    }

    fun addToSearchHistory(query: String){
        searches.remove(query)
        searches.add(query)
        searchHistoryPref.edit().putStringSet(PREF_SEARCH, searches).apply()
    }

    fun getSearchHistory(): List<String> {
        return searches.toList()
    }
}