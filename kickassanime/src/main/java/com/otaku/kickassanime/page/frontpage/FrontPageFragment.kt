package com.otaku.kickassanime.page.frontpage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.ActivityNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.otaku.fetch.base.TAG
import com.otaku.fetch.base.databinding.CarouselItemLayoutBinding
import com.otaku.fetch.base.databinding.TileItemBinding
import com.otaku.fetch.base.livedata.State
import com.otaku.fetch.base.ui.BindingFragment
import com.otaku.kickassanime.R
import com.otaku.kickassanime.databinding.FragmentFrontPageBinding
import com.otaku.kickassanime.db.models.AnimeTile
import com.otaku.kickassanime.page.adapters.AnimeTileAdapterNoPaging
import com.otaku.kickassanime.page.adapters.HeaderAdapter
import com.otaku.kickassanime.page.frontpage.data.FrontPageViewModel
import com.otaku.kickassanime.utils.Utils.showError
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs


@AndroidEntryPoint
class FrontPageFragment : BindingFragment<FragmentFrontPageBinding>(R.layout.fragment_front_page) {

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

    private val newAnimeAdapter = AnimeTileAdapterNoPaging<CarouselItemLayoutBinding>(
        com.otaku.fetch.base.R.layout.carousel_item_layout
    ) { binding, item ->
        binding.tileData = item
        binding.card.setOnClickListener { onItemClick(item) }
    }

    private val adapter = ConcatAdapter()

    override fun onBind(binding: FragmentFrontPageBinding, savedInstanceState: Bundle?) {
        initAppbar(
            binding.shineView,
            binding.toolbar,
            binding.collapsingToolbar,
            binding.appbarLayout,
            findNavController()
        )
        initFrontPageList()
        initFlow()
    }

    private fun initFrontPageList() {
        initCarousel()
        initList()
        binding.refreshLayout.setOnRefreshListener {
            frontPageViewModel.refreshAllPages()
        }
    }

    private fun initList() {
        if (adapter.adapters.size == 0) adapter.apply {
            addAdapter(HeaderAdapter(getString(R.string.subbed_anime), getString(R.string.more)) {
                findNavController().navigate(FrontPageFragmentDirections.actionFrontPageFragmentToSubListFragment())
            })
            addAdapter(subbedAnimeAdapter)
            addAdapter(HeaderAdapter(getString(R.string.dubbed_anime), getString(R.string.more)) {
                findNavController().navigate(FrontPageFragmentDirections.actionFrontPageFragmentToDubListFragment())
            })
            addAdapter(dubbedAnimeAdapter)
        }
        binding.container.adapter = adapter
    }

    private fun initCarousel() {
        binding.carousel.set3DItem(true)
        binding.carousel.adapter = newAnimeAdapter
        binding.appbarLayout.addOnOffsetChangedListener { appbarLayout, verticalOffset ->
            val alpha = 1 - abs(verticalOffset / appbarLayout.height.toFloat())
            binding.carouselContainer.alpha = alpha
            binding.refreshLayout.isEnabled = verticalOffset == 0
            binding.carousel.isVisible = abs(verticalOffset) < appbarLayout.totalScrollRange
        }
        binding.carouselHeading.actionButton.setOnClickListener {
            findNavController().navigate(FrontPageFragmentDirections.actionFrontPageFragmentToAllListFragment())
        }
    }

    private fun initFlow() {
        frontPageViewModel.zipped.observe(viewLifecycleOwner) { data ->
            Log.i(TAG, "for all anime ${data.all.size} items loaded")
            newAnimeAdapter.submitList(data.all)
            Log.i(TAG, "for sub anime ${data.sub.size} items loaded")
            subbedAnimeAdapter.submitList(data.sub)
            Log.i(TAG, "for dub anime ${data.dub.size} items loaded")
            dubbedAnimeAdapter.submitList(data.dub)
            showContent()
        }

        frontPageViewModel.isLoading().observe(viewLifecycleOwner) {
            when (it) {
                is State.FAILED -> {
                    Log.e(TAG, "NETWORK CALL FAILED")
                    it.exception?.let { exception -> showError(exception.cause, requireActivity()) }
                    binding.refreshLayout.isRefreshing = false
                }

                is State.LOADING -> {
                    Log.i(TAG, "NETWORK CALL REQUESTED")
                    binding.refreshLayout.isRefreshing = true
                }

                is State.SUCCESS -> {
                    Log.i(TAG, "NETWORK CALL SUCCESS")
                    binding.refreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun showContent() {
        binding.shimmerLoading.stopShimmer()
        binding.shimmerLoading.isVisible = false
        binding.refreshLayout.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.container.adapter = null
        binding.carousel.adapter = null
        (binding.container.layoutManager as? GridLayoutManager)?.spanSizeLookup = null
    }

    override fun onResume() {
        super.onResume()
        val spanCount = resources.getInteger(com.otaku.fetch.base.R.integer.span_count)
        (binding.container.layoutManager as? GridLayoutManager)?.spanSizeLookup =
            object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (position) {
                        0, subbedAnimeAdapter.itemCount + 1 -> spanCount
                        else -> 1
                    }
                }
            }
    }

    private fun onItemClick(item: AnimeTile) {
        val extras = ActivityNavigator.Extras.Builder()
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .build()
        findNavController().navigate(
            FrontPageFragmentDirections.actionFrontPageFragmentToEpisodeFragment(
                title = item.title,
                episodeSlugId = item.episodeSlugId,
                animeSlugId = item.animeSlugId
            ),
            extras
        )
    }
}


