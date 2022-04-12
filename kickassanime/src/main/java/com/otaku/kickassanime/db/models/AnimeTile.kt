package com.otaku.kickassanime.db.models

data class AnimeTile(
    val enTitle: String,
    val animeSlug: String,
    val animeSlugId: Int,
    val episodeSlug: String,
    val episodeSlugId: Int,
    val image: String
)
