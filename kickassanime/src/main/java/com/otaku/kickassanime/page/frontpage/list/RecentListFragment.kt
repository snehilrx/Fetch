package com.otaku.kickassanime.page.frontpage.list

import androidx.navigation.fragment.findNavController
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.db.models.AnimeTile

class RecentListFragment : FrontPageListFragment() {
    override fun getList() = frontPageListViewModel.recent
    override fun getListTag() = "Recent"
    override fun onItemClick(item: ITileData) {
        if (item is AnimeTile)
            findNavController().navigate(
                RecentListFragmentDirections.actionRecentToEpisodeFragment(
                    title = item.title,
                    episodeSlug = item.episodeSlug ?: "",
                    animeSlug = item.animeSlug
                )
            )
    }
}