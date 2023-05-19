package com.otaku.kickassanime.page.frontpage.list

import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.paging.map
import androidx.recyclerview.widget.RecyclerView
import com.otaku.fetch.base.ui.BindingFragment
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.R
import com.otaku.kickassanime.databinding.FragmentAnimeListBinding
import com.otaku.kickassanime.databinding.ListControlsBinding
import com.otaku.kickassanime.page.adapters.AnimeTileAdapter
import com.otaku.kickassanime.utils.Utils.showError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class ListFragment<Binding : ViewDataBinding> :
    BindingFragment<FragmentAnimeListBinding>(R.layout.fragment_anime_list) {

    override fun onBind(binding: FragmentAnimeListBinding, savedInstanceState: Bundle?) {
        initAppbar(binding.appbar, navController = findNavController(), hideBackButton())
        val animeAdapter = AnimeTileAdapter(
            layoutId,
            onBind
        )
        initFrontPageList(animeAdapter)
        initFlow(animeAdapter)
        filter(binding)
    }

    protected open fun hideBackButton() = false

    protected abstract val layoutId: Int

    protected abstract val onBind: (Binding, ITileData) -> Unit

    private fun initFrontPageList(
        animeAdapter: AnimeTileAdapter<*>
    ) {
        binding.animeList.adapter = animeAdapter
    }

    private fun initFlow(
        animeAdapter: AnimeTileAdapter<*>
    ) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                getList().collectLatest {
                    animeAdapter.submitData(lifecycle, it.map { item -> item })
                }
            }
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
        val isRefreshing = loadState.mediator?.refresh is LoadState.Loading

        // show empty list
        listControlsBinding.emptyList.isVisible = isListEmpty
        // Show loading spinner during initial load or refresh.
        listControlsBinding.progressBar.isVisible = isRefreshing
        // Only show the list if refresh succeeds, either from the local db or the remote.
        list.isVisible = !(isListEmpty && isRefreshing)
        Log.e("debug", "$isListEmpty and $isRefreshing")
        // Show the retry state if initial load or refresh fails.
        listControlsBinding.retryBtn.isVisible =
            loadState.mediator?.refresh is LoadState.Error && adapter.itemCount == 0

    }

    protected open fun filter(binding: FragmentAnimeListBinding) {}

    abstract fun getList(): Flow<PagingData<out ITileData>>

    abstract fun getListTag(): String

}

