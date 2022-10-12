package com.otaku.kickassanime.page.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.ListAdapter
import com.otaku.kickassanime.db.models.AnimeTile

class AnimeTileAdapterNoPaging<T: ViewDataBinding>(
    @LayoutRes private val layoutId: Int,
    private val onBind: (T, AnimeTile) -> Unit
) : ListAdapter<AnimeTile, AnimeTileAdapter.AnimeTileViewHolder<T>>(
    AnimeTileAdapter.AnimeTileComparator
) {

    sealed class Item<T>(val data: T)

    class AnimeItem(val animeTile: AnimeTile) : Item<AnimeTile>(animeTile)

    class HeaderItem(
        val title: CharSequence,
        val actionButtonText: CharSequence,
        val onClick: () -> Unit
    ) : Item<CharSequence>(title)

    class CaItem(
        val title: CharSequence,
        val actionButtonText: CharSequence,
        val onClick: () -> Unit
    ) : Item<CharSequence>(title)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AnimeTileAdapter.AnimeTileViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), layoutId, parent, false
            ),
            onBind
        )

    override fun onBindViewHolder(holder: AnimeTileAdapter.AnimeTileViewHolder<T>, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }
}
