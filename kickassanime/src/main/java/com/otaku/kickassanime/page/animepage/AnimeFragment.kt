package com.otaku.kickassanime.page.animepage

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.marginEnd
import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.ActivityNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.mikepenz.iconics.IconicsColor
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.IconicsSize
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.backgroundColor
import com.mikepenz.iconics.utils.color
import com.mikepenz.iconics.utils.padding
import com.mikepenz.iconics.utils.size
import com.otaku.fetch.base.livedata.State
import com.otaku.fetch.base.ui.BindingFragment
import com.otaku.fetch.base.utils.UiUtils
import com.otaku.fetch.base.utils.UiUtils.getThemeColor
import com.otaku.fetch.base.utils.UiUtils.showError
import com.otaku.fetch.base.utils.UiUtils.toPxInt
import com.otaku.kickassanime.R
import com.otaku.kickassanime.databinding.FragmentAnimeBinding
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.page.adapters.EpisodeAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnimeFragment : BindingFragment<FragmentAnimeBinding>(R.layout.fragment_anime) {

    private val animeViewModel by viewModels<AnimeViewModel>()

    private val args: AnimeFragmentArgs by navArgs()

    override fun onBind(binding: FragmentAnimeBinding, savedInstanceState: Bundle?) {
        super.onBind(binding, savedInstanceState)
        initAppbar(
            binding.shineView,
            binding.toolbar,
            binding.collapsingToolbar,
            binding.appbarLayout,
            findNavController(),
            true
        )
        animeViewModel.fetchAnime(args.animeSlug)
        initObserver(args.animeSlugId)
        initRecyclerView()
        setUpFavourite()
    }

    private fun setUpFavourite() {
        binding.favorite.apply {
            checkedIcon = IconicsDrawable(requireContext()).apply {
                icon = FontAwesome.Icon.faw_heart
                color = IconicsColor.parse("#ff1a40")
                size = IconicsSize.TOOLBAR_ICON_SIZE
                style = Paint.Style.FILL_AND_STROKE
                padding = IconicsSize.TOOLBAR_ICON_PADDING
            }
            uncheckedIcon = IconicsDrawable(requireContext()).apply {
                icon = FontAwesome.Icon.faw_heart
                color = IconicsColor.colorInt(
                    getThemeColor(
                        requireContext().theme, com.lapism.search.R.attr.colorOnPrimary
                    )
                )
                size = IconicsSize.TOOLBAR_ICON_SIZE
                style = Paint.Style.STROKE
                padding = IconicsSize.TOOLBAR_ICON_PADDING
            }
            setOnClickListener {
                binding.anime?.animeSlugId?.let { animeSlugId ->
                    animeViewModel.setFavourite(
                        animeSlugId, binding.favorite.isChecked
                    )
                }
            }
        }
    }

    private fun initRecyclerView() {
        val displayMetrics = resources.displayMetrics
        (binding.episodeList.layoutManager as GridLayoutManager).spanCount =
            (displayMetrics.heightPixels / (48.toPxInt + resources.getDimension(
                com.otaku.fetch.base.R.dimen.item_spacing
            ).toInt() * 2)).coerceAtLeast(1)
    }

    private fun initObserver(animeSlugId: Int) {
        var oldEpisodeList: LiveData<List<EpisodeAdapter.Episode>>? = null
        animeViewModel.getAnime(animeSlugId).observe(this) { animeEntityNullable ->
            animeEntityNullable?.let { animeEntity ->
                binding.anime = animeEntity
                binding.favorite.isChecked = animeEntity.favourite
                binding.episodeList.adapter = getEpisodeAdapter(animeEntity)
                val animeId = animeEntity.animeId
                if (animeId != null) {
                    oldEpisodeList?.removeObservers(this)
                    oldEpisodeList = animeViewModel.getEpisodeList(animeId)
                    oldEpisodeList?.observe(this) {
                        (binding.episodeList.adapter as? EpisodeAdapter)?.submitList(it)
                    }
                }
            }
        }
        animeViewModel.state.observe(this) {
            when (it) {
                is State.LOADING -> {
                    showLoading()
                }
                is State.FAILED -> {
                    hideLoading()
                    showError(it.exception, this.requireActivity())
                }
                is State.SUCCESS -> {
                    hideLoading()
                }
            }
        }
    }

    private fun showLoading() {
        binding.shimmerLoading.startShimmer()
        binding.shimmerLoading.isVisible = true
        binding.list.isVisible = false
    }

    private fun hideLoading() {
        binding.shimmerLoading.stopShimmer()
        binding.shimmerLoading.isVisible = false
        binding.list.isVisible = true
    }

    private fun getEpisodeAdapter(animeEntity: AnimeEntity) = EpisodeAdapter {
        val title = animeEntity.getDisplayTitle() ?: "NO TITLE"
        val animeSlugId = animeEntity.animeSlugId
        val episodeSlugId = it.id
        val actionAnimeFragmentToEpisodeActivity =
            AnimeFragmentDirections.actionAnimeFragmentToEpisodeActivity(
                title, episodeSlugId, animeSlugId
            )
        val extras =
            ActivityNavigator.Extras.Builder().addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                .build()
        findNavController().navigate(actionAnimeFragmentToEpisodeActivity, extras)
    }

    companion object {
        @BindingAdapter("startArgs", "navGraphRes")
        @JvmStatic
        fun setGraph(view: FragmentContainerView, args: AnimeEntity, navGraphId: Int) {
            view.getFragment<NavHostFragment>().navController.setGraph(
                navGraphId, bundleOf(
                    "animeSlugId" to args.animeSlugId,
                    "animeSlug" to (args.animeslug ?: ""),
                    "title" to args.getDisplayTitle()
                )
            )
        }
    }
}
