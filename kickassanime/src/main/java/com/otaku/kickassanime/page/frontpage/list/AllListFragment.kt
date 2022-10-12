package com.otaku.kickassanime.page.frontpage.list

import androidx.navigation.fragment.findNavController
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.db.models.AnimeTile

class AllListFragment : FrontPageListFragment() {

    override fun getList() = frontPageListViewModel.all
    override fun getListTag() = "ALL"
    override fun onItemClick(item: ITileData) {
        if (item is AnimeTile)
            findNavController().navigate(
                AllListFragmentDirections.actionAllListFragmentToEpisodeFragment(
                    title = item.title,
                    episodeSlugId = item.episodeSlugId,
                    animeSlugId = item.animeSlugId
                )
            )
    }
}