package com.otaku.kickassanime.page.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.otaku.kickassanime.db.models.AnimeTile

class AnimeTileAdapter<T : ViewDataBinding>(
    private val limit: Int = 0,
    @LayoutRes private val layoutId: Int,
    private val onBind: (T, AnimeTile) -> Unit
) : PagingDataAdapter<AnimeTile, AnimeTileAdapter<T>.AnimeTileViewHolder>(AnimeTileComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AnimeTileViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), layoutId, parent, false
            ),
            onBind
        )

    override fun getItemCount(): Int {
        val count = super.getItemCount()
        return if (limit in 1..count) limit else count
    }

    inner class AnimeTileViewHolder(private val binding: T, private val onBind: (T, AnimeTile) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AnimeTile) = with(binding) {
            onBind(this, item)
        }
    }

    object AnimeTileComparator : DiffUtil.ItemCallback<AnimeTile>() {
        override fun areItemsTheSame(oldItem: AnimeTile, newItem: AnimeTile) =
            oldItem.episodeSlugId == newItem.episodeSlugId

        override fun areContentsTheSame(oldItem: AnimeTile, newItem: AnimeTile) =
            oldItem == newItem
    }

    override fun onBindViewHolder(holder: AnimeTileViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }
}