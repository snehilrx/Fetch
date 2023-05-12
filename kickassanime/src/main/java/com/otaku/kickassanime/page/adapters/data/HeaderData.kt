package com.otaku.kickassanime.page.adapters.data

import com.otaku.fetch.data.BaseItem
import com.otaku.kickassanime.databinding.HeadingItemBinding

data class HeaderData(
    val headerId: Int,
    val initHeading: (HeadingItemBinding) -> Unit
) : BaseItem {
    override fun getItemViewType(): Int {
        return BaseItem.ITEM_TYPE_HEADER_TITLE
    }

    override fun areItemsTheSame(newItem: BaseItem): Boolean {
        return newItem is HeaderData
    }

    override fun areContentsTheSame(newItem: BaseItem): Boolean {
        return newItem is HeaderData && headerId == newItem.headerId
    }
}