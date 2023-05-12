package com.otaku.kickassanime.page.adapters.data

import com.carouselrecyclerview.CarouselRecyclerview
import com.lapism.search.widget.MaterialSearchBar
import com.otaku.fetch.data.BaseItem
import com.otaku.kickassanime.databinding.HeadingItemBinding

data class CarouselData(
    val initCarousel: (CarouselRecyclerview) -> Unit
) : BaseItem {
    override fun getItemViewType(): Int {
        return BaseItem.ITEM_TYPE_HEADER_CAROUSEL
    }

    override fun areItemsTheSame(newItem: BaseItem): Boolean {
        return newItem is CarouselData
    }

    override fun areContentsTheSame(newItem: BaseItem): Boolean {
        return newItem is CarouselData
    }
}