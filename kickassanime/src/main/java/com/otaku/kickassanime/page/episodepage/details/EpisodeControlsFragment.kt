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



    override fun onBind(binding: FragmentEpisodeControlsBinding, savedInstanceState: Bundle?) {

    }



}