package com.otaku.kickassanime.page.episodepage

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.otaku.kickassanime.databinding.ActivityEpisodeBinding
import java.lang.ref.WeakReference

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
internal class PlayerListener(
    binding: ActivityEpisodeBinding,
    viewModel: EpisodeViewModel,
    showPlayerError: (PlaybackException) -> Unit
) : Player.Listener {

    private val binding = WeakReference(binding)
    private val viewModel = WeakReference(viewModel)
    private val showPlayerError = WeakReference(showPlayerError)

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        showPlayerError.get()?.invoke(error)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) binding.get()?.playerView?.player?.play()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == ExoPlayer.STATE_ENDED) {
            binding.get()?.episodeDetails?.animeSlug?.let { viewModel.get()?.addToFavourites(it) }
        }
        binding.get()?.playerView?.keepScreenOn =
            !(playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED)
    }

    fun releaseReferences() {
        binding.clear()
        viewModel.clear()
        showPlayerError.clear()
    }
}