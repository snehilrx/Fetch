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
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lapism.search.widget.MaterialSearchView
import com.otaku.fetch.base.TAG
import com.otaku.fetch.base.databinding.CarouselItemLayoutBinding
import com.otaku.fetch.base.databinding.TileItemBinding
import com.otaku.fetch.base.livedata.State
import com.otaku.fetch.base.ui.BindingFragment
import com.otaku.fetch.base.ui.searchInterface
import com.otaku.kickassanime.R
import com.otaku.kickassanime.api.model.AnimeSearchResponse
import com.otaku.kickassanime.databinding.FragmentFrontPageBinding
import com.otaku.kickassanime.db.models.AnimeTile
import com.otaku.kickassanime.page.adapters.AnimeTileAdapterNoPaging
import com.otaku.kickassanime.page.adapters.HeaderAdapter
import com.otaku.kickassanime.page.animepage.AnimeActivity
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
        binding.root.setOnClickListener { onItemClick(item as AnimeTile) }
    }

    private val dubbedAnimeAdapter = AnimeTileAdapterNoPaging<TileItemBinding>(
        com.otaku.fetch.base.R.layout.tile_item
    ) { binding, item ->
        binding.tileData = item
        binding.root.setOnClickListener { onItemClick(item as AnimeTile) }
    }

    private val newAnimeAdapter = AnimeTileAdapterNoPaging<CarouselItemLayoutBinding>(
        com.otaku.fetch.base.R.layout.carousel_item_layout
    ) { binding, item ->
        binding.tileData = item
        binding.card.setOnClickListener { onItemClick(item as AnimeTile) }
    }

    private val adapter = ConcatAdapter()

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

        initSearchBar()
        initFrontPageList()
        initFlow()
        initMenu()
    }

    private fun initMenu() {
        setHasOptionsMenu(true)
        binding.toolbar.setOnMenuItemClickListener(menuProvider::onMenuItemSelected)
    }

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
            textView.text = getItem(position).first
            textView.setOnClickListener {
                onClick(getItem(position))
            }
        }
    }

    private fun initSearchBar() {
        val stringAdapter = StringAdapter {
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

        searchInterface?.setSuggestions(stringAdapter)

        binding.searchBar.apply {
            setHint(getString(R.string.search_anime))
            setOnClickListener {
                searchInterface?.openSearch()
                stringAdapter.submitList(
                    frontPageViewModel.getSearchHistory().map { it to it })
            }
        }

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
        binding.appbarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (abs(verticalOffset) == appBarLayout.totalScrollRange) {
                binding.carousel.visibility = View.INVISIBLE
                binding.carouselHeading.root.visibility = View.INVISIBLE
                binding.searchBar.visibility = View.INVISIBLE
            } else if (verticalOffset == 0) {
                binding.carousel.visibility = View.VISIBLE
                binding.carouselHeading.root.visibility = View.VISIBLE
                binding.searchBar.visibility = View.VISIBLE
            } else {
                binding.carousel.visibility = View.VISIBLE
                binding.carouselHeading.root.visibility = View.VISIBLE
                binding.searchBar.visibility = View.VISIBLE
            }
        }
        initCarousel()
        initList()
        binding.refreshLayout.setOnRefreshListener {
            frontPageViewModel.refreshAllPages()
        }
    }

    private fun initList() {
        if (adapter.adapters.size == 0)
            adapter.apply {
                addAdapter(
                    HeaderAdapter(
                        getString(R.string.subbed_anime),
                        getString(R.string.more)
                    ) {
                        findNavController().navigate(FrontPageFragmentDirections.actionFrontPageFragmentToSubListFragment())
                    })
                addAdapter(subbedAnimeAdapter)
                addAdapter(
                    HeaderAdapter(
                        getString(R.string.dubbed_anime),
                        getString(R.string.more)
                    ) {
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


