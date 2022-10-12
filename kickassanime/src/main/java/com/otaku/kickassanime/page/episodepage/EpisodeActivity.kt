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
import androidx.lifecycle.LiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.*
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
import com.otaku.kickassanime.api.model.Maverickki
import com.otaku.kickassanime.databinding.ActivityEpisodeBinding
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@UnstableApi @AndroidEntryPoint
class EpisodeActivity : BindingActivity<ActivityEpisodeBinding>(R.layout.activity_episode) {

    private lateinit var mFullScreenDialog: Dialog
    private lateinit var episodeLiveData: LiveData<EpisodeEntity?>
    private lateinit var animeLiveData: LiveData<AnimeEntity?>
    private val viewModel: EpisodeViewModel by viewModels()

    private lateinit var args: EpisodeActivityArgs

    private val hlsMediaSource by lazy { HlsMediaSource.Factory(buildCacheDataSourceFactory()) }

    private val mediaSources = ConcatenatingMediaSource()

    private val trackSelector by lazy {
        DefaultTrackSelector(this, AdaptiveTrackSelection.Factory())
    }

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
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
        binding.playerView.findViewById<ImageButton>(androidx.media3.ui.R.id.exo_fullscreen)?.setImageDrawable(
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
        binding.playerView.player = player
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
        val fullscreen =
            binding.playerView.findViewById<View>(androidx.media3.ui.R.id.exo_fullscreen)
        fullscreen.isVisible = true
        binding.playerView.setFullscreenButtonClickListener {
            if(it)
                enterFullScreen()
            else
                exitFullScreen()
        }
        player.addMediaSource(mediaSources)
        player.playWhenReady = true
        player.prepare()
    }

    override fun onStart() {
        super.onStart()
        binding.playerView.player?.play()
    }

    override fun onStop() {
        super.onStop()
        binding.playerView.player?.stop()
    }

    override fun onResume() {
        super.onResume()
        binding.playerView.player?.play()
    }

    override fun onPause() {
        super.onPause()
        binding.playerView.player?.stop()
    }


    override fun onDestroy() {
        destroyWebView()
        trackSelector.release()
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
    }


    private fun buildCacheDataSourceFactory(): DataSource.Factory {
        val cache = getDownloadCache()
        val cacheSink = CacheDataSink.Factory()
            .setCache(cache)
        val upstreamFactory = DefaultDataSource.Factory(this, DefaultHttpDataSource.Factory())
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
        viewModel.getLoadState().observe(this) {
            when (it) {
                is State.FAILED -> {
                    if(it.shouldTerminateActivity) {
                        showError(it.exception, this)
                    } else {
                        showError(it.exception, this) {/* no-op */}
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
        binding.webView.maverickkiCallback = {
            viewModel.addMaverickki(it)
        }
        binding.webView.kaaPlayerCallback = {
            viewModel.addKaaPlayer(it)
        }
        binding.webView.onProgressChanged = {
            binding.progress = it
        }
    }

    private fun destroyWebView() {
        binding.webView.maverickkiCallback = null
        binding.webView.kaaPlayerCallback = null
        binding.webView.onProgressChanged = null
    }

    private fun fetchRemote() {
        viewModel.fetchEpisode(args.animeSlugId, args.episodeSlugId)
    }

    private fun initObservers(savedInstanceState: Bundle?) {
        initLiveData()
        episodeLiveData.observe(this) { episode ->
            if (episode != null) {
                setAppbarEpisodeNumber(binding, "EP: ${episode.name}")
                val url = episode.link1 ?: episode.link2 ?: episode.link3 ?: episode.link4
                if (url != null) {
                    if (savedInstanceState == null) binding.webView.loadUrl(url)
                }
            }
            initDetailsFragment(episode, animeLiveData.value)
        }
        animeLiveData.observe(this) { anime ->
            if (anime != null) {
                binding.animeDetails = anime
                binding.collapsingToolbar.title = anime.name
            }
            initDetailsFragment(episodeLiveData.value, anime)
        }
        viewModel.getKaaPlayerVideoLink().observe(this) {
            addLink(it)
        }
        viewModel.getMaverickkiVideo().observe(this) {
            addLink(
                Maverickki.BASE_URL + it.hls
            )
        }
    }

    private fun initLiveData() {
        if (this::episodeLiveData.isInitialized) episodeLiveData.removeObservers(this)
        if (this::animeLiveData.isInitialized) animeLiveData.removeObservers(this)
        animeLiveData = viewModel.getAnime(args.animeSlugId)
        episodeLiveData = viewModel.getEpisode(args.episodeSlugId)
    }

    private fun initDetailsFragment(episode: EpisodeEntity?, anime: AnimeEntity?) {
        if (episode != null && anime != null)
            (supportFragmentManager.findFragmentByTag("episodeDetailsContainer") as? NavHostFragment)
                ?.navController?.setGraph(
                    R.navigation.episode_detatils_navigation,
                    bundleOf(Pair("anime", anime), Pair("episode", episode))
                )
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.removeObservers(this)
        showLoading()
        intent.extras?.let { args = EpisodeActivityArgs.fromBundle(it) }
        if (this::episodeLiveData.isInitialized) episodeLiveData.removeObservers(this)
        if (this::animeLiveData.isInitialized) animeLiveData.removeObservers(this)
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