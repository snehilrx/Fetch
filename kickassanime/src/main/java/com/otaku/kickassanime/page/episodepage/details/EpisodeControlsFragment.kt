package com.otaku.kickassanime.page.episodepage.details

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.ActivityNavigator
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.otaku.fetch.base.ui.BindingFragment
import com.otaku.fetch.base.utils.UiUtils.showError
import com.otaku.kickassanime.R
import com.otaku.kickassanime.databinding.FragmentEpisodeControlsBinding
import com.otaku.kickassanime.page.animepage.AnimeActivity
import com.otaku.kickassanime.page.episodepage.EpisodeViewModel

class EpisodeControlsFragment :
    BindingFragment<FragmentEpisodeControlsBinding>(R.layout.fragment_episode_controls) {

    private val args: EpisodeControlsFragmentArgs by navArgs()
    private val viewModel: EpisodeViewModel by activityViewModels()

    init {
        FontAwesome.Icon.faw_chevron_right
    }

    override fun onBind(binding: FragmentEpisodeControlsBinding, savedInstanceState: Bundle?) {
        binding.anime.setOnClickListener {
            startActivity(
                AnimeActivity.newInstance(this.requireActivity(), args.anime)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            )
        }
        binding.next.setOnClickListener {
            openNextEpisode()
        }
        binding.previous.setOnClickListener {
            openPrevEpisode()
        }
        binding.mal.setOnClickListener {
            openLink(
                "https://myanimelist.net/anime/${
                    args.anime.malId
                }"
            )
        }
        binding.links.setOnClickListener {
            val link = viewModel.getVideoLink().value
            if(link != null) {
                openLink(link)
            } else {
                Toast.makeText(requireContext(),"No Link was found", Toast.LENGTH_SHORT).show()
            }
        }
        initDropDown()
        viewModel.onNextEpisode = this::openNextEpisode
        viewModel.onPreviousEpisode = this::openPrevEpisode
    }



    private fun initDropDown() {
        viewModel.getLinks().observe(this){ links ->
            if(links.size>0){
                binding.servers.setSimpleItems(links.map { it.first }.toTypedArray())
                binding.servers.onItemClickListener =
                    OnItemClickListener { _, _, position, _ -> viewModel.setCurrentServer(links[position].second) }
                binding.servers.setText(links[0].first)
                viewModel.setCurrentServer(links[0].second)
            } else {
              showError(Exception("No servers found"), requireActivity())
            }
        }
    }

    private fun openLink(link: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        try {
            startActivity(browserIntent)
        } catch (e: ActivityNotFoundException){
            Toast.makeText(context, "No activity found to open link $link", Toast.LENGTH_SHORT).show()
        }
    }

    fun openNextEpisode() {
        val next = args.episode.next
        if (next != null) {
            openEpisode(next)
        } else {
            Toast.makeText(requireContext(), "No Next Episode", Toast.LENGTH_SHORT).show()
        }
    }

    fun openPrevEpisode() {
        val prev = args.episode.prev
        if (prev != null) {
            openEpisode(prev)
        } else {
            Toast.makeText(requireContext(), "No Previous Episode", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openEpisode(episodeId: Int) {
        val actionAnimeFragmentToEpisodeActivity =
            EpisodeControlsFragmentDirections.actionEpisodeDetailsFragmentToEpisodeActivity(
                args.anime.getDisplayTitle() ?: "about:blank",
                episodeSlugId = episodeId,
                animeSlugId = args.anime.animeSlugId
            )
        val extras = ActivityNavigator.Extras.Builder()
            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            .build()
        findNavController().navigate(actionAnimeFragmentToEpisodeActivity, extras)
    }
}