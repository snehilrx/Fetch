package com.otaku.kickassanime.page.frontpage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.ActivityNavigator
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.carouselrecyclerview.CarouselRecyclerview
import com.google.android.material.appbar.AppBarLayout
import com.lapism.search.widget.MaterialSearchView
import com.mikepenz.iconics.view.IconicsTextView
import com.otaku.fetch.base.TAG
import com.otaku.fetch.base.databinding.CarouselItemLayoutBinding
import com.otaku.fetch.base.livedata.State
import com.otaku.fetch.base.ui.BindingFragment
import com.otaku.fetch.base.ui.searchInterface
import com.otaku.fetch.base.ui.setOnClick
import com.otaku.fetch.data.BaseItem
import com.otaku.fetch.data.BaseItem.Companion.ITEM_TYPE_HEADER_CAROUSEL
import com.otaku.fetch.data.BaseItem.Companion.ITEM_TYPE_HEADER_TITLE
import com.otaku.fetch.data.BaseItem.Companion.ITEM_TYPE_LIST
import com.otaku.fetch.data.BaseItem.Companion.ITEM_TYPE_SEARCH
import com.otaku.fetch.data.ITileData
import com.otaku.kickassanime.R
import com.otaku.kickassanime.databinding.FragmentFrontPageBinding
import com.otaku.kickassanime.databinding.HeaderMaterialSearchBarBinding
import com.otaku.kickassanime.databinding.ItemSearchHintBinding
import com.otaku.kickassanime.db.models.AnimeSearchResult
import com.otaku.kickassanime.db.models.AnimeTile
import com.otaku.kickassanime.page.adapters.AnimeTileAdapterNoPaging
import com.otaku.kickassanime.page.adapters.FrontPageAdapter
import com.otaku.kickassanime.page.adapters.data.CarouselData
import com.otaku.kickassanime.page.adapters.data.HeaderData
import com.otaku.kickassanime.page.adapters.data.SearchBarData
import com.otaku.kickassanime.page.animepage.AnimeActivity
import com.otaku.kickassanime.page.frontpage.data.FrontPageViewModel
import com.otaku.kickassanime.utils.Utils.showError
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FrontPageFragment : BindingFragment<FragmentFrontPageBinding>(R.layout.fragment_front_page) {

    private val frontPageViewModel: FrontPageViewModel by activityViewModels()

    private val disableRefreshOnOffset =
        AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            binding.refreshLayout.isEnabled = verticalOffset == 0
        }

    private val frontPageAdapter = FrontPageAdapter { binding, item ->
        binding.tileData = item
        binding.root.setOnClick { onItemClick(item) }
    }

    private val carouselAdapter = AnimeTileAdapterNoPaging<CarouselItemLayoutBinding>(
        com.otaku.fetch.base.R.layout.carousel_item_layout
    ) { binding, item ->
        binding.tileData = item
        binding.card.setOnClickListener { onItemClick(item) }
    }

    private val searchHintAdapter = AnimeTileAdapterNoPaging(
        R.layout.item_search_hint,
    ) { binding: ItemSearchHintBinding, iTileData: ITileData ->
        if (iTileData is AnimeSearchResult) {
            binding.anime = iTileData
            binding.root.setOnClick {
                startActivity(
                    AnimeActivity.newInstance(
                        requireActivity(),
                        iTileData.animeEntity
                    )
                )
            }
        }
    }

    private val historyAdapter = StringAdapter {
        val data = it.second
        if (data is String) {
            openSearchResultFragment(data)
        }
    }


    override fun onBind(binding: FragmentFrontPageBinding, savedInstanceState: Bundle?) {
        initAppbar(
            binding.shineView,
            binding.toolbar,
            binding.collapsingToolbar,
            binding.appbarLayout,
            findNavController()
        )
        initFrontPageList()
        initSearchBar()
        initFlow()
    }

    inner class StringAdapter(private val onClick: (Pair<String, Any>) -> Unit) :
        ListAdapter<Pair<String, Any>, RecyclerView.ViewHolder>(
            object : DiffUtil.ItemCallback<Pair<String, Any>>() {
                override fun areItemsTheSame(
                    oldItem: Pair<String, Any>,
                    newItem: Pair<String, Any>
                ): Boolean =
                    oldItem.first == newItem.first

                override fun areContentsTheSame(
                    oldItem: Pair<String, Any>,
                    newItem: Pair<String, Any>
                ): Boolean =
                    oldItem.first == newItem.first
            }
        ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.simple_item_iconics,
                    parent,
                    false
                )
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val textView = holder.itemView as IconicsTextView
            val item = getItem(position)
            val text = "{faw-history}   ${item.first}"
            textView.text = text

            textView.setOnClick {
                onClick(item)
            }
        }
    }

    private fun initSearchBar(binding: HeaderMaterialSearchBarBinding) {
        binding.searchBar.apply {
            setHint(getString(R.string.search_anime))
            setOnClick {
                searchInterface?.openSearch()
                historyAdapter.submitList(
                    frontPageViewModel.getSearchHistory().map { it to it })
            }
        }
    }

    private fun initSearchBar() {
        searchInterface?.setSuggestions(
            ConcatAdapter(
                searchHintAdapter,
                historyAdapter
            )
        )
        frontPageViewModel.getSearchSuggestions().observe(this) { list ->
            if (list.isEmpty()) {
                searchInterface?.showError(Exception("No Results"))
            } else {
                searchHintAdapter.submitList(list)
            }
        }

        frontPageViewModel.searchIsLoading().observe(this) {
            when (it) {
                is State.FAILED -> {
                    searchInterface?.showError(Exception("Something went wrong"))
                }
                is State.LOADING -> {
                    searchInterface?.showLoading()
                }
                is State.SUCCESS -> {
                    searchInterface?.showContent()
                }
            }
        }
        searchInterface?.setQueryListener(
            object : MaterialSearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: CharSequence) {
                    if (newText.length > 3) {
                        frontPageViewModel.querySearchSuggestions(newText)
                    } else {
                        frontPageViewModel.querySearchSuggestions("")
                    }
                }

                override fun onQueryTextSubmit(query: CharSequence) {
                    historyAdapter.submitList(
                        frontPageViewModel.getSearchHistory().map { it to it })
                    openSearchResultFragment(query.toString())
                    searchHintAdapter.submitList(emptyList())
                }
            }
        )

    }

    private fun openSearchResultFragment(query: String) {
        searchInterface?.closeSearch()
        val actionFrontPageFragmentToSearchFragment =
            FrontPageFragmentDirections.actionFrontPageFragmentToSearchFragment(query)
        Navigation.findNavController(requireView())
            .navigate(actionFrontPageFragmentToSearchFragment)
    }

    private fun initFrontPageList() {
        binding.container.adapter = frontPageAdapter
        binding.appbarLayout.addOnOffsetChangedListener(disableRefreshOnOffset)
        binding.refreshLayout.setOnRefreshListener {
            frontPageViewModel.refreshAllPages()
        }
    }

    private fun initCarousel(carousel: CarouselRecyclerview) {
        if (carousel.adapter == null) {
            carousel.set3DItem(true)
            carouselAdapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            carousel.adapter = carouselAdapter
        }
    }

    private fun initFlow() {
        val preItems = ArrayList<BaseItem>()
        preItems.add(0, SearchBarData(this::initSearchBar))
        preItems.add(1, HeaderData(0) {
            it.title = getString(R.string.recent)
            it.actionButtonText = getString(R.string.more)
            it.actionButton.setOnClick {
                findNavController().navigate(FrontPageFragmentDirections.actionFrontPageFragmentToRecent())
            }
        })
        preItems.add(2, CarouselData(this::initCarousel))
        preItems.add(3, HeaderData(1) {
            it.title = getString(R.string.sub)
            it.actionButtonText = getString(R.string.more)
            it.actionButton.setOnClick {
                findNavController()
                    .navigate(FrontPageFragmentDirections.actionFrontPageFragmentToTrending())
            }
        })
        frontPageViewModel.zipped().observe(viewLifecycleOwner) { data ->
            carouselAdapter.submitList(data.first)
            val frontPageViews = ArrayList<BaseItem>(preItems)
            frontPageViews.addAll(data.second)
            frontPageViews.add(HeaderData(0) {
                it.title = getString(R.string.dub)
                it.actionButtonText = getString(R.string.more)
                it.actionButton.setOnClick {
                    findNavController()
                        .navigate(FrontPageFragmentDirections.actionFrontPageFragmentToPopular())
                }
            })
            frontPageViews.addAll(data.third)
            frontPageAdapter.submitList(frontPageViews)
            showContent()
        }

        frontPageViewModel.isLoading().observe(viewLifecycleOwner) {
            when (it) {
                is State.FAILED -> {
                    Log.e(TAG, "NETWORK CALL FAILED")
                    it.exception?.let { exception -> showError(exception, requireActivity()) }
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
        binding.refreshLayout.setOnRefreshListener(null)
        binding.appbarLayout.removeOnOffsetChangedListener(disableRefreshOnOffset)
        (binding.container.layoutManager as? GridLayoutManager)?.spanSizeLookup = null
    }

    override fun onResume() {
        super.onResume()
        val spanCount = resources.getInteger(com.otaku.fetch.base.R.integer.span_count)
        (binding.container.layoutManager as? GridLayoutManager)?.spanSizeLookup =
            object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (frontPageAdapter.getItemViewType(position)) {
                        ITEM_TYPE_HEADER_CAROUSEL -> spanCount
                        ITEM_TYPE_HEADER_TITLE -> spanCount
                        ITEM_TYPE_SEARCH -> spanCount
                        ITEM_TYPE_LIST -> 1
                        else -> spanCount
                    }
                }
            }
    }

    private fun onItemClick(item: ITileData) {
        val extras = ActivityNavigator.Extras.Builder()
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .build()
        when (item) {
            is AnimeTile -> {
                findNavController().navigate(
                    FrontPageFragmentDirections.actionFrontPageFragmentToEpisodeActivity(
                        title = item.title,
                        episodeSlug = item.episodeSlug ?: "",
                        animeSlug = item.animeSlug
                    ),
                    extras
                )
            }
        }
    }
}


