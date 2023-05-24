package com.otaku.kickassanime.page.frontpage.list

import androidx.navigation.fragment.findNavController
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.db.models.AnimeTile

class PopularFragment : FrontPageListFragment() {
    override fun getList() = frontPageListViewModel.popular
    override fun getListTag() = "Popular"
    override fun onItemClick(item: ITileData) {
        if (item is AnimeTile)
            findNavController().navigate(
                PopularFragmentDirections.actionPopularToEpisodeActivity(
                    title = item.title ?: "",
                    episodeSlug = item.episodeSlug ?: "",
                    animeSlug = item.animeSlug
                )
            )
    }
}