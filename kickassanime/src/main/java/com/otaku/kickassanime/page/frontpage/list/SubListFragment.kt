package com.otaku.kickassanime.page.frontpage.list

import androidx.navigation.fragment.findNavController
import com.otaku.kickassanime.db.models.AnimeTile

class SubListFragment : FrontPageListFragment() {
    override fun getList() = frontPageListViewModel.sub
    override fun getListTag() = "SUB"
    override fun onItemClick(item: AnimeTile) {
        findNavController().navigate(
            SubListFragmentDirections.actionSubListFragmentToEpisodeFragment(
                title = item.title,
                episodeSlugId = item.episodeSlugId,
                animeSlugId = item.animeSlugId
            )
        )
    }
}