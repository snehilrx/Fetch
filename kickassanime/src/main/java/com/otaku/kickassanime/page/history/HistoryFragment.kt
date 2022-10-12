package com.otaku.kickassanime.page.history

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.paging.map
import androidx.recyclerview.widget.GridLayoutManager
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.R
import com.otaku.kickassanime.databinding.FragmentAnimeListBinding
import com.otaku.kickassanime.databinding.ItemHistoryBinding
import com.otaku.kickassanime.db.models.AnimeHistory
import com.otaku.kickassanime.page.frontpage.list.ListFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : ListFragment<ItemHistoryBinding>() {

    private val historyViewModel by viewModels<HistoryViewModel>()

    override fun onBind(binding: FragmentAnimeListBinding, savedInstanceState: Bundle?) {
        super.onBind(binding, savedInstanceState)
        (binding.animeList.layoutManager as GridLayoutManager).spanCount = 1
    }

    override val layoutId: Int
        get() = R.layout.item_history

    override val onBind: (ItemHistoryBinding, ITileData) -> Unit = { itemHistoryBinding: ItemHistoryBinding, iTileData: ITileData ->
        itemHistoryBinding.history = iTileData
        itemHistoryBinding.root.setOnClickListener { onItemClick(iTileData) }
    }

    override fun getList(): LiveData<PagingData<ITileData>> = historyViewModel.recents.map {
        pagingData -> pagingData.map { it as ITileData }
    }

    override fun getListTag() = "Recently Watched"


    private fun onItemClick(item: ITileData) {
        if(item is AnimeHistory){
            val actionHistoryFragmentToEpisodeActivity =
                HistoryFragmentDirections.actionHistoryFragmentToEpisodeActivity(
                    title = item.title,
                    episodeSlugId = item.episodeSlugId,
                    animeSlugId = item.animeSlugId
                )
            findNavController().navigate(actionHistoryFragmentToEpisodeActivity)
        }
    }
}