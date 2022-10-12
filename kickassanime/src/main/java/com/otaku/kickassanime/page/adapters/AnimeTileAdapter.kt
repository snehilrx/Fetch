package com.otaku.kickassanime.page.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.db.models.AnimeTile

class AnimeTileAdapter<T : ViewDataBinding>(
    @LayoutRes private val layoutId: Int,
    private val onBind: (T, ITileData) -> Unit,
) : PagingDataAdapter<ITileData, AnimeTileAdapter.AnimeTileViewHolder<T>>(AnimeTileComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AnimeTileViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), layoutId, parent, false
            ),
            onBind
        )

    class AnimeTileViewHolder<T : ViewDataBinding>(
        private val binding: T,
        private val onBind: (T, ITileData) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ITileData) = with(binding) {
            onBind(this, item)
        }
    }

    object AnimeTileComparator : DiffUtil.ItemCallback<ITileData>() {
        override fun areItemsTheSame(oldItem: ITileData, newItem: ITileData) =
            oldItem.areItemsTheSame(newItem)

        override fun areContentsTheSame(oldItem: ITileData, newItem: ITileData) =
            oldItem.areContentsTheSame(newItem)
    }

    override fun onBindViewHolder(holder: AnimeTileViewHolder<T>, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    companion object {
        const val TAG = "ANIME_TILE_ADAPTER"
    }
}