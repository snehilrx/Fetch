package com.otaku.kickassanime.page.episodepage

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.postDelayed
import androidx.core.view.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.cast.CastPlayer
import androidx.media3.common.*
import androidx.media3.common.MediaItem.SubtitleConfiguration
import androidx.media3.common.util.RepeatModeUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.*
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.source.*
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView.ARTWORK_DISPLAY_MODE_FILL
import androidx.navigation.navArgs
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.otaku.fetch.base.TAG
import com.otaku.fetch.base.download.DownloadUtils
import com.otaku.fetch.base.download.changeOrigin
import com.otaku.fetch.base.download.toMediaUri
import com.otaku.fetch.base.livedata.State
import com.otaku.fetch.base.settings.Settings
import com.otaku.fetch.base.settings.dataStore
import com.otaku.fetch.base.ui.BindingActivity
import com.otaku.fetch.base.ui.setOnClick
import com.otaku.fetch.base.utils.UiUtils.loadBitmapFromUrl
import com.otaku.fetch.base.utils.UiUtils.showError
import com.otaku.fetch.base.utils.UiUtils.statusBarHeight
import com.otaku.kickassanime.R
import com.otaku.kickassanime.api.model.CommonSubtitle
import com.otaku.kickassanime.databinding.ActivityEpisodeBinding
import com.otaku.kickassanime.db.models.CommonVideoLink
import com.otaku.kickassanime.db.models.TimestampType
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.page.animepage.AnimeActivity
import com.otaku.kickassanime.utils.Utils.binarySearchGreater
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject
import kotlin.math.abs


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

    private val useOfflineMode by lazy { args.mediaItem != null }

    // region Media stuff
    @Inject
    lateinit var cache: Cache

    private val headersMap: Map<String, String> = mapOf("origin" to "https://kaavid.com")

    private val cachingDataSourceFactory by lazy {
        val cacheSink = if (useOfflineMode) {
            null
        } else {
            CacheDataSink.Factory().setCache(cache)
        }
        val upstreamFactory = if (useOfflineMode) {
            null
        } else {
            DefaultDataSource.Factory(
                this,
                OkHttpDataSource.Factory(okhttp.changeOrigin())
                    .setDefaultRequestProperties(headersMap)
            )
        }
        return@lazy CacheDataSource.Factory().setCache(cache)
            .setCacheWriteDataSinkFactory(cacheSink)
            .setCacheReadDataSourceFactory(FileDataSource.Factory())
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
    private val hlsMediaSource by lazy {
        HlsMediaSource.Factory(cachingDataSourceFactory)
    }
    private val dashMediaSource by lazy {
        DashMediaSource.Factory(cachingDataSourceFactory)
    }
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

    @Inject
    lateinit var downloadUtils: DownloadUtils

    override fun onBind(binding: ActivityEpisodeBinding, savedInstanceState: Bundle?) {
        args = navArgs<EpisodeActivityArgs>().value
        fetchRemote()
        playerViewUiHelper = PlayerViewUiHelper(
            this, binding.playerView, binding.aspectRatioFrameLayout, isFullscreen()
        )
        playerViewUiHelper.onSelectStream = {
            onSelectStream {
                loadLink(it)
            }
        }
        initAppbar(
            binding.appbarImageView, binding.toolbar
        )
        initialize()
        initializeWebView()
        setTransparentStatusBar()
        showBackButton()
        statusBarHeight {
            runOnUiThread {
                binding.appbarLayout.setPaddingRelative(0, it, 0, 0)
            }
        }
    }

    private fun isFullscreen(): Boolean {
        return binding.playerView.parent != binding.aspectRatioFrameLayout
    }

    private fun initPlayer() {
        val player =
            ExoPlayer.Builder(this)
                .setUsePlatformDiagnostics(true)
                .setTrackSelector(trackSelector)
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(this)
                        .setDataSourceFactory(cachingDataSourceFactory)
                )
                .build()

        initCastHelper(player)
        trackSelector.setParameters(
            trackSelector.buildUponParameters().setAllowVideoMixedMimeTypeAdaptiveness(true)
        )
        castContext = try {
            CastContext.getSharedInstance()
        } catch (e: RuntimeException) {
            Log.e(TAG, "creating media cast context failed", e)
            null
        }
        binding.playerView.setKeepContentOnPlayerReset(true)
        binding.animeDetails?.getImageUrl()?.let {
            loadBitmapFromUrl(
                it, this
            ) { bitmap ->
                binding.playerView.defaultArtwork = bitmap?.toDrawable(resources)
            }
        }
        binding.playerView.artworkDisplayMode = ARTWORK_DISPLAY_MODE_FILL
        viewModel.getPlaybackTime().observe(this) {
            if (it != null) {
                binding.playerView.player?.seekTo(it)
            }
        }
        initPlayerControls()

        binding.playerView.setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE)
        player.addListener(playerListener)
        playerViewUiHelper.showLoading()
        args.mediaItem?.toMediaUri()?.let { media ->
            downloadUtils.getDownloadTracker()
                .getDownloadRequest(media)
                ?.let {
                    player.addMediaSource(
                        DownloadHelper.createMediaSource(
                            it,
                            cachingDataSourceFactory
                        )
                    )
                    player.prepare()
                    player.play()
                }
        }
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
                        .setMimeType(it.getFormat()).setLanguage(it.getLanguage()).build()
                }
                val mediaItems = mediaSource.map {
                    it.mediaItem.buildUpon().setSubtitleConfigurations(subs).build()
                }
                currentPlayer.setMediaItems(
                    mediaItems,
                    oldPlayer?.currentMediaItemIndex ?: C.INDEX_UNSET,
                    oldPlayer?.currentPosition ?: C.TIME_UNSET
                )
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
                var oldPos = -1L
                override fun run() {
                    if (timestamps.isNotEmpty()) {
                        val currentPos = binding.playerView.player?.currentPosition ?: return
                        if (oldPos != currentPos) {
                            val ts = timestamps.binarySearchGreater { x ->
                                x.first.compareTo(currentPos)
                            }
                            val type = ts?.second
                            val skipIntroButton = playerViewUiHelper.skipIntroButton
                            if (type != null && abs(currentPos - ts.first) == 5000L) {
                                skipIntroButton.setOnClick {
                                    ts.first.let { binding.playerView.player?.seekTo(it) }
                                }
                                oldPos = currentPos
                                skipIntroButton.text =
                                    getString(R.string.skip).format(type)
                                skipIntroButton.isVisible = getSkipButtonVisiblity(type)
                            } else {
                                skipIntroButton.isVisible = false
                            }
                            skipIntroButton.animate().alpha(
                                if (skipIntroButton.isVisible) {
                                    1.0f
                                } else {
                                    0.0f
                                }
                            )
                        }
                    }
                    handler.postDelayed(this, 5000)
                }
            }
            timeSkipLoop?.let { handler.postDelayed(timeSkipLoop as Runnable, 2000) }
        }
    }

    private fun getSkipButtonVisiblity(type: String) = when (type) {
        TimestampType.INTRO.type -> true
        TimestampType.RECAP.type -> true
        TimestampType.CANON.type -> false
        TimestampType.MUST_WATCH.type -> false
        TimestampType.BRANDING.type -> false
        TimestampType.MIXED_INTRO.type -> true
        TimestampType.NEW_INTRO.type -> true
        TimestampType.FILLER.type -> true
        TimestampType.TRANSITION.type -> true
        TimestampType.CREDITS.type -> true
        TimestampType.MIXED_CREDITS.type -> true
        TimestampType.NEW_CREDITS.type -> true
        TimestampType.PREVIEW.type -> false
        TimestampType.TITLE_CARD.type -> false
        TimestampType.UNKNOWN.type -> false
        else -> false
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // If the event was not handled then see if the player view can handle it.
        return super.dispatchKeyEvent(event) || binding.playerView.dispatchKeyEvent(event)
    }

    override fun onPause() {
        super.onPause()
        timeSkipLoop?.let { handler.removeCallbacks(it) }
        binding.playerView.player?.currentPosition?.let {
            binding.episodeDetails?.episodeSlug?.let { episodeSlug ->
                viewModel.updatePlayBackTime(
                    episodeSlug, it
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
        val mediaItem = MediaItem.Builder().setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle("${args.title} ${binding.episodeDetails?.title}").build()
        ).setUri(item.getLink()).setMimeType(
            when (item.getVideoType()) {
                CommonVideoLink.HLS -> MimeTypes.APPLICATION_M3U8
                CommonVideoLink.DASH -> MimeTypes.APPLICATION_MPD
                else -> return
            }
        ).build()
        mediaSource.add(
            when (item.getVideoType()) {
                CommonVideoLink.HLS -> hlsMediaSource.createMediaSource(mediaItem)
                CommonVideoLink.DASH -> dashMediaSource.createMediaSource(mediaItem)
                else -> return
            }
        )
        playMedia()
    }

    private fun playMedia() {
        (binding.playerView.player as? ExoPlayer)?.let { player ->
            player.clearMediaItems()
            val subs = subtitleSources.filter { it.getLink().isNotEmpty() }.map {
                SingleSampleMediaSource.Factory(cachingDataSourceFactory).createMediaSource(
                    SubtitleConfiguration.Builder(Uri.parse(it.getLink()))
                        .setMimeType(it.getFormat()).setLanguage(it.getLanguage()).build(),
                    C.TIME_UNSET
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

    private fun initialize() {
        initPlayer()
        initObservers()
    }

    private fun initializeWebView() {
        binding.webView.videoLinksCallback = {
            viewModel.handleVideoLinks(it)
        }
        binding.webView.crunchyRollCallback = {
            viewModel.handleCrunchyRoll(it)
        }
    }

    private fun destroyWebView() {
        binding.webView.videoLinksCallback = null
        binding.webView.crunchyRollCallback = null
        binding.webView.webViewClient = object : WebViewClient() {}
        binding.webView.webChromeClient = null
        binding.webView.clearHistory()
        binding.webView.clearCache(true)
        binding.webView.destroy()
    }

    private fun fetchRemote() {
        val pref = this.dataStore.data
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                pref.collectLatest {
                    if (binding.webView.url == null)
                        viewModel.fetchEpisode(
                            args.animeSlug,
                            args.episodeSlug,
                            useOfflineMode,
                            it[Settings.SKIP_ENABLED] == true
                        )
                }
            }
        }
    }

    private fun initLoadingState() {
        viewModel.getLoadState().observe(this) {
            when (it) {
                is State.FAILED -> {
                    showError(it.exception, this)
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

        viewModel.getEpisodeWithAnime(args.episodeSlug, args.animeSlug).observe(this) { item ->
            val episode = item?.first
            val anime = item?.second
            if (episode != null && anime != null) {
                val title = buildString {
                    append("Episode ")
                    append(episode.episodeNumber?.toInt())
                }
                setAppbarEpisodeNumber(binding, title)
                binding.episodeDetails = episode
                playerViewUiHelper.subtitle.text = title
                playerViewUiHelper.enableNextPrevEpisodeButtons(episode.next, episode.prev)
                binding.animeDetails = anime
                binding.collapsingToolbar.title = anime.name
                playerViewUiHelper.title.text = episode.title
                initDetails(episode, anime)
            }
        }

        var oldUrl: String? = null
        if (!useOfflineMode) {
            viewModel.getCurrentServer().observe(this) {
                if (oldUrl == null || oldUrl != it) {
                    mediaSource.clear()
                    binding.webView.loadUrl(it)
                    oldUrl = it
                }
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
        binding.episodeDetailsContainer.links.setOnClick {
            val currentMediaItem = binding.playerView.player?.currentMediaItem
            if (currentMediaItem != null) {
                downloadUtils.getDownloadTracker().toggleDownload(
                    currentMediaItem.buildUpon()
                        .setSubtitleConfigurations(
                            subtitleSources.map {
                                return@map SubtitleConfiguration.Builder(Uri.parse(it.getLink()))
                                    .setMimeType(it.getFormat()).setLanguage(it.getLanguage())
                                    .build()
                            }
                        )
                        .setMediaId(episode.episodeSlug).build(),
                    DefaultRenderersFactory(this),
                    this
                )
            } else {
                showError("Not media is found (Media Item is null)", this)
            }
        }
        initDropDown()
    }


    private fun initDropDown() {
        if (useOfflineMode) {
            val offlineText = getString(R.string.offline)
            binding.episodeDetailsContainer.servers.setSimpleItems(arrayOf(offlineText))
            binding.episodeDetailsContainer.servers.setText(offlineText)
        }
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
        val episodeIntent = Intent(this, EpisodeActivity::class.java)
        val args = EpisodeActivityArgs(
            animeSlug = args.animeSlug, episodeSlug = episodeSlug, title = args.title
        )
        episodeIntent.putExtras(args.toBundle())
        episodeIntent.flags =
            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(episodeIntent)
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
        val streamList = viewModel.getVideoLink().value ?: return
        AlertDialog.Builder(this)
            .setTitle("Choose Stream")
            .setItems(streamList.map {
                it.getLinkName()
            }.toTypedArray()) { _, which ->
                operation(streamList[which])
            }
            .create()?.show()
    }


    private fun showPlayerError(error: Exception) {
        showError(error, this@EpisodeActivity, "retry") {
            binding.playerView.player?.prepare()
        }
    }
}