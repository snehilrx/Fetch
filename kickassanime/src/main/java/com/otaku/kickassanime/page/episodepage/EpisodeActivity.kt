package com.otaku.kickassanime.page.episodepage

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.widget.*
import androidx.activity.viewModels
import androidx.compose.ui.text.intl.Locale
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.postDelayed
import androidx.core.view.*
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.media3.cast.CastPlayer
import androidx.media3.common.*
import androidx.media3.common.MediaItem.SubtitleConfiguration
import androidx.media3.common.util.RepeatModeUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.*
import androidx.media3.datasource.okhttp.OkHttpDataSource
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
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.maxkeppeler.sheets.info.InfoSheet
import com.otaku.fetch.ModuleRegistry
import com.otaku.fetch.base.TAG
import com.otaku.fetch.base.download.DownloadUtils
import com.otaku.fetch.base.livedata.State
import com.otaku.fetch.base.settings.Settings
import com.otaku.fetch.base.settings.dataStore
import com.otaku.fetch.base.ui.BindingActivity
import com.otaku.fetch.base.ui.setOnClick
import com.otaku.fetch.base.utils.UiUtils.loadBitmapFromUrl
import com.otaku.fetch.base.utils.UiUtils.showError
import com.otaku.fetch.base.utils.UiUtils.statusBarHeight
import com.otaku.kickassanime.R
import com.otaku.kickassanime.Strings
import com.otaku.kickassanime.api.model.CommonSubtitle
import com.otaku.kickassanime.api.model.ServerLinks
import com.otaku.kickassanime.databinding.ActivityEpisodeBinding
import com.otaku.kickassanime.db.models.CommonVideoLink
import com.otaku.kickassanime.db.models.TimestampType
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.page.animepage.AnimeActivity
import com.otaku.kickassanime.utils.Constants.headersMap
import com.otaku.kickassanime.utils.OfflineSubsHelper
import com.otaku.kickassanime.utils.Quality
import com.otaku.kickassanime.utils.Utils.binarySearchGreater
import com.otaku.kickassanime.utils.slugToEpisodeLink
import com.otaku.kickassanime.work.DownloadAllEpisodeTask
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.abs


@UnstableApi
@AndroidEntryPoint
class EpisodeActivity : BindingActivity<ActivityEpisodeBinding>(R.layout.activity_episode) {

    private lateinit var castHelper: CastHelper
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
    @Inject
    lateinit var cache: Cache

    @Inject
    @Named("unlimited")
    lateinit var offlineCache: Cache

    @Inject
    lateinit var offlineSubsHelper: OfflineSubsHelper

    private val offlineCachingDataSourceFactory by lazy {
        return@lazy CacheDataSource.Factory()
            .setCache(offlineCache).setCacheWriteDataSinkFactory(null)
            .setCacheReadDataSourceFactory(FileDataSource.Factory())
            .setUpstreamDataSourceFactory(null)
            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE)
    }

    private val onlineCachingDataSourceFactory by lazy {
        val cacheSink = CacheDataSink.Factory().setCache(cache)
        val upstreamFactory = DefaultDataSource.Factory(
            applicationContext,
            OkHttpDataSource.Factory(okhttp).setDefaultRequestProperties(headersMap)
        )
        return@lazy CacheDataSource.Factory().setCache(cache)
            .setCacheWriteDataSinkFactory(cacheSink)
            .setCacheReadDataSourceFactory(FileDataSource.Factory())
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    private val onlineHlsMediaSource by lazy {
        HlsMediaSource.Factory(onlineCachingDataSourceFactory)
    }
    private val onlineDashMediaSource by lazy {
        DashMediaSource.Factory(onlineCachingDataSourceFactory)
    }

    private val mediaSource = ArrayList<MediaSource>()
    private val subtitleSources = ArrayList<CommonSubtitle>()
    private lateinit var playerListener: PlayerListener
    // endregion

    @Inject
    lateinit var okhttp: OkHttpClient

    @Inject
    lateinit var downloadUtils: DownloadUtils

    override fun onBind(binding: ActivityEpisodeBinding, savedInstanceState: Bundle?) {
        args = navArgs<EpisodeActivityArgs>().value
        freeWebView()
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
        setTransparentStatusBar()
        showBackButton()
        statusBarHeight {
            runOnUiThread {
                binding.appbarLayout.setPaddingRelative(0, it, 0, 0)
            }
        }
    }

    private fun freeWebView() {
        val webView =
            ModuleRegistry.modules[Strings.KICKASSANIME]?.appModule?.webView as? CustomWebView
        webView?.release()
    }

    private fun isFullscreen(): Boolean {
        return binding.playerView.parent != binding.aspectRatioFrameLayout
    }

    private fun getTrackSelector(preferences: Preferences): DefaultTrackSelector {
        val videoQuality = preferences[Settings.DOWNLOADS_VIDEO_QUALITY]?.let { quality ->
            Quality.entries[quality.toIntOrNull() ?: 0].bitrate
        } ?: Quality.MAX.bitrate
        return DefaultTrackSelector(applicationContext, AdaptiveTrackSelection.Factory()).apply {
            setParameters(
                DefaultTrackSelector.Parameters.Builder(applicationContext)
                    .apply {
                        setAllowVideoMixedMimeTypeAdaptiveness(true)
                        clearOverridesOfType(C.TRACK_TYPE_TEXT)
                        setPreferredTextLanguage(null)
                        setPreferredTextLanguages(
                            preferences[Settings.SUBTITLE_LANGUAGE] ?: Locale.current.language
                        )
                        setPreferredAudioLanguages(
                            preferences[Settings.SUBTITLE_LANGUAGE] ?: Locale.current.language
                        )
                        setMaxVideoBitrate(videoQuality)
                        setMinVideoBitrate(videoQuality - 1000)
                    }
            )
        }
    }

    private fun initPlayer(preferences: Preferences) {
        releasePlayer(binding.playerView.player)
        if (this::castHelper.isInitialized) {
            castHelper.release()
        }
        val player =
            ExoPlayer.Builder(applicationContext)
                .setTrackSelector(getTrackSelector(preferences))
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(applicationContext)
                        .setDataSourceFactory(onlineCachingDataSourceFactory)
                ).build()

        castContext = try {
            CastContext.getSharedInstance()
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
        binding.playerView.artworkDisplayMode = ARTWORK_DISPLAY_MODE_FILL
        viewModel.getPlaybackTime().observe(this) {
            if (it != null) {
                binding.playerView.player?.seekTo(it)
            }
        }
        initPlayerControls()

        binding.playerView.setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE)
        playerListener = PlayerListener(binding, viewModel, this::showPlayerError)
        player.addListener(playerListener)
        playerViewUiHelper.showLoading()
    }

    private fun initCastHelper(player: ExoPlayer) {
        val playerView = binding.playerView
        if (this::castHelper.isInitialized) {
            castHelper.release()
        }
        castHelper = CastHelper(castContext, player) { currentPlayer ->
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
        playerView.player = player
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
                                skipIntroButton.text = getString(R.string.skip).format(type)
                                skipIntroButton.isVisible = getSkipButtonVisibility(type)
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

    private fun getSkipButtonVisibility(type: String) = when (type) {
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
        val player = binding.playerView.player
        releasePlayer(player)
        castHelper.release()
        super.onDestroy()
    }

    private fun releasePlayer(player: Player?) {
        if (player != null) {
            playerListener.releaseReferences()
            player.removeListener(playerListener)
            player.release()
        }
    }

    private fun loadLink(item: CommonVideoLink) {
        Log.i(TAG, "DataLink ${item.getLink()}")
        val mediaItem = MediaItem.Builder().setMediaMetadata(
            MediaMetadata.Builder().setTitle("${args.title} ${binding.episodeDetails?.title}")
                .build()
        ).setUri(item.getLink()).setMimeType(
            when (item.getVideoType()) {
                CommonVideoLink.HLS -> MimeTypes.APPLICATION_M3U8
                CommonVideoLink.DASH -> MimeTypes.APPLICATION_MPD
                else -> return
            }
        ).build()
        mediaSource.add(
            when (item.getVideoType()) {
                CommonVideoLink.HLS -> onlineHlsMediaSource.createMediaSource(mediaItem)
                CommonVideoLink.DASH -> onlineDashMediaSource.createMediaSource(mediaItem)
                else -> return
            }
        )
        playMedia()
    }

    private fun playMedia() {
        (binding.playerView.player as? ExoPlayer)?.let { player ->
            player.clearMediaItems()
            val onlineSubs = subtitleSources.filter { it.getLink().isNotEmpty() }.map {
                SingleSampleMediaSource.Factory(onlineCachingDataSourceFactory).createMediaSource(
                    SubtitleConfiguration.Builder(Uri.parse(it.getLink()))
                        .setMimeType(it.getFormat()).setLanguage(it.getLanguage()).build(),
                    C.TIME_UNSET
                )
            }
            val offlineSubs = offlineSubsHelper.loadSubs(
                args.animeSlug, args.episodeSlug, onlineCachingDataSourceFactory
            ) ?: emptyList()
            val mergingMediaSource = MergingMediaSource(
                *mediaSource.toTypedArray(), *(onlineSubs + offlineSubs).toTypedArray()
            )
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
        lifecycleScope.launch {
            dataStore.data.collectLatest {
                initPlayer(it)
                cancel()
            }
        }
        initObservers()
    }

    private fun fetchRemote() {
        viewModel.fetchEpisode(
            args.animeSlug, args.episodeSlug, applicationContext.dataStore
        )
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

        viewModel.getCurrentServer().observe(this) {
            mediaSource.clear()
            when (it) {
                is ServerLinks.OfflineServerLink -> {
                    binding.episodeDetailsContainer.download.isEnabled = false
                    mediaSource.clear()
                    mediaSource.add(
                        DownloadHelper.createMediaSource(
                            it.download.request,
                            offlineCachingDataSourceFactory
                        )
                    )
                    playMedia()
                }

                is ServerLinks.OnlineServerLink -> {
                    viewModel.loadPage(it.link)
                }

                else -> {
                    Log.e("Episode", "Invalid server link")
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
        binding.episodeDetailsContainer.download.setOnClick {
            binding.playerView.player?.pause()
            binding.playerView.player?.playWhenReady = false
            val title = getString(R.string.downloading_episode)
            val successMessage = getString(R.string.your_requested_episode_is_being_downloaded)
            val successMessage1 =
                getString(R.string.meanwhile_we_will_switch_the_playback_server_to_offline)

            val progressDialog =
                ProgressDialog.show(this, title, getString(R.string.listing_out_video_files))
            val taskInfo = createEpisodeDownloadWork(episode)
            if (taskInfo == null) {
                progressDialog.hide()
                showError(getString(R.string.downloading_episode_failed), this)
            } else {
                taskInfo.observe(this) { info ->
                    when (info.state) {
                        WorkInfo.State.ENQUEUED -> {
                            progressDialog.show()
                        }

                        WorkInfo.State.RUNNING -> {
                            progressDialog.show()
                        }

                        WorkInfo.State.SUCCEEDED -> {
                            progressDialog.hide()
                            lifecycleScope.launch {
                                delay(200)
                                InfoSheet().show(this@EpisodeActivity) {
                                    title(title)
                                    content(buildString {
                                        append(successMessage)
                                        append(anime.name)
                                        append(" ")
                                        append(episode.episodeNumber)
                                        append(successMessage1)
                                    })
                                    displayPositiveButton(true)
                                    onPositive("OK") {
                                        switchToOfflineMode(episode.episodeSlug)
                                        dismiss()
                                    }
                                }
                            }
                        }

                        WorkInfo.State.FAILED -> {
                            progressDialog.hide()
                            showError(getString(R.string.downloading_episode_failed), this)
                        }

                        else -> {
                            // no op
                        }
                    }
                }
            }
            binding.playerView.player?.pause()
        }
        initDropDown()
    }

    private fun switchToOfflineMode(episodeSlug: String) {
        viewModel.checkOfflineServers(episodeSlug)
    }

    private fun createEpisodeDownloadWork(episode: EpisodeEntity): LiveData<WorkInfo>? {
        val animeSlug = episode.animeSlug ?: return null
        val episodeSlug = episode.episodeSlug
        val request = OneTimeWorkRequest.Builder(DownloadAllEpisodeTask::class.java).setInputData(
            DownloadAllEpisodeTask.createNewInput(
                arrayOf(episodeSlug.slugToEpisodeLink(animeSlug)),
                arrayOf(episodeSlug),
                animeSlug
            )
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(networkType = NetworkType.CONNECTED).build()
        ).build()
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueueUniqueWork(
            episode.episodeSlug,
            ExistingWorkPolicy.KEEP,
            request
        )
        return workManager.getWorkInfoByIdLiveData(request.id)
    }


    private fun initDropDown() {
        viewModel.getServersLinks().observe(this) { links ->
            if (links.isNotEmpty()) {
                val list = links.toTypedArray()
                val serverNames = links.map { it.serverName }.toTypedArray()
                binding.episodeDetailsContainer.servers.setSimpleItems(serverNames)
                binding.episodeDetailsContainer.servers.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, position, _ ->
                        viewModel.setCurrentServer(list[position])
                    }
                // first link will always be offline first
                binding.episodeDetailsContainer.servers.setText(list[0].serverName)
                viewModel.setCurrentServer(list[0])
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
            Toast.makeText(applicationContext, "No Next Episode", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPrevEpisode(episode: EpisodeEntity) {
        val prev = episode.prev
        if (prev != null) {
            openEpisode(prev)
        } else {
            Toast.makeText(applicationContext, "No Previous Episode", Toast.LENGTH_SHORT).show()
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
        releasePlayer(binding.playerView.player)
        viewModel.clearServers()
        showLoading()
        intent.extras?.let { args = EpisodeActivityArgs.fromBundle(it) }
        fetchRemote()
        lifecycleScope.launch {
            dataStore.data.collectLatest {
                initPlayer(it)
                cancel()
            }
        }
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
        AlertDialog.Builder(this).setTitle("Choose Stream").setItems(streamList.map {
            it.getLinkName()
        }.toTypedArray()) { _, which ->
            operation(streamList[which])
        }.create()?.show()
    }


    private fun showPlayerError(error: Exception) {
        showError(error, this@EpisodeActivity, "retry") {
            binding.playerView.player?.prepare()
        }
    }
}