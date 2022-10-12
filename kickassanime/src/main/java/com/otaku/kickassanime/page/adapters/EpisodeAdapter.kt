package com.otaku.kickassanime.page.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.otaku.kickassanime.R
import com.otaku.kickassanime.databinding.ItemEpisodeBinding
import com.otaku.kickassanime.db.models.AnimeTile
import com.otaku.kickassanime.db.models.entity.EpisodeEntity

class EpisodeAdapter(private val onItemClicked: ((Episode) -> Unit)) : ListAdapter<EpisodeAdapter.Episode, EpisodeAdapter.EpisodeViewHolder>(EpisodeComparator) {

    data class Episode(
        val title: String,
        val id: Int
    )

    class EpisodeViewHolder(
        private val layoutInflater: LayoutInflater,
        private val bindings: ItemEpisodeBinding = ItemEpisodeBinding.inflate(layoutInflater),
        private val onItemClicked: ((Episode) -> Unit)
    ) : RecyclerView.ViewHolder(bindings.root) {
        fun bind(item: Episode) {
            bindings.title.text = item.title
            bindings.root.setOnClickListener {
                onItemClicked(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        return EpisodeViewHolder(LayoutInflater.from(parent.context), ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false), onItemClicked)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object EpisodeComparator : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Episode, newItem: Episode) =
            oldItem == newItem
    }
}