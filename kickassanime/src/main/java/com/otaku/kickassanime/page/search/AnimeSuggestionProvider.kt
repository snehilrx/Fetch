package com.otaku.kickassanime.page.search

import android.content.SearchRecentSuggestionsProvider

class AnimeSuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.otaku.kickassanime.page.search.AnimeSuggestionProvider"
        const val MODE: Int = DATABASE_MODE_QUERIES
    }
}