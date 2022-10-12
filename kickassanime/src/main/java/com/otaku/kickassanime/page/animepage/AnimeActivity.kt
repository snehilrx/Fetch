package com.otaku.kickassanime.page.animepage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import com.otaku.fetch.base.ui.BindingActivity
import com.otaku.kickassanime.R
import com.otaku.kickassanime.api.model.AnimeSearchResponse
import com.otaku.kickassanime.databinding.ActivityAnimeBinding
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.utils.asAnimeEntity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnimeActivity : BindingActivity<ActivityAnimeBinding>(R.layout.activity_anime) {
    private val args: AnimeActivityArgs? by lazy {
        intent.extras?.let {
            AnimeActivityArgs.fromBundle(it)
        }
    }

    override fun onBind(binding: ActivityAnimeBinding, savedInstanceState: Bundle?) {
        super.onBind(binding, savedInstanceState)
        binding.animeEntity = args?.anime
        binding.graph = R.navigation.anime_navigation
        setTransparentStatusBar()
    }

    class AnimeActivityArgs(var anime: AnimeEntity) {
        companion object {
            private const val ARG_ANIME = "anime_args"
            fun fromBundle(bundle: Bundle): AnimeActivityArgs? {
                return bundle.getParcelable<AnimeEntity>(ARG_ANIME)?.let { AnimeActivityArgs(it) }
            }

            fun toBundle(anime: AnimeEntity): Bundle {
                return bundleOf(ARG_ANIME to anime)
            }
        }
    }

    companion object {
        fun newInstance(activity: Activity, anime: AnimeEntity): Intent {
            return Intent(activity, AnimeActivity::class.java).apply {
                putExtras(AnimeActivityArgs.toBundle(anime))
            }
        }

        fun newInstance(activity: Activity, anime: AnimeSearchResponse): Intent {
            return newInstance(
                activity,
                anime.asAnimeEntity()
            )

        }
    }

}
