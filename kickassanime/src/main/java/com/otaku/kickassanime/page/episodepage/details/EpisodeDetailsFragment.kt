package com.otaku.kickassanime.page.episodepage.details

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.ActivityNavigator
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.otaku.fetch.base.ui.BindingFragment
import com.otaku.kickassanime.R
import com.otaku.kickassanime.databinding.FragmentEpisodeDetailsBinding
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.page.animepage.AnimeActivity
import com.otaku.kickassanime.page.animepage.AnimeFragmentDirections
import com.otaku.kickassanime.utils.LocalDateTimeSerializable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

class EpisodeDetailsFragment :
    BindingFragment<FragmentEpisodeDetailsBinding>(R.layout.fragment_episode_details) {

    private val args: EpisodeDetailsFragmentArgs by navArgs()

    init {
        FontAwesome.Icon.faw_chevron_right
    }

    override fun onBind(binding: FragmentEpisodeDetailsBinding, savedInstanceState: Bundle?) {
        binding.anime.setOnClickListener {
            startActivity(AnimeActivity.newInstance(this.requireActivity(), args.anime).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
        }
        binding.next.setOnClickListener {
            val next = args.episode.next
            if(next != null){
                openEpisode(next)
            } else{
                Toast.makeText(it.context, "No Next Episode", Toast.LENGTH_SHORT).show()
            }
        }
        binding.previous.setOnClickListener {
            val prev = args.episode.prev
            if(prev != null){
                openEpisode(prev)
            } else{
                Toast.makeText(it.context, "No Previous Episode", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openEpisode(episodeId: Int) {
        val actionAnimeFragmentToEpisodeActivity =
            EpisodeDetailsFragmentDirections.actionEpisodeDetailsFragmentToEpisodeActivity(
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