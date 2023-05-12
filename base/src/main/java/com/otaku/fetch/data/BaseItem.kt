package com.otaku.fetch.data

interface BaseItem {
    companion object {
        const val ITEM_TYPE_HEADER_CAROUSEL = 0
        const val ITEM_TYPE_HEADER_TITLE = 1
        const val ITEM_TYPE_LIST = 2
        const val ITEM_TYPE_SEARCH = 3
    }

    fun getItemViewType() : Int
    fun areItemsTheSame(newItem: BaseItem): Boolean
    fun areContentsTheSame(newItem: BaseItem): Boolean
}