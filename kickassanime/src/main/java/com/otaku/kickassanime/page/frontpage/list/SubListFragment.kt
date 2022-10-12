package com.otaku.kickassanime.page.frontpage.list

import androidx.navigation.fragment.findNavController
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.db.models.AnimeTile

class SubListFragment : FrontPageListFragment() {
    override fun getList() = frontPageListViewModel.sub
    override fun getListTag() = "SUB"
    override fun onItemClick(item: ITileData) {
        if (item is AnimeTile)
            findNavController().navigate(
                SubListFragmentDirections.actionSubListFragmentToEpisodeFragment(
                    title = item.title,
                    episodeSlugId = item.episodeSlugId,
                    animeSlugId = item.animeSlugId
                )
            )
    }
}