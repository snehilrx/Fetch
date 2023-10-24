package com.otaku.fetch.data

interface ITileData : BaseItem {
    fun areItemsTheSame(newItem: ITileData): Boolean
    fun areContentsTheSame(newItem: ITileData): Boolean

    val imageUrl: String
    val tags: List<String>
    val title: String?

    override fun areItemsTheSame(newItem: BaseItem): Boolean {
        return newItem is ITileData && areItemsTheSame(newItem)
    }

    override fun areContentsTheSame(newItem: BaseItem): Boolean {
        return newItem is ITileData && areContentsTheSame(newItem)
    }

    override fun getItemViewType(): Int {
        return BaseItem.ITEM_TYPE_LIST
    }

}