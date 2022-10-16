package com.otaku.kickassanime.page.episodepage

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.*
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navArgs
import com.otaku.fetch.base.livedata.State
import com.otaku.fetch.base.ui.BindingActivity
import com.otaku.fetch.base.utils.UiUtils.loadBitmapFromUrl
import com.otaku.fetch.base.utils.UiUtils.showError
import com.otaku.kickassanime.R
import com.otaku.kickassanime.Strings
import com.otaku.kickassanime.api.model.Maverickki
import com.otaku.kickassanime.databinding.ActivityEpisodeBinding
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.page.episodepage.details.EpisodeControlsFragment
import com.otaku.kickassanime.utils.TrackSelectionDialog
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Inject


@UnstableApi
@AndroidEntryPoint
class EpisodeActivity : BindingActivity<ActivityEpisodeBinding>(R.layout.activity_episode) {

    private lateinit var mFullScreenDialog: Dialog
    private val viewModel: EpisodeViewModel by viewModels()

    private lateinit var args: EpisodeActivityArgs

    private val hlsMediaSource by lazy { HlsMediaSource.Factory(buildCacheDataSourceFactory()) }

    private val mediaSources = ConcatenatingMediaSource()

    private val trackSelector by lazy {
        DefaultTrackSelector(this, AdaptiveTrackSelection.Factory())
    }

    @Inject
    lateinit var okhttp: OkHttpClient

    override fun onBind(binding: ActivityEpisodeBinding, savedInstanceState: Bundle?) {
        args = navArgs<EpisodeActivityArgs>().value
        mFullScreenDialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        mFullScreenDialog.setOnDismissListener {
            exitFullScreen()
        }
        initAppbar(
            binding.appbarImageView,
            binding.toolbar
        )
        initialize(savedInstanceState)
        initializeWebView()
        setTransparentStatusBar()
        showBackButton()
        binding.appbarLayout.setPaddingRelative(0, _statusBarHeight, 0, 0)
    }

    private fun exitFullScreen() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        (binding.playerView.parent as ViewGroup).removeView(binding.playerView)
        binding.aspectRatioFrameLayout.addView(
            binding.playerView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        showSystemUI()
        mFullScreenDialog.dismiss()
    }

    // This snippet hides the system bars.
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        mFullScreenDialog.window?.decorView?.let {
            WindowInsetsControllerCompat(window, it).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        mFullScreenDialog.window?.decorView?.let {
            WindowInsetsControllerCompat(
                window,
                it
            ).show(WindowInsetsCompat.Type.systemBars())
        }
    }

    @SuppressLint("PrivateResource")
    private fun enterFullScreen() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        (binding.playerView.parent as ViewGroup).removeView(binding.playerView)
        mFullScreenDialog.addContentView(
            binding.playerView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        binding.playerView.findViewById<ImageButton>(androidx.media3.ui.R.id.exo_fullscreen)
            ?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    androidx.media3.ui.R.drawable.exo_ic_fullscreen_exit
                )
            )
        mFullScreenDialog.show()
        hideSystemUI()
    }

    private fun initPlayer() {
        val player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
        trackSelector.setParameters(
            trackSelector
                .buildUponParameters()
                .setAllowVideoMixedMimeTypeAdaptiveness(true)
        )
        player.repeatMode = ExoPlayer.REPEAT_MODE_OFF
        binding.playerView.player = player
        binding.playerView.findViewById<View>(androidx.media3.ui.R.id.exo_next).setOnClickListener {
            viewModel.onNextEpisode?.invoke()
        }
        binding.playerView.findViewById<View>(androidx.media3.ui.R.id.exo_prev).setOnClickListener {
            viewModel.onPreviousEpisode?.invoke()
        }
        binding.playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
        binding.playerView.setKeepContentOnPlayerReset(true)
        binding.animeDetails?.getImageUrl()?.let {
            loadBitmapFromUrl(
                it, this
            ) { bitmap ->
                binding.playerView.defaultArtwork = bitmap?.toDrawable(resources)
            }
        }
        binding.playerView.useArtwork = true
        viewModel.getPlaybackTime().observe(this){
            if(it != null) {
                binding.playerView.player?.seekTo(it)
            }
        }
        initPlayerControls()
        player.addListener(object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                tracks.groups.forEach {
                    it.getTrackFormat(0)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                showError(error, this@EpisodeActivity, "retry") {
                    player.prepare()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                viewModel.setIsPlaying(isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if(playbackState == ExoPlayer.STATE_ENDED) {
                    binding.episodeDetails?.episodeSlugId?.let { viewModel.addToFavourites(it) }
                }
                binding.playerView.keepScreenOn = !(playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED)
            }
        })
    }

    private fun initPlayerControls() {
        val settingButton =
            binding.playerView.findViewById<ImageButton>(androidx.media3.ui.R.id.exo_settings)
        settingButton.setOnClickListener {
            binding.playerView.player?.let { it1 ->
                TrackSelectionDialog.createForPlayer(it1) {}.show(this)
            }
        }
        val fullscreen =
            binding.playerView.findViewById<View>(androidx.media3.ui.R.id.exo_fullscreen)
        fullscreen.isVisible = true
        binding.playerView.setFullscreenButtonClickListener {
            if (it)
                enterFullScreen()
            else
                exitFullScreen()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.playerView.player?.currentPosition?.let {
            binding.episodeDetails?.episodeSlugId?.let { episodeSlugId ->
                viewModel.updatePlayBackTime(
                    episodeSlugId,
                    it
                )
            }
        }
        binding.playerView.player?.stop()
    }


    override fun onDestroy() {
        destroyWebView()
        binding.playerView.player?.release()
        super.onDestroy()
    }

    private fun addLink(url: String) {
        val mediaItem = MediaItem.Builder()
            .setMediaMetadata(
                MediaMetadata.Builder().setTitle("${args.title} ${binding.episodeDetails?.title}")
                    .build()
            )
            .setUri(url)
            .setMimeType(
                MimeTypes.APPLICATION_M3U8
            ).build()
        val mediaSource = hlsMediaSource.createMediaSource(mediaItem)
        mediaSources.addMediaSource(mediaSource)
        binding.progress = 100
        (binding.playerView.player as? ExoPlayer)?.let { player ->
            if (mediaSources.size == 1) {
                player.playWhenReady = true
                player.addMediaSource(mediaSources)
                player.prepare()
            }
        }
    }


    private fun buildCacheDataSourceFactory(): DataSource.Factory {
        val cache = getDownloadCache()
        val cacheSink = CacheDataSink.Factory()
            .setCache(cache)
        val upstreamFactory = DefaultDataSource.Factory(this, OkHttpDataSource.Factory(okhttp))
        return CacheDataSource.Factory()
            .setCache(cache)
            .setCacheWriteDataSinkFactory(cacheSink)
            .setCacheReadDataSourceFactory(FileDataSource.Factory())
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    @Synchronized
    private fun getDownloadCache(): Cache {
        var downloadCacheLocal = downloadCache
        if (downloadCacheLocal == null) {
            val downloadContentDirectory = File(
                cacheDir,
                "video"
            )
            downloadCacheLocal = SimpleCache(
                downloadContentDirectory,
                NoOpCacheEvictor(),
                StandaloneDatabaseProvider(this)
            )
            downloadCache = downloadCacheLocal
        }
        return downloadCacheLocal
    }

    private fun initialize(savedInstanceState: Bundle?) {
        initPlayer()
        fetchRemote()
        viewModel.getIsPlaying().observe(this) {
            if (it) binding.playerView.player?.play()
        }
        viewModel.getLoadState().observe(this) {
            when (it) {
                is State.FAILED -> {
                    if (it.shouldTerminateActivity) {
                        showError(it.exception, this)
                    } else {
                        showError(it.exception, this) {/* no-op */ }
                    }
                }
                is State.LOADING -> showLoading()
                is State.SUCCESS -> {
                    hideLoading()
                    initObservers(savedInstanceState)
                }
            }
        }
    }

    private fun initializeWebView() {
        binding.webView.videoLinksCallback = {
            viewModel.handleVideoLinks(it)
        }
        binding.webView.onProgressChanged = {
            binding.progress = it
        }
    }

    private fun destroyWebView() {
        binding.webView.videoLinksCallback = null
        binding.webView.onProgressChanged = null
    }

    private fun fetchRemote() {
        viewModel.fetchEpisode(args.animeSlugId, args.episodeSlugId)
    }

    private fun initObservers(savedInstanceState: Bundle?) {
        viewModel.getEpisode(args.episodeSlugId).observe(this) { episode ->
            if (episode != null) {
                setAppbarEpisodeNumber(binding, "EP: ${episode.name}")
                binding.episodeDetails = episode
                viewModel.addToHistory(episode)
                viewModel.loadDustUrls(episode.link1)
                viewModel.loadMobile2Urls(episode.link4)
            }
            viewModel.getAnime(args.animeSlugId).observe(this) { anime ->
                if (anime != null) {
                    binding.animeDetails = anime
                    binding.collapsingToolbar.title = anime.name
                }
                initDetailsFragment(episode, anime)
            }
        }

        viewModel.getCurrentServer().observe(this) {
            mediaSources.clear()
            binding.webView.loadUrl(it)
        }

        viewModel.getVideoLink().observe(this) {
            addLink(it)
        }
    }


    private fun initDetailsFragment(episode: EpisodeEntity?, anime: AnimeEntity?) {
        if (episode != null && anime != null)
            getEpisodeControlsNavigationHost()
                ?.navController?.setGraph(
                    R.navigation.episode_detatils_navigation,
                    bundleOf(Pair("anime", anime), Pair("episode", episode))
                )
    }

    private fun getEpisodeControlsNavigationHost() =
        (supportFragmentManager.findFragmentByTag("episodeDetailsContainer") as? NavHostFragment)

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.removeObservers(this)
        showLoading()
        intent.extras?.let { args = EpisodeActivityArgs.fromBundle(it) }
        mediaSources.clear()
        initialize(null)
    }


    private fun showLoading() {
        binding.shimmerLoading.startShimmer()
        binding.shimmerLoading.isVisible = true
        binding.episode.isVisible = false
    }

    private fun hideLoading() {
        binding.shimmerLoading.stopShimmer()
        binding.shimmerLoading.isVisible = false
        binding.episode.isVisible = true
    }

    private fun setAppbarEpisodeNumber(binding: ActivityEpisodeBinding, name: String?) {
        binding.episodeNumber.isVisible = true
        binding.episodeNumber.text = name
    }

    companion object {
        @JvmStatic
        private var downloadCache: SimpleCache? = null
    }
}