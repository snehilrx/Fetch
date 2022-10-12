package com.otaku.kickassanime.page.frontpage.list

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.otaku.fetch.base.databinding.TileItemBinding
import com.otaku.fetch.base.ui.BindingFragment
import com.otaku.kickassanime.R
import com.otaku.kickassanime.databinding.FragmentAnimeListBinding
import com.otaku.kickassanime.databinding.ListControlsBinding
import com.otaku.kickassanime.db.models.AnimeTile
import com.otaku.kickassanime.page.adapters.AnimeTileAdapter
import com.otaku.kickassanime.page.frontpage.list.data.FrontPageListViewModel
import com.otaku.kickassanime.utils.Utils.showError
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
abstract class FrontPageListFragment :
    BindingFragment<FragmentAnimeListBinding>(R.layout.fragment_anime_list) {

    protected val frontPageListViewModel: FrontPageListViewModel by viewModels()

    override fun onBind(binding: FragmentAnimeListBinding, savedInstanceState: Bundle?) {
        initAppbar(binding.appbar, findNavController())
        val animeAdapter = AnimeTileAdapter<TileItemBinding>(
            layoutId = com.otaku.fetch.base.R.layout.tile_item
        ) { adapterBinding, item ->
            adapterBinding.tileData = item
            adapterBinding.root.setOnClickListener {
                onItemClick(item)
            }
        }
        initFrontPageList(animeAdapter)
        initFlow(animeAdapter)
    }

    private fun initFrontPageList(
        animeAdapter: AnimeTileAdapter<TileItemBinding>
    ) {
        binding.animeList.adapter = animeAdapter
    }

    private fun initFlow(
        animeAdapter: AnimeTileAdapter<TileItemBinding>
    ) {
        getList().observe(viewLifecycleOwner) { data ->
            animeAdapter.submitData(lifecycle, data)
        }
        lifecycleScope.launch {
            animeAdapter.loadStateFlow.collectLatest {
                attachLoadStateToUI(it, animeAdapter, binding.controls, binding.animeList)
            }
        }
    }

    private fun attachLoadStateToUI(
        loadState: CombinedLoadStates,
        adapter: PagingDataAdapter<*, *>,
        listControlsBinding: ListControlsBinding,
        list: RecyclerView
    ) {
        loadState.mediator?.refresh?.let {
            if (it is LoadState.Error) {
                showError(it, requireActivity())
            }
        }
        val isListEmpty =
            loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0

        // show empty list
        listControlsBinding.emptyList.isVisible = isListEmpty
        // Show loading spinner during initial load or refresh.
        listControlsBinding.progressBar.isVisible = loadState.mediator?.refresh is LoadState.Loading
        // Only show the list if refresh succeeds, either from the local db or the remote.
        list.isVisible =
            loadState.source.refresh is LoadState.NotLoading || loadState.mediator?.refresh is LoadState.NotLoading
        // Show the retry state if initial load or refresh fails.
        listControlsBinding.retryBtn.isVisible =
            loadState.mediator?.refresh is LoadState.Error && adapter.itemCount == 0

    }

    abstract fun getList(): LiveData<PagingData<AnimeTile>>

    abstract fun getListTag(): String

    abstract fun onItemClick(item: AnimeTile)
}

