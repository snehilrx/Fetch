package com.otaku.kickassanime.page.frontpage

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.otaku.fetch.base.databinding.TileItemBinding
import com.otaku.fetch.base.ui.BindingFragment
import com.otaku.fetch.base.ui.setNullableAdapter
import com.otaku.kickassanime.R
import com.otaku.kickassanime.databinding.FragmentFrontPageBinding
import com.otaku.kickassanime.db.models.AnimeTile
import com.otaku.kickassanime.page.adapters.AnimeTileAdapterNoPaging
import com.otaku.kickassanime.page.adapters.CarouselAdapter
import com.otaku.kickassanime.page.adapters.HeaderAdapter
import com.otaku.kickassanime.page.frontpage.data.FrontPageViewModel
import com.otaku.kickassanime.utils.Utils.showError
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FrontPageFragment : BindingFragment<FragmentFrontPageBinding>(R.layout.fragment_front_page) {

    private lateinit var carouselAdapter: CarouselAdapter

    private val frontPageViewModel: FrontPageViewModel by activityViewModels()


    private val subbedAnimeAdapter = AnimeTileAdapterNoPaging<TileItemBinding>(
        com.otaku.fetch.base.R.layout.tile_item
    ) { binding, item ->
        binding.tileData = item
        binding.root.setOnClickListener { onItemClick(item) }
    }

    private val dubbedAnimeAdapter = AnimeTileAdapterNoPaging<TileItemBinding>(
        com.otaku.fetch.base.R.layout.tile_item
    ) { binding, item ->
        binding.tileData = item
        binding.root.setOnClickListener { onItemClick(item) }
    }

    private val adapter = ConcatAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAppbar(binding.appbar, findNavController())
        initFrontPageList()
        initFlow()
    }

    private fun initFrontPageList() {
        mergeAdapters()
        binding.container.setNullableAdapter(adapter)
        binding.container.layoutParams = binding.container.layoutParams.apply {
            height = (resources.displayMetrics.heightPixels)
        }
        val spanCount = resources.getInteger(com.otaku.fetch.base.R.integer.span_count)
        (binding.container.layoutManager as? GridLayoutManager)?.spanSizeLookup =
            object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (position) {
                        0, 1, 2, subbedAnimeAdapter.itemCount + 3 -> spanCount
                        else -> 1
                    }
                }
            }
        binding.appbar.appbarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            binding.refreshLayout.isEnabled = verticalOffset == 0
        }
        binding.refreshLayout.setOnRefreshListener {
            frontPageViewModel.refreshAllPages()
        }
    }

    private fun mergeAdapters() {
        carouselAdapter = CarouselAdapter()
        adapter.apply {
            addAdapter(HeaderAdapter(getString(R.string.more), getString(R.string.more)) {
                findNavController().navigate(FrontPageFragmentDirections.actionFrontPageFragmentToAllListFragment())
            })
            addAdapter(carouselAdapter)
            addAdapter(HeaderAdapter(getString(R.string.subbed_anime), getString(R.string.more)) {
                findNavController().navigate(FrontPageFragmentDirections.actionFrontPageFragmentToSubListFragment())
            })
            addAdapter(subbedAnimeAdapter)
            addAdapter(HeaderAdapter(getString(R.string.dubbed_anime), getString(R.string.more)) {
                findNavController().navigate(FrontPageFragmentDirections.actionFrontPageFragmentToDubListFragment())
            })
            addAdapter(dubbedAnimeAdapter)
        }
    }

    private fun initFlow() {
        frontPageViewModel.all.observe(viewLifecycleOwner) { data ->
            Log.i(TAG, "for all anime ${data.size} items loaded")
            carouselAdapter.submitList(data)
        }
        frontPageViewModel.sub.observe(viewLifecycleOwner) { data ->
            Log.i(TAG, "for sub anime ${data.size} items loaded")
            subbedAnimeAdapter.submitList(data)
        }
        frontPageViewModel.dub.observe(viewLifecycleOwner) { data ->
            Log.i(TAG, "for dub anime ${data.size} items loaded")
            dubbedAnimeAdapter.submitList(data)
        }

        frontPageViewModel.isLoading().observe(viewLifecycleOwner) {
            when (it) {
                is FrontPageViewModel.State.FAILED -> {
                    Log.e(TAG, "NETWORK CALL FAILED")
                    it.exception?.let { exception -> showError(exception.cause, requireActivity()) }
                    binding.refreshLayout.isRefreshing = false
                }

                is FrontPageViewModel.State.LOADING -> {
                    Log.i(TAG, "NETWORK CALL REQUESTED")
                    binding.refreshLayout.isRefreshing = true
                }

                is FrontPageViewModel.State.SUCCESS -> {
                    Log.i(TAG, "NETWORK CALL SUCCESS")
                    binding.refreshLayout.isRefreshing = false
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clear()
    }

    override fun onPause() {
        super.onPause()
        clear()
        binding.refreshLayout.isEnabled = false
    }

    override fun onResume() {
        super.onResume()
        binding.refreshLayout.isEnabled = true
    }

    private fun clear() {

    }

    private fun onItemClick(item: AnimeTile) {
        findNavController().navigate(
            FrontPageFragmentDirections.actionFrontPageFragmentToEpisodeFragment(
                title = item.title,
                episodeSlugId = item.episodeSlugId,
                animeSlugId = item.animeSlugId
            )
        )
    }

    companion object {
        private const val TAG = "FrontPageFragment"
    }

}


