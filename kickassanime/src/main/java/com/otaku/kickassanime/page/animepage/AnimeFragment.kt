package com.otaku.kickassanime.page.animepage

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.ActivityNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.otaku.fetch.base.livedata.State
import com.otaku.fetch.base.ui.BindingFragment
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
                    TODO("Handle error")
                }
                is State.SUCCESS -> {
                    hideLoading()
                }
                else -> {}
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
                title,
                episodeSlugId,
                animeSlugId
            )
        val extras = ActivityNavigator.Extras.Builder()
            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            .build()
        findNavController().navigate(actionAnimeFragmentToEpisodeActivity, extras)
    }

    companion object {
        @BindingAdapter("startArgs", "navGraphRes")
        @JvmStatic
        fun setGraph(view: FragmentContainerView, args: AnimeEntity, navGraphId: Int) {
            view.getFragment<NavHostFragment>().navController.setGraph(
                navGraphId,
                bundleOf("animeSlugId" to args.animeSlugId, "animeSlug" to (args.animeslug ?: ""),"title" to args.getDisplayTitle())
            )
        }
    }
}
