package com.otaku.kickassanime.page.frontpage.list

import com.otaku.kickassanime.db.models.AnimeTile

class AllListFragment : FrontPageListFragment() {
    override fun getList() = frontPageListViewModel.all
    override fun getListTag() = "ALL"
    override fun onItemClick(item: AnimeTile) {
        navigate(
            AllListFragmentDirections.actionAllListFragmentToEpisodeFragment(
                title = item.title,
                episodeSlugId = item.episodeSlugId,
                animeSlugId = item.animeSlugId
            )
        )
    }
}