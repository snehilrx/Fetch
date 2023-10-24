package com.otaku.kickassanime.page.episodepage

import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.google.android.gms.cast.framework.CastContext
import java.lang.ref.WeakReference


@UnstableApi
class CastHelper(
    context: CastContext?,
    private val simpleExoPlayer: ExoPlayer,
    setCurrentPlayer: ((Player) -> Unit)
) : SessionAvailabilityListener {


    private var castPlayer: CastPlayer? =
        context?.let { CastPlayer(it, EnhancedMediaItemConverter()) }

    init {
        castPlayer?.setSessionAvailabilityListener(this)
    }

    override fun onCastSessionAvailable() {
        castPlayer?.let { setCurrentPlayer(it) }
    }

    override fun onCastSessionUnavailable() {
        setCurrentPlayer(simpleExoPlayer)
    }

    private fun setCurrentPlayer(currentPlayer: Player) {
        setCurrentPlayer.get()?.invoke(currentPlayer)
    }

    fun release() {
        castPlayer?.release()
        castPlayer?.setSessionAvailabilityListener(null)
        setCurrentPlayer.clear()
    }

    private val setCurrentPlayer = WeakReference(setCurrentPlayer)
}