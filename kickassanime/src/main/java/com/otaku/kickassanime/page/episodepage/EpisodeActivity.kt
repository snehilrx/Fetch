package com.otaku.kickassanime.page.episodepage

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.widget.*
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.core.os.postDelayed
import androidx.core.view.*
import androidx.media3.cast.CastPlayer
import androidx.media3.common.*
import androidx.media3.common.MediaItem.SubtitleConfiguration
import androidx.media3.common.util.RepeatModeUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.*
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.*
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerControlView
import androidx.navigation.navArgs
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaTrack
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.dynamite.DynamiteModule.LoadingException
import com.otaku.fetch.base.TAG
import com.otaku.fetch.base.livedata.State
import com.otaku.fetch.base.ui.BindingActivity
import com.otaku.fetch.base.ui.setOnClick
import com.otaku.fetch.base.utils.UiUtils.loadBitmapFromUrl
import com.otaku.fetch.base.utils.UiUtils.showError
import com.otaku.kickassanime.R
import com.otaku.kickassanime.api.model.CommonSubtitle
import com.otaku.kickassanime.databinding.ActivityEpisodeBinding
import com.otaku.kickassanime.db.models.CommonVideoLink
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.page.animepage.AnimeActivity
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Inject


@UnstableApi
@AndroidEntryPoint
class EpisodeActivity : BindingActivity<ActivityEpisodeBinding>(R.layout.activity_episode) {


    private var castContext: CastContext? = null

    // region Threading stuff
    private val handler = Handler(Looper.getMainLooper())
    private var timeSkipLoop: Runnable? = null
    private var delayedPlayBack: Runnable? = null
    // endregion

    private lateinit var playerViewUiHelper: PlayerViewUiHelper
    private val viewModel: EpisodeViewModel by viewModels()
    private lateinit var args: EpisodeActivityArgs

    // region Media stuff
    private val cachingDataSourceFactory by lazy {
        val cache = getDownloadCache()
        val cacheSink = CacheDataSink.Factory()
            .setCache(cache)
        val upstreamFactory = DefaultDataSource.Factory(this, OkHttpDataSource.Factory(okhttp))
        return@lazy CacheDataSource.Factory()
            .setCache(cache)
            .setCacheWriteDataSinkFactory(cacheSink)
            .setCacheReadDataSourceFactory(FileDataSource.Factory())
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
    private val hlsMediaSource by lazy { HlsMediaSource.Factory(cachingDataSourceFactory) }
    private val dashMediaSource by lazy { DashMediaSource.Factory(cachingDataSourceFactory) }
    private val trackSelector by lazy {
        DefaultTrackSelector(this, AdaptiveTrackSelection.Factory()).apply {
            setParameters(
                DefaultTrackSelector.Parameters.Builder(this@EpisodeActivity)
                    .setPreferredTextLanguage("en")
            )
        }
    }
    private val mediaSource = ArrayList<MediaSource>()
    private val subtitleSources = ArrayList<CommonSubtitle>()
    private val playerListener by lazy { PlayerListener(binding, viewModel, this::showPlayerError) }
    // endregion

    @Inject
    lateinit var okhttp: OkHttpClient

    override fun onBind(binding: ActivityEpisodeBinding, savedInstanceState: Bundle?) {
        args = navArgs<EpisodeActivityArgs>().value
        fetchRemote()
        playerViewUiHelper = PlayerViewUiHelper(
            this,
            binding.playerView,
            binding.aspectRatioFrameLayout,
            isFullscreen()
        )
        playerViewUiHelper.onSelectStream = {
            onSelectStream {
                loadLink(it)
            }
        }
        initAppbar(
            binding.appbarImageView,
            binding.toolbar
        )
        initialize()
        initializeWebView()
        setTransparentStatusBar()
        showBackButton()
        binding.appbarLayout.setPaddingRelative(0, mStatusBarHeight, 0, 0)
    }

    private fun isFullscreen(): Boolean {
        return binding.playerView.parent != binding.aspectRatioFrameLayout
    }

    private fun initPlayer() {
        val player = ExoPlayer.Builder(this)
            .setUsePlatformDiagnostics(true)
            .setTrackSelector(trackSelector)
            .build()
        trackSelector.setParameters(
            trackSelector
                .buildUponParameters()
                .setAllowVideoMixedMimeTypeAdaptiveness(true)
        )
        castContext = try {
            CastContext.getSharedInstance(applicationContext)
        } catch (e: RuntimeException) {
            Log.e(TAG, "creating media cast context failed", e)
            null
        }
        initCastHelper(player)
        binding.playerView.setKeepContentOnPlayerReset(true)
        binding.animeDetails?.getImageUrl()?.let {
            loadBitmapFromUrl(
                it, this
            ) { bitmap ->
                binding.playerView.defaultArtwork = bitmap?.toDrawable(resources)
            }
        }
        binding.playerView.useArtwork = true
        viewModel.getPlaybackTime().observe(this) {
            if (it != null) {
                binding.playerView.player?.seekTo(it)
            }
        }
        initPlayerControls()

        binding.playerView.setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE)
        player.addListener(playerListener)
        playerViewUiHelper.showLoading()
    }

    private fun initCastHelper(player: ExoPlayer) {
        val playerView = binding.playerView
        val castHelper = CastHelper(castContext, player, playerView)
        castHelper.setCurrentPlayer = { currentPlayer ->
            val oldPlayer = playerView.player
            playerView.player = currentPlayer
            playerView.controllerHideOnTouch = currentPlayer === player

            // Player state management.
            var playbackPositionMs = C.TIME_UNSET
            var playWhenReady = false

            if (oldPlayer != null) {
                // Save state from the previous player.
                val playbackState = oldPlayer.playbackState
                if (playbackState != Player.STATE_ENDED) {
                    playbackPositionMs = oldPlayer.currentPosition
                    playWhenReady = oldPlayer.playWhenReady
                }
                oldPlayer.stop()
                oldPlayer.clearMediaItems()
            }

            if (currentPlayer === player) {
                // exoplayer
                playerView.controllerShowTimeoutMs = PlayerControlView.DEFAULT_SHOW_TIMEOUT_MS
                playerView.defaultArtwork = null
                playMedia()
                currentPlayer.seekTo(playbackPositionMs)
            } else if (currentPlayer is CastPlayer) {
                // cast player
                playerView.controllerShowTimeoutMs = 0
                playerView.showController()
                playerView.defaultArtwork = ResourcesCompat.getDrawable(
                    playerView.context.resources,
                    androidx.media3.cast.R.drawable.cast_album_art_placeholder,
                    null
                )
                currentPlayer.clearMediaItems()

                val subs = subtitleSources.map {
                    SubtitleConfiguration.Builder(Uri.parse(it.getLink()))
                        .setMimeType(it.getFormat())
                        .setLanguage(it.getLanguage())
                        .build()
                }
                val mediaItems =  mediaSource.map {
                    it.mediaItem.buildUpon()
                        .setSubtitleConfigurations(subs)
                        .build()
                }
                currentPlayer.setMediaItems(mediaItems,
                    oldPlayer?.currentMediaItemIndex ?: C.INDEX_UNSET,
                    oldPlayer?.currentPosition ?: C.TIME_UNSET)
                oldPlayer?.trackSelectionParameters?.let {
                    currentPlayer.trackSelectionParameters = it
                }
                currentPlayer.seekTo(playbackPositionMs)
                currentPlayer.playWhenReady = playWhenReady
                currentPlayer.prepare()
            }
        }
        val controls = binding.episodeDetailsContainer
        CastButtonFactory.setUpMediaRouteButton(applicationContext, controls.mediaCast)
        controls.castBtn.setOnClick { controls.mediaCast.performClick() }

    }

    private fun initPlayerControls() {
        viewModel.getTimeSkip().observe(this) { timestamps ->
            timeSkipLoop?.let { handler.removeCallbacks(it) }
            timeSkipLoop = object : Runnable {
                override fun run() {
                    val currentPos = binding.playerView.player?.currentPosition
                    for (i in 0..timestamps.size - 2) {
                        if (currentPos != null && currentPos >= timestamps[i] && currentPos <= timestamps[i + 1]) {
                            playerViewUiHelper.skipIntroButton.isVisible = true
                            playerViewUiHelper.skipIntroButton.setOnClick {
                                binding.playerView.player?.seekTo(
                                    timestamps[i] + 1
                                )
                            }
                        } else {
                            playerViewUiHelper.skipIntroButton.isVisible = false
                        }
                    }
                    handler.postDelayed(this, 2000)
                }
            }
            timeSkipLoop?.let { handler.postDelayed(timeSkipLoop as Runnable, 2000) }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // If the event was not handled then see if the player view can handle it.
        return super.dispatchKeyEvent(event) || binding.playerView.dispatchKeyEvent(event)
    }

    override fun onPause() {
        super.onPause()
        timeSkipLoop?.let { handler.removeCallbacks(it) }
        binding.playerView.player?.currentPosition?.let {
            binding.episodeDetails?.episodeSlugId?.let { episodeSlugId ->
                viewModel.updatePlayBackTime(
                    episodeSlugId,
                    it
                )
            }
        }
        val player = binding.playerView.player
        if (player is ExoPlayer) {
            player.stop()
        }
    }


    override fun onDestroy() {
        destroyWebView()
        val player = binding.playerView.player
        if (player is ExoPlayer) {
            releasePlayer()
        }
        super.onDestroy()
    }

    private fun releasePlayer() {
        mediaSource.clear()
        subtitleSources.clear()
        binding.playerView.player?.clearMediaItems()
        binding.playerView.player?.removeListener(playerListener)
        binding.playerView.player?.release()
        binding.playerView.player = null
    }

    private fun loadLink(item: CommonVideoLink) {
        Log.i(TAG, "DataLink ${item.getLink()}")
        val mediaItem = MediaItem.Builder()
            .setMediaMetadata(
                MediaMetadata.Builder().setTitle("${args.title} ${binding.episodeDetails?.title}")
                    .build()
            )
            .setUri(item.getLink())
            .setMimeType(
                when (item.getVideoType()) {
                    CommonVideoLink.HLS -> MimeTypes.APPLICATION_M3U8
                    CommonVideoLink.DASH -> MimeTypes.APPLICATION_MPD
                    else -> return
                }
            ).build()
        mediaSource.add(
            when (item.getVideoType()) {
                CommonVideoLink.HLS -> hlsMediaSource.createMediaSource(
                    mediaItem
                )
                CommonVideoLink.DASH -> {
                    dashMediaSource.createMediaSource(mediaItem)
                }
                else -> return
            }
        )
        playMedia()
    }

    private fun playMedia() {
        (binding.playerView.player as? ExoPlayer)?.let { player ->
            player.clearMediaItems()
            val subs = subtitleSources.filter { it.getLink().isNotEmpty() }.map {
                SingleSampleMediaSource.Factory(cachingDataSourceFactory)
                    .createMediaSource(
                        SubtitleConfiguration.Builder(Uri.parse(it.getLink()))
                            .setMimeType(it.getFormat()).setLanguage(it.getLanguage())
                            .build(), C.TIME_UNSET
                    )
            }
            val mergingMediaSource =
                MergingMediaSource(*mediaSource.toTypedArray(), *subs.toTypedArray())
            player.addMediaSource(
                mergingMediaSource
            )
            player.playWhenReady = true
            player.prepare()
            delayedPlayBack?.let { handler.removeCallbacks(it) }
            delayedPlayBack = handler.postDelayed(600) {
                weakReference.get()?.playerView?.player?.play()
                viewModel.getPlaybackTime().value?.let {
                    player.seekTo(it)
                }
            }

        }
    }

    private fun addSubtitle(commonSubtitle: List<CommonSubtitle>) {
        if (commonSubtitle.isNotEmpty()) {
            subtitleSources.clear()
            subtitleSources.addAll(commonSubtitle)
            playMedia()
        }
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
                LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE),
                StandaloneDatabaseProvider(this)
            )
            downloadCache = downloadCacheLocal
        }
        return downloadCacheLocal
    }

    private fun initialize() {
        initPlayer()
        initObservers()
    }

    private fun initializeWebView() {
        binding.webView.videoLinksCallback = {
            viewModel.handleVideoLinks(it)
        }
    }

    private fun destroyWebView() {
        binding.webView.videoLinksCallback = null
    }

    private fun fetchRemote() {
        viewModel.fetchEpisode(args.animeSlugId, args.episodeSlugId)
    }

    private fun initLoadingState() {
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
                }
            }
        }
    }

    private fun initObservers() {
        initLoadingState()
        viewModel.getIsPlaying().observe(this) {
            if (it) binding.playerView.player?.play()
        }

        viewModel.getEpisodeWithAnime(args.episodeSlugId, args.animeSlugId)
            .observe(this) { item ->
                val episode = item?.first
                val anime = item?.second
                if (episode != null && anime != null) {
                    val title = buildString {
                        append("Episode ")
                        append(episode.name)
                    }
                    setAppbarEpisodeNumber(binding, title)
                    binding.episodeDetails = episode
                    playerViewUiHelper.subtitle.text = title
                    playerViewUiHelper.enableNextPrevEpisodeButtons(episode.next, episode.prev)
                    binding.animeDetails = anime
                    binding.collapsingToolbar.title = anime.name
                    playerViewUiHelper.title.text = anime.name
                    initDetails(episode, anime)
                }
            }

        var oldUrl: String? = null
        viewModel.getCurrentServer().observe(this) {
            if (oldUrl == null || oldUrl != it) {
                mediaSource.clear()
                binding.webView.loadUrl(it)
                oldUrl = it
            }
        }

        viewModel.getVideoLink().observe(this) { videoLinks ->
            if (videoLinks.isNotEmpty()) {
                loadLink(videoLinks[0])
            }
        }

        viewModel.getSubtitle().observe(this) {
            addSubtitle(it)
        }
    }


    private fun initDetails(episode: EpisodeEntity, anime: AnimeEntity) {
        binding.episodeDetailsContainer.anime.setOnClick {
            startActivity(
                AnimeActivity.newInstance(this, anime)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            )
        }
        playerViewUiHelper.next.setOnClick {
            openNextEpisode(episode)
        }
        playerViewUiHelper.prev.setOnClick {
            openPrevEpisode(episode)
        }
        binding.episodeDetailsContainer.next.setOnClick {
            openNextEpisode(episode)
        }
        binding.episodeDetailsContainer.previous.setOnClick {
            openPrevEpisode(episode)
        }
        binding.episodeDetailsContainer.mal.setOnClick {
            openLink(
                "https://myanimelist.net/anime/${
                    episode.episodeSlugId
                }"
            )
        }
        binding.episodeDetailsContainer.links.setOnClick {
            val link = viewModel.getVideoLink().value
            if (link != null) {
                onSelectStream { openLink(it.getLink()) }
            } else {
                Toast.makeText(this, "No Link was found", Toast.LENGTH_SHORT).show()
            }
        }
        initDropDown()
    }


    private fun initDropDown() {
        viewModel.getServersLinks().observe(this) { links ->
            if (links.isNotEmpty()) {
                val list = links.toTypedArray()
                val serverNames = links.map { it.serverName }.toTypedArray()
                binding.episodeDetailsContainer.servers.setSimpleItems(serverNames)
                binding.episodeDetailsContainer.servers.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, position, _ ->
                        viewModel.setCurrentServer(
                            list[position].link
                        )
                    }
                binding.episodeDetailsContainer.servers.setText(list[0].serverName)
                viewModel.setCurrentServer(list[0].link)
            } else if (links !is LinkedHashSet) {
                showError(Exception("No servers found"), this)
            }
        }
    }

    private fun openLink(link: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        try {
            startActivity(browserIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No activity found to open link $link", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun openNextEpisode(episode: EpisodeEntity) {
        val next = episode.next
        if (next != null) {
            openEpisode(next)
        } else {
            Toast.makeText(this, "No Next Episode", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPrevEpisode(episode: EpisodeEntity) {
        val prev = episode.prev
        if (prev != null) {
            openEpisode(prev)
        } else {
            Toast.makeText(this, "No Previous Episode", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openEpisode(episodeSlug: String) {
        // todo
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        releasePlayer()
        viewModel.clearServers()
        showLoading()
        intent.extras?.let { args = EpisodeActivityArgs.fromBundle(it) }
        fetchRemote()
        initPlayer()
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

    private fun onSelectStream(operation: (link: CommonVideoLink) -> Unit) {
        val chooseStream = AlertDialog.Builder(this)
        val streamList = viewModel.getVideoLink().value ?: return
        chooseStream.setTitle("Choose Stream")
        chooseStream.setItems(streamList.map {
            try {
                Uri.parse(it.getLink()).host ?: "Kick Server"
            } catch (e: NullPointerException) {
                "Kick Server"
            }
        }.toTypedArray()) { _, which ->
            operation(streamList[which])
        }
        chooseStream.create()?.show()
    }


    private fun showPlayerError(error: Exception) {
        showError(error, this@EpisodeActivity, "retry") {
            binding.playerView.player?.prepare()
        }
    }

    companion object {
        @JvmStatic
        private var downloadCache: SimpleCache? = null

        private const val MAX_CACHE_SIZE: Long = 2000000000

    }
}