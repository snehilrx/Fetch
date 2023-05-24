package com.otaku.kickassanime.page.frontpage.list

import androidx.navigation.fragment.findNavController
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.db.models.AnimeTile

class TrendingListFragment : FrontPageListFragment() {

    override fun getList() = frontPageListViewModel.trending
    override fun getListTag() = "ALL"
    override fun onItemClick(item: ITileData) {
        if (item is AnimeTile)
            findNavController().navigate(
                TrendingListFragmentDirections.actionTrendingToEpisodeActivity(
                    title = item.title ?: "",
                    episodeSlug = item.episodeSlug ?: "",
                    animeSlug = item.animeSlug
                )
            )
    }
}