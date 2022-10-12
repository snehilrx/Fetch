package com.otaku.kickassanime.page.frontpage.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.otaku.fetch.base.R
import com.otaku.fetch.base.databinding.TileItemBinding
import com.otaku.kickassanime.databinding.FragmentAnimeListBinding
import com.otaku.kickassanime.databinding.ListControlsBinding
import com.otaku.kickassanime.db.models.AnimeTile
import com.otaku.kickassanime.page.adapters.AnimeTileAdapter
import com.otaku.kickassanime.page.frontpage.FrontPageBaseFragment
import com.otaku.kickassanime.page.frontpage.list.data.FrontPageListViewModel
import com.otaku.kickassanime.utils.Utils.showError
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


abstract class FrontPageListFragment : FrontPageBaseFragment() {

    protected val frontPageListViewModel: FrontPageListViewModel by viewModels()

    private lateinit var binding: FragmentAnimeListBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val animeAdapter = AnimeTileAdapter<TileItemBinding>(
            layoutId = R.layout.tile_item
        ) { binding, item ->
            binding.tileData = item
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
        initFrontPageList(animeAdapter)
        initFlow(animeAdapter)
    }

    abstract fun onItemClick(item: AnimeTile)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(savedInstanceState == null)appbarController.restoreAppbar(0)
        binding = DataBindingUtil.inflate(
            inflater,
            com.otaku.kickassanime.R.layout.fragment_anime_list,
            container,
            false
        )
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        appbarController.saveAppbar()?.let { outState.putInt(getListTag(), it) }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        savedInstanceState?.getInt(getListTag())?.let { appbarController.restoreAppbar(it) }
        super.onViewStateRestored(savedInstanceState)
    }

    private fun initFrontPageList(animeAdapter: AnimeTileAdapter<TileItemBinding>) {
        binding.animeList.adapter = animeAdapter
    }

    private fun initFlow(animeAdapter: AnimeTileAdapter<TileItemBinding>) {
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
}

