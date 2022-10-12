package com.otaku.fetch.data

interface ITileData {
    fun areItemsTheSame(newItem: ITileData): Boolean
    fun areContentsTheSame(newItem: ITileData): Boolean

    val imageUrl: String
    val tags: List<String>
    val title: String
}