package com.otaku.kickassanime.page.episodepage

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.otaku.kickassanime.databinding.ActivityEpisodeBinding

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
internal class PlayerListener(
    private val binding: ActivityEpisodeBinding,
    private val viewModel: EpisodeViewModel,
    private val showPlayerError: (PlaybackException) -> Unit
) : Player.Listener {

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        showPlayerError(error)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        viewModel.setIsPlaying(isPlaying)
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == ExoPlayer.STATE_ENDED) {
            binding.episodeDetails?.episodeSlugId?.let { viewModel.addToFavourites(it) }
        }
        binding.playerView.keepScreenOn =
            !(playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED)
    }
}