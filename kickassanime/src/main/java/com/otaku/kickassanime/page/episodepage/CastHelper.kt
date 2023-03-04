package com.otaku.kickassanime.page.episodepage

import android.R
import androidx.core.content.res.ResourcesCompat
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.DefaultMediaItemConverter
import androidx.media3.cast.MediaItemConverter
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.SubtitleConfiguration
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.SingleSampleMediaSource
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaInfo.UNKNOWN_DURATION
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaTrack
import com.google.android.gms.cast.framework.CastContext


@UnstableApi
class CastHelper(
    context: CastContext?,
    private val simpleExoPlayer: ExoPlayer,
    playerView: PlayerView
) : SessionAvailabilityListener {

    private var castPlayer: CastPlayer? = context?.let { CastPlayer(it, EnhancedMediaItemConverter()) }

    init {
        castPlayer?.setSessionAvailabilityListener(this)
        playerView.player = simpleExoPlayer
    }

    override fun onCastSessionAvailable() {
        castPlayer?.let { setCurrentPlayer(it) }
    }

    override fun onCastSessionUnavailable() {
        setCurrentPlayer(simpleExoPlayer)
    }

    private fun setCurrentPlayer(currentPlayer: Player) {
        setCurrentPlayer?.invoke(currentPlayer)
    }

    var setCurrentPlayer: ((Player) -> Unit)? = null
}