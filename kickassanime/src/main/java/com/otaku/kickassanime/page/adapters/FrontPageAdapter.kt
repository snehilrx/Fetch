package com.otaku.kickassanime.page.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.otaku.fetch.base.databinding.TileItemBinding
import com.otaku.fetch.data.BaseItem
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.databinding.HeaderFrontPageBinding
import com.otaku.kickassanime.databinding.HeaderMaterialSearchBarBinding
import com.otaku.kickassanime.databinding.HeadingItemBinding
import com.otaku.kickassanime.page.adapters.data.CarouselData
import com.otaku.kickassanime.page.adapters.data.HeaderData
import com.otaku.kickassanime.page.adapters.data.SearchBarData

class BaseItemDiff : DiffUtil.ItemCallback<BaseItem>() {
    override fun areItemsTheSame(oldItem: BaseItem, newItem: BaseItem) =
        oldItem.areItemsTheSame(newItem)

    override fun areContentsTheSame(oldItem: BaseItem, newItem: BaseItem) =
        oldItem.areContentsTheSame(newItem)

}

class FrontPageAdapter(
    private val onBind: (TileItemBinding, ITileData) -> Unit
) : ListAdapter<BaseItem, ViewHolder>(BaseItemDiff()) {

    class ListViewHolder(
        parent: ViewGroup,
        onBind: (TileItemBinding, ITileData) -> Unit,
        binding: TileItemBinding = TileItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ) : AnimeTileAdapter.AnimeTileViewHolder<TileItemBinding>(binding, onBind)

    class HeaderCarouselViewHolder(
        parent: ViewGroup,
        private val binding: HeaderFrontPageBinding = HeaderFrontPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ) : ViewHolder(binding.root) {

        fun bind(carouselData: CarouselData) {
            carouselData.initCarousel(binding.carousel)
        }
    }

    class HeadingTitleViewHolder(
        parent: ViewGroup,
        private val headingItemBinding: HeadingItemBinding = HeadingItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ) : ViewHolder(headingItemBinding.root) {

        fun bind(headerData: HeaderData) {
            headerData.initHeading(headingItemBinding)
        }

    }

    class SearchBarViewHolder(
        parent: ViewGroup,
        private val headingItemBinding: HeaderMaterialSearchBarBinding = HeaderMaterialSearchBarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ) : ViewHolder(headingItemBinding.root) {

        fun bind(searchBarData: SearchBarData) {
            searchBarData.initSearchBar(headingItemBinding)
        }

    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).getItemViewType()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            BaseItem.ITEM_TYPE_HEADER_TITLE -> {
                HeadingTitleViewHolder(parent)
            }
            BaseItem.ITEM_TYPE_LIST -> {
                ListViewHolder(parent, onBind)
            }
            BaseItem.ITEM_TYPE_HEADER_CAROUSEL -> {
                HeaderCarouselViewHolder(parent)
            }
            BaseItem.ITEM_TYPE_SEARCH -> {
                SearchBarViewHolder(parent)
            }
            else -> throw Exception("Illegal Item")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(holder) {
            is HeaderCarouselViewHolder -> {
                holder.bind(getItem(position) as CarouselData)
            }
            is HeadingTitleViewHolder -> {
                holder.bind(getItem(position) as HeaderData)
            }
            is ListViewHolder -> {
                holder.bind(getItem(position) as ITileData)
            }
            is SearchBarViewHolder -> {
                holder.bind(getItem(position) as SearchBarData)
            }
        }
    }

}