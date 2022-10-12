package com.otaku.kickassanime.page.frontpage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.otaku.fetch.base.databinding.CarouselItemLayoutBinding
import com.otaku.fetch.base.databinding.TileItemBinding
import com.otaku.kickassanime.R
import com.otaku.kickassanime.databinding.FragmentFrontPageBinding
import com.otaku.kickassanime.db.models.AnimeTile
import com.otaku.kickassanime.page.adapters.AnimeTileAdapterNoPaging
import com.otaku.kickassanime.page.adapters.CarouselAdapter
import com.otaku.kickassanime.page.adapters.HeaderAdapter
import com.otaku.kickassanime.page.frontpage.data.FrontPageViewModel
import com.otaku.kickassanime.utils.Utils.showError

class FrontPageFragment : FrontPageBaseFragment() {

    private lateinit var binding: FragmentFrontPageBinding
    private val frontPageViewModel: FrontPageViewModel by activityViewModels()


    private val newAnimeAdapter = AnimeTileAdapterNoPaging<CarouselItemLayoutBinding>(
        com.otaku.fetch.base.R.layout.carousel_item_layout
    ) { binding, item ->
        binding.tileData = item
        binding.root.setOnClickListener { onItemClick(item) }
    }

    private fun onItemClick(item: AnimeTile) {
        navigate(
            FrontPageFragmentDirections.actionFrontPageFragmentToEpisodeFragment(
                title = item.title,
                episodeSlugId = item.episodeSlugId,
                animeSlugId = item.animeSlugId
            )
        )
    }

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_front_page, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFrontPageList()
        initFlow()
    }

    private fun initFrontPageList() {
        val adapter = ConcatAdapter().apply {
            addAdapter(HeaderAdapter(getString(R.string.new_anime), getString(R.string.more)) {
                navigate(FrontPageFragmentDirections.actionFrontPageFragmentToAllListFragment())
            })
            addAdapter(CarouselAdapter(newAnimeAdapter))
            addAdapter(HeaderAdapter(getString(R.string.subbed_anime), getString(R.string.more)) {
                navigate(FrontPageFragmentDirections.actionFrontPageFragmentToSubListFragment())
            })
            addAdapter(subbedAnimeAdapter)
            addAdapter(HeaderAdapter(getString(R.string.dubbed_anime), getString(R.string.more)) {
                navigate(FrontPageFragmentDirections.actionFrontPageFragmentToDubListFragment())
            })
            addAdapter(dubbedAnimeAdapter)
        }
        binding.container.adapter = adapter
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
        binding.refreshLayout.setOnRefreshListener {
            frontPageViewModel.refreshAllPages()
        }
    }

    private fun initFlow() {
        frontPageViewModel.all.observe(viewLifecycleOwner) { data ->
            Log.i(TAG, "for all anime ${data.size} items loaded")
            newAnimeAdapter.submitList(data)
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

    override fun onPause() {
        super.onPause()
        binding.refreshLayout.isEnabled = false
        frontPageViewModel.appbarOffset = appbarController.saveAppbar() ?: 0
    }

    override fun onResume() {
        super.onResume()
        binding.refreshLayout.isEnabled = true
        appbarController.restoreAppbar(frontPageViewModel.appbarOffset)
    }

    companion object {
        private const val TAG = "FrontPageFragment"
    }

}


