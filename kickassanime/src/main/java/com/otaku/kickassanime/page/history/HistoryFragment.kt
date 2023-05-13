package com.otaku.kickassanime.page.history

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.paging.map
import androidx.recyclerview.widget.GridLayoutManager
import com.otaku.fetch.base.isLandscape
import com.otaku.fetch.base.ui.setOnClick
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.R
import com.otaku.kickassanime.databinding.FragmentAnimeListBinding
import com.otaku.kickassanime.databinding.ItemHistoryBinding
import com.otaku.kickassanime.db.models.AnimeHistory
import com.otaku.kickassanime.page.frontpage.list.ListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class HistoryFragment : ListFragment<ItemHistoryBinding>() {

    private val historyViewModel by viewModels<HistoryViewModel>()

    override fun onBind(binding: FragmentAnimeListBinding, savedInstanceState: Bundle?) {
        super.onBind(binding, savedInstanceState)
        (binding.animeList.layoutManager as GridLayoutManager).spanCount = if (isLandscape) 2 else 1
    }

    override val layoutId: Int
        get() = R.layout.item_history

    override val onBind: (ItemHistoryBinding, ITileData) -> Unit =
        { itemHistoryBinding: ItemHistoryBinding, iTileData: ITileData ->
            itemHistoryBinding.history = iTileData
            itemHistoryBinding.root.setOnClick { onItemClick(iTileData) }
        }

    override fun getList(): Flow<PagingData<ITileData>> =
        historyViewModel.recent.map { pagingData ->
            pagingData.map { it }
        }

    override fun getListTag() = "Recently Watched"

    override fun hideBackButton() = true


    private fun onItemClick(item: ITileData) {
        if (item is AnimeHistory) {
            val actionHistoryFragmentToEpisodeActivity =
                HistoryFragmentDirections.actionHistoryFragmentToEpisodeActivity(
                    title = item.title,
                    episodeSlug = item.episodeSlug,
                    animeSlug = item.animeSlug
                )
            findNavController().navigate(actionHistoryFragmentToEpisodeActivity)
        }
    }
}