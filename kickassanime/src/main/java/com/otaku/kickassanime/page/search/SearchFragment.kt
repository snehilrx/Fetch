package com.otaku.kickassanime.page.search

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.paging.PagingData
import com.maxkeppeler.sheets.input.InputSheet
import com.otaku.fetch.base.ui.setOnClick
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.databinding.FragmentAnimeListBinding
import com.otaku.kickassanime.db.models.AnimeSearchResult
import com.otaku.kickassanime.page.animepage.AnimeActivity
import com.otaku.kickassanime.page.frontpage.list.FrontPageListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow

@AndroidEntryPoint
class SearchFragment : FrontPageListFragment() {

    private val viewModel: SearchViewModel by viewModels()
    private val args: SearchFragmentArgs by navArgs()

    override fun getList(): Flow<PagingData<ITileData>> = viewModel.doSearch(args.query)

    override fun getListTag(): String {
        return args.query
    }

    override fun onItemClick(item: ITileData) {
        if (item is AnimeSearchResult) {
            startActivity(
                AnimeActivity.newInstance(
                    requireActivity(),
                    item.animeEntity
                )
            )
        }
    }

    override fun filter(binding: FragmentAnimeListBinding) {
        viewModel.getFilters()
        binding.filter.isVisible = true
        binding.filter.setOnClick {
            this.context?.let {
                InputSheet().show(it) {
                    title("Filter")
                    onPositive {

                        dismiss()
                    }
                    onNegative {
                        dismiss()
                    }
                    addAddOnComponent { }
                }
            }
        }
    }
}