package com.otaku.kickassanime.page.frontpage.list

import androidx.navigation.fragment.findNavController
import com.otaku.kickassanime.db.models.AnimeTile

class DubListFragment : FrontPageListFragment() {
    override fun getList() = frontPageListViewModel.dub
    override fun getListTag() = "DUB"
    override fun onItemClick(item: AnimeTile) {
        findNavController().navigate(
            DubListFragmentDirections.actionDubListFragmentToEpisodeFragment(
                title = item.title,
                episodeSlugId = item.episodeSlugId,
                animeSlugId = item.animeSlugId
            )
        )
    }
}