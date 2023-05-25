package com.otaku.kickassanime.page.search

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.paging.PagingData
import com.otaku.fetch.base.livedata.GenericState
import com.otaku.fetch.base.ui.setOnClick
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.databinding.FragmentAnimeListBinding
import com.otaku.kickassanime.db.models.AnimeSearchResult
import com.otaku.kickassanime.page.animepage.AnimeActivity
import com.otaku.kickassanime.page.frontpage.list.FrontPageListFragment
import com.otaku.kickassanime.ui.theme.KickassAnimeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow

@AndroidEntryPoint
class SearchFragment : FrontPageListFragment() {

    val viewModel: SearchViewModel by viewModels()
    private val args: SearchFragmentArgs by navArgs()

    override fun onBind(binding: FragmentAnimeListBinding, savedInstanceState: Bundle?) {
        viewModel.doSearch(args.query)
        super.onBind(binding, savedInstanceState)
    }

    override fun getList(): Flow<PagingData<out ITileData>> = viewModel.searchPager

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
        viewModel.loadFilters()
        viewModel.getFilters().observe(viewLifecycleOwner) {
            when (it) {
                is GenericState.FAILED,
                is GenericState.LOADING -> {
                    binding.filter.hide()
                }

                is GenericState.SUCCESS -> {
                    binding.filter.show()
                }

                else -> {}
            }
        }
        binding.filter.setOnClick {
            activity?.let {
                FilterSheet().show(it) { view ->
                    val filters = viewModel.getFilters().value?.obj ?: return@show
                    view.setContent {
                        KickassAnimeTheme {
                            SearchFilter(
                                filters = filters,
                                onFilter = { selectedGenres, selectedYear, selectedType ->
                                    viewModel.doSearch(
                                        args.query,
                                        genre = selectedGenres,
                                        year = selectedYear?.toIntOrNull(),
                                        type = selectedType
                                    )
                                    refresh()
                                },
                                viewModel.genresState,
                                viewModel.yearsState,
                                viewModel.typesState
                            )
                        }
                    }
                }
            }
        }
    }
}