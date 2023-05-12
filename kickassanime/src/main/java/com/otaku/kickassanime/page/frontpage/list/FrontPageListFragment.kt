package com.otaku.kickassanime.page.frontpage.list

import androidx.fragment.app.viewModels
import com.otaku.fetch.base.databinding.TileItemBinding
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.page.frontpage.list.data.FrontPageListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class FrontPageListFragment : ListFragment<TileItemBinding>() {

    protected val frontPageListViewModel: FrontPageListViewModel by viewModels()

    override val layoutId: Int
        get() = com.otaku.fetch.base.R.layout.tile_item
    override val onBind: (TileItemBinding, ITileData) -> Unit
        get() = { adapterBinding, item ->
            adapterBinding.tileData = item
            adapterBinding.root.setOnClickListener {
                this.onItemClick(item)
            }
        }

    abstract fun onItemClick(item: ITileData)
}