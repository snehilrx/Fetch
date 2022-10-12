package com.otaku.kickassanime.page.search

import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.navArgs
import androidx.paging.PagingData
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.api.model.AnimeSearchResponse
import com.otaku.kickassanime.page.animepage.AnimeActivity
import com.otaku.kickassanime.page.frontpage.list.FrontPageListFragment
import com.otaku.kickassanime.page.frontpage.list.ListFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : FrontPageListFragment() {

    val viewModel: SearchViewModel by viewModels()
    private val args: SearchFragmentArgs by navArgs()

    override fun getList(): LiveData<PagingData<ITileData>> = viewModel.doSearch(args.query)

    override fun getListTag(): String {
        return args.query
    }

    override fun onItemClick(item: ITileData) {
        if (item is AnimeSearchResponse) {
            startActivity(
                AnimeActivity.newInstance(
                    requireActivity(),
                    item
                )
            )
        }
    }
}