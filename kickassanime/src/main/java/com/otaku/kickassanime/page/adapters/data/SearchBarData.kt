package com.otaku.kickassanime.page.adapters.data

import com.otaku.fetch.data.BaseItem
import com.otaku.kickassanime.databinding.HeaderMaterialSearchBarBinding

data class SearchBarData(val initSearchBar: (HeaderMaterialSearchBarBinding) -> Unit) : BaseItem {
    override fun getItemViewType(): Int {
        return BaseItem.ITEM_TYPE_SEARCH
    }

    override fun areItemsTheSame(newItem: BaseItem): Boolean {
        return newItem is SearchBarData
    }

    override fun areContentsTheSame(newItem: BaseItem): Boolean {
        return newItem is SearchBarData
    }
}