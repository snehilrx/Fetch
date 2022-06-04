package com.otaku.kickassanime.page.frontpage

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.maxkeppeler.sheets.info.InfoSheet
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import com.otaku.fetch.base.databinding.CarouselItemLayoutBinding
import com.otaku.fetch.base.databinding.TileItemBinding
import com.otaku.fetch.base.ui.BindingFragment
import com.otaku.kickassanime.R
import com.otaku.kickassanime.databinding.FragmentFrontPageBinding
import com.otaku.kickassanime.page.adapters.AnimeTileAdapter
import com.otaku.kickassanime.page.adapters.HeadingViewHolder
import com.otaku.kickassanime.page.adapters.ItemListAdapter
import com.otaku.kickassanime.page.adapters.SimpleItem
import com.otaku.kickassanime.utils.Constraints.NETWORK_PAGE_SIZE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FrontPageFragment : BindingFragment<FragmentFrontPageBinding>(R.layout.fragment_front_page) {

    private val frontPageViewModel: FrontPageViewModel by viewModels()

    @Inject
    lateinit var itemListAdapter: ItemListAdapter

    private val newAnimeAdapter = AnimeTileAdapter<CarouselItemLayoutBinding>(
        NETWORK_PAGE_SIZE,
        com.otaku.fetch.base.R.layout.carousel_item_layout
    ) { binding, item ->
        binding.tileData = item
    }

    private val subbedAnimeAdapter = AnimeTileAdapter<TileItemBinding>(
        NETWORK_PAGE_SIZE,
        com.otaku.fetch.base.R.layout.tile_item
    ) { binding, item ->
        binding.tileData = item
    }

    private val dubbedAnimeAdapter = AnimeTileAdapter<TileItemBinding>(
        NETWORK_PAGE_SIZE,
        com.otaku.fetch.base.R.layout.tile_item
    ) { binding, item ->
        binding.tileData = item
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFrontPageList()
        initFlow()
    }

    private fun initFrontPageList() {
        itemListAdapter.submitList(
            listOf(
                HeadingViewHolder.Heading("New Anime"),
                SimpleItem(ItemListAdapter.VIEW_TYPE_CAROUSEL, newAnimeAdapter),
                HeadingViewHolder.Heading("Subbed Anime"),
                SimpleItem(ItemListAdapter.VIEW_TYPE_GRID, subbedAnimeAdapter),
                HeadingViewHolder.Heading("Dubbed Anime"),
                SimpleItem(ItemListAdapter.VIEW_TYPE_GRID, dubbedAnimeAdapter),
            )
        )
        binding.frontPageList.adapter = itemListAdapter
        binding.frontPageList.layoutManager = LinearLayoutManager(context)
    }

    private fun initFlow() {
        frontPageViewModel.all.observe(viewLifecycleOwner) { data ->
            newAnimeAdapter.submitData(lifecycle, data)
        }
        frontPageViewModel.subs.observe(viewLifecycleOwner) { data ->
            subbedAnimeAdapter.submitData(lifecycle, data)
        }
        frontPageViewModel.dubs.observe(viewLifecycleOwner) { data ->
            dubbedAnimeAdapter.submitData(lifecycle, data)
        }
        lifecycleScope.launch {

            fun attachLoadStateToUI(
                loadState: CombinedLoadStates,
                adapter: PagingDataAdapter<*, *>
            ) {
                loadState.mediator?.refresh?.let {
                    if (it is LoadState.Error) {
                        showError(it)
                    }
                }

                val isListEmpty =
                    loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0

                // show empty list
                binding.emptyList.isVisible = isListEmpty
                // Only show the list if refresh succeeds, either from the the local db or the remote.
                binding.frontPageList.isVisible =
                    loadState.source.refresh is LoadState.NotLoading || loadState.mediator?.refresh is LoadState.NotLoading
                // Show loading spinner during initial load or refresh.
                binding.progressBar.isVisible = loadState.mediator?.refresh is LoadState.Loading
                // Show the retry state if initial load or refresh fails.
                binding.retryBtn.isVisible =
                    loadState.mediator?.refresh is LoadState.Error && adapter.itemCount == 0
            }

            subbedAnimeAdapter.loadStateFlow.collect {
                attachLoadStateToUI(it, subbedAnimeAdapter)
            }

            dubbedAnimeAdapter.loadStateFlow.collect {
                attachLoadStateToUI(it, dubbedAnimeAdapter)
            }

            newAnimeAdapter.loadStateFlow.collect {
                attachLoadStateToUI(it, newAnimeAdapter)
            }
        }
    }

    private fun showError(loadingError: LoadState.Error) {
        val errorIcon = IconicsDrawable(requireActivity(), FontAwesome.Icon.faw_bug).apply {
            colorInt = Color.RED
            sizeDp = 24
        }
        InfoSheet().show(requireActivity()) {
            title("Oops, we got an error")
            loadingError.error.localizedMessage?.let { content(it) }
            onPositive("ok", errorIcon) {
                dismiss()
            }
        }
    }
}


