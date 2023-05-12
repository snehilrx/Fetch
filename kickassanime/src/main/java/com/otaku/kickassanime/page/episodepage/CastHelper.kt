package com.otaku.kickassanime.page.episodepage

import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
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