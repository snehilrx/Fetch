package com.otaku.kickassanime.page.episodepage

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.otaku.fetch.base.ui.BindingFragment
import com.otaku.fetch.bindings.ImageViewBindings
import com.otaku.kickassanime.R
import com.otaku.kickassanime.Strings.KICKASSANIME_URL
import com.otaku.kickassanime.databinding.FragmentEpisodeBinding
import com.otaku.kickassanime.utils.Utils.showError
import com.otaku.kickassanime.utils.model.Response
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EpisodeFragment : BindingFragment<FragmentEpisodeBinding>(R.layout.fragment_episode) {

    private val viewModel: EpisodeViewModel by viewModels()

    private val args by navArgs<EpisodeFragmentArgs>()
    private var episodeSlugId: Int? = null
    private var animeSlugId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        episodeSlugId = args.episodeSlugId
        animeSlugId = args.animeSlugId
        if (resources.configuration.orientation == SCREEN_ORIENTATION_PORTRAIT) {
            initializePortrait()
        } else {
            initializeLanding()
        }
        return binding.root
    }

    private fun initializeLanding() {

    }

    private fun initializePortrait() {
        initAppbar(binding.appbar, findNavController())
        fetchRemote()
        initObservers()
    }

    private fun setAppbarBackground(image: String?) {
        if (image.isNullOrEmpty()) return
        ImageViewBindings.imageUrl(
            binding.appbar.appbarImageView,
            "$KICKASSANIME_URL/uploads/$image"
        )
    }

    private fun fetchRemote() {
        val animeId = animeSlugId
        val episodeId = episodeSlugId
        if (animeId != null && episodeId != null) {
            viewModel.fetchEpisode(episodeId, animeId)
            viewModel.fetchAnime(animeId)
        }
    }

    private fun initObservers() {
        viewModel.getEpisode().observe(viewLifecycleOwner) {
            when (it) {
                is Response.Error -> showError(it.error, requireActivity()) {
                    Navigation.findNavController(requireView()).popBackStack()
                }
                is Response.Success -> {
                    if (it.data == null) showError(
                        Exception("Episode not found"),
                        requireActivity()
                    ) {
                        Navigation.findNavController(requireView()).popBackStack()
                    } else {
                        setAppbarEpisodeNumber(it.data?.name)
                        binding.episodeDetails = it.data
                    }
                }
            }
        }

        viewModel.getAnime().observe(viewLifecycleOwner) {
            when (it) {
                is Response.Error -> showError(it.error, requireActivity()) {
                    Navigation.findNavController(requireView()).popBackStack()
                }
                is Response.Success -> {
                    setAppbarBackground(it.data?.image)
                    binding.animeDetails = it.data
                }
            }
        }
    }

    private fun setAppbarEpisodeNumber(name: String?) {
        binding.appbar.episodeNumber.isVisible = true
        binding.appbar.episodeNumber.text = "EP: $name"
    }

}
