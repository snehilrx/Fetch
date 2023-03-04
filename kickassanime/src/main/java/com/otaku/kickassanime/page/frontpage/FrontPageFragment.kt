package com.otaku.kickassanime.page.frontpage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.ActivityNavigator
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.carouselrecyclerview.CarouselRecyclerview
import com.lapism.search.widget.MaterialSearchBar
import com.lapism.search.widget.MaterialSearchView
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
import com.otaku.kickassanime.R
import com.otaku.kickassanime.api.model.AnimeSearchResponse
import com.otaku.kickassanime.databinding.FragmentFrontPageBinding
import com.otaku.kickassanime.databinding.HeaderMaterialSearchBarBinding
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

    private val frontPageAdapter = FrontPageAdapter { binding, item ->
        binding.tileData = item
        binding.root.setOnClick { onItemClick(item as AnimeTile) }
    }

    private val newAnimeAdapter = AnimeTileAdapterNoPaging<CarouselItemLayoutBinding>(
        com.otaku.fetch.base.R.layout.carousel_item_layout
    ) { binding, item ->
        binding.tileData = item
        binding.card.setOnClickListener { onItemClick(item as AnimeTile) }
    }

    private val stringAdapter = StringAdapter {
        val data = it.second
        if (data is AnimeSearchResponse) {
            searchInterface?.closeSearch()
            startActivity(
                AnimeActivity.newInstance(
                    requireActivity(),
                    data
                )
            )
        } else if (data is String) {
            openSearchResultFragment(data)
        }
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_front_page, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.history -> {
                    findNavController().navigate(FrontPageFragmentDirections.actionFrontPageFragmentToHistoryFragment())
                    true
                }
                R.id.favourites -> {
                    findNavController().navigate(FrontPageFragmentDirections.actionFrontPageFragmentToFavouritesFragment())
                    true
                }
                else -> false
            }
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
        initMenu()
    }

    @SuppressWarnings
    private fun initMenu() {
        setHasOptionsMenu(true)
        binding.toolbar.setOnMenuItemClickListener(menuProvider::onMenuItemSelected)
    }


    @Deprecated("Deprecated in Java")
    @SuppressWarnings
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuProvider.onCreateMenu(menu, inflater)
    }

    inner class StringAdapter(private val onClick: (Pair<String, Any>) -> Unit) :
        ListAdapter<Pair<String, Any>, RecyclerView.ViewHolder>(
            object : ItemCallback<Pair<String, Any>>() {
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
                    com.google.android.material.R.layout.m3_auto_complete_simple_item,
                    parent,
                    false
                )
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val textView = holder.itemView as TextView
            val item = getItem(position)
            textView.text = item.first
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
                stringAdapter.submitList(
                    frontPageViewModel.getSearchHistory().map { it to it })
            }
        }
    }

    private fun initSearchBar() {
        searchInterface?.setSuggestions(stringAdapter)
        frontPageViewModel.getSearchSuggestions().observe(this) { list ->
            if (list.isEmpty()) {
                searchInterface?.showError(Exception("No Results"))
            } else {
                stringAdapter.submitList(list.map { it.title to it })
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
                    if (newText.length > 2)
                        frontPageViewModel.querySearchSuggestions(newText.toString())
                    else {
                        stringAdapter.submitList(
                            frontPageViewModel.getSearchHistory().map { it to it })
                    }
                }

                override fun onQueryTextSubmit(query: CharSequence) {
                    openSearchResultFragment(query.toString())
                    stringAdapter.submitList(emptyList())
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
        binding.appbarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            binding.refreshLayout.isEnabled = verticalOffset == 0
        }
        binding.refreshLayout.setOnRefreshListener {
            frontPageViewModel.refreshAllPages()
        }
    }

    private fun initCarousel(carousel: CarouselRecyclerview) {
        if (carousel.adapter == null) {
            carousel.set3DItem(true)
            newAnimeAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            carousel.adapter = newAnimeAdapter
        }
    }

    private fun initFlow() {
        frontPageViewModel.zipped.observe(viewLifecycleOwner) { data ->
            Log.i(TAG, "for all anime ${data.all.size} items loaded")
            newAnimeAdapter.submitList(data.all)
            val frontPageViews = ArrayList<BaseItem>()
            frontPageViews.add(SearchBarData(this::initSearchBar))
            frontPageViews.add(HeaderData(0) {
                it.title = getString(R.string.new_anime)
                it.actionButtonText = getString(R.string.more)
                it.actionButton.setOnClick {
                    findNavController().navigate(FrontPageFragmentDirections.actionFrontPageFragmentToAllListFragment())
                }
            })
            frontPageViews.add(CarouselData(this::initCarousel))
            frontPageViews.add(HeaderData(0) {
                it.title = getString(R.string.subbed_anime)
                it.actionButtonText = getString(R.string.more)
                it.actionButton.setOnClick {
                    findNavController()
                        .navigate(FrontPageFragmentDirections.actionFrontPageFragmentToSubListFragment())
                }
            })
            frontPageViews.addAll(data.sub)
            frontPageViews.add(HeaderData(1) {
                it.title = getString(R.string.dubbed_anime)
                it.actionButtonText = getString(R.string.more)
                it.actionButton.setOnClick {
                    findNavController()
                        .navigate(FrontPageFragmentDirections.actionFrontPageFragmentToDubListFragment())
                }
            })
            frontPageViews.addAll(data.dub)

            frontPageAdapter.submitList(frontPageViews)
            Log.i(TAG, "for sub anime ${data.sub.size} items loaded")
            Log.i(TAG, "for dub anime ${data.dub.size} items loaded")
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


