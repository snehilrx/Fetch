package com.otaku.kickassanime.page.episodepage

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.otaku.fetch.base.livedata.State
import com.otaku.fetch.base.ui.BindingActivity
import com.otaku.kickassanime.R
import com.otaku.kickassanime.Strings.KICKASSANIME_URL
import com.otaku.kickassanime.api.model.Maverickki
import com.otaku.kickassanime.databinding.ActivityEpisodeBinding
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.utils.Utils.showError
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EpisodeActivity : BindingActivity<ActivityEpisodeBinding>(R.layout.activity_episode) {

    private lateinit var episodeLiveData: LiveData<EpisodeEntity?>
    private lateinit var animeLiveData: LiveData<AnimeEntity?>

    private lateinit var menu: BottomSheetDialog
    private val viewModel: EpisodeViewModel by viewModels()

    private lateinit var args: EpisodeActivityArgs

    override fun onBind(binding: ActivityEpisodeBinding, savedInstanceState: Bundle?) {
        args = navArgs<EpisodeActivityArgs>().value
        initAppbar(
            binding.appbarImageView,
            binding.toolbar
        )
        initialize(savedInstanceState)
        if (savedInstanceState == null) initializeWebView()
        setUpVideoBottomSheet()
        setTransparentStatusBar()
        showBackButton()
    }

    override fun onPause() {
        super.onPause()
        binding.videoPlayerContent.getPlayerAsync(this, this) { player ->
            player.allowBackgroundAudio(false)
            player.pause()
            player.stop()
        }
    }

    private fun initializePlayer(url: String, previewThumbnail: Caption? = null) {
        binding.videoPlayerContent.getPlayerAsync(this, this) { jwPlayer ->
            if (jwPlayer.playlist == null || jwPlayer.playlist.isEmpty()) {
                KeepScreenOnHandler(jwPlayer, window)
                val playlist = playlistItem(url, previewThumbnail)
                val config = PlayerConfig.Builder()
                    .uiConfig(UiConfig.Builder().displayAllControls().build())
                    .autostart(true)
                    .playlist(mutableListOf(playlist))
                    .thumbnailPreview(PlayerConfig.THUMBNAIL_PREVIEW_IMAGE)
                    .allowCrossProtocolRedirects(true)
                    .build()
                jwPlayer.setup(config)
                jwPlayer.allowBackgroundAudio(false)
                setOnMenuClickListener(jwPlayer)
                val jwPlayerCompat = JWPlayerCompat(jwPlayer)
                jwPlayerCompat.addOnReadyListener {
                    if (this.isDestroyed) {
                        jwPlayer.pause()
                    }
                }
                jwPlayerCompat.addOnFirstFrameListener {
                    binding.progress = 100
                }
                jwPlayerCompat.addOnErrorListener {
                    binding.progress = 100
                    showError(it.exception, this)
                }
            } else {
                jwPlayer.playlist[0].sources.add(MediaSource.Builder().file(url).build())
            }
        }
    }

    private fun playlistItem(
        url: String,
        previewThumbnail: Caption?
    ): PlaylistItem? {
        return PlaylistItem.Builder()
            .title(args.title)
            .sources(mutableListOf((MediaSource.Builder().file(url).build())))
            .tracks(if (previewThumbnail == null) emptyList() else listOf(previewThumbnail))
            .image(KICKASSANIME_URL + "uploads/" + binding.animeDetails?.image)
            .description(binding.animeDetails?.enTitle)
            .mediaId(binding.animeDetails?.animeslug)
            .build()
    }

    private fun setOnMenuClickListener(jwPlayer: JWPlayer) {
        (jwPlayer.getViewModelForUiGroup(UiGroup.SETTINGS_MENU) as SettingsMenuViewModel).isUiLayerVisible.observe(
            this
        ) {
            if (it) {
                menu.show()
            } else {
                menu.hide()
            }
        }
    }

    private fun setUpVideoBottomSheet() {
        val menuView = binding.videoPlayerContent.controlsContainer.menuView
        (menuView.parent as ViewGroup).removeView(menuView)
        menu = BottomSheetDialog(this, R.style.Theme_KickassAnime_VideoMenuBottomSheet)
        menu.setContentView(menuView)
        menu.window
            ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundResource(android.R.color.transparent)
        menuView.isVisible = true
        menuView.layoutParams = menuView.layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    private fun initialize(savedInstanceState: Bundle?) {
        fetchRemote()
        viewModel.getLoadState().observe(this) {
            when (it) {
                is State.FAILED -> TODO()
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

    override fun onBackPressed() {
        if (binding.videoPlayerContent.player?.fullscreen == true)
            binding.videoPlayerContent.player?.setFullscreen(false, true)
        else
            super.onBackPressed()
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
            initializePlayer(it)
        }
        viewModel.getMaverickkiVideo().observe(this) {
            val previewThumbnail = Caption.Builder()
                .file(Maverickki.BASE_URL + it.timelineThumbnail)
                .kind(CaptionType.THUMBNAILS)
                .build()
            initializePlayer(Maverickki.BASE_URL + it.hls, previewThumbnail)
        }
    }

    private fun initLiveData() {
        if(this::episodeLiveData.isInitialized) episodeLiveData.removeObservers(this)
        if(this::animeLiveData.isInitialized) animeLiveData.removeObservers(this)
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewModel.removeObservers(this)
        showLoading()
        intent?.extras?.let { args = EpisodeActivityArgs.fromBundle(it) }
        if (this::episodeLiveData.isInitialized) episodeLiveData.removeObservers(this)
        if (this::animeLiveData.isInitialized) animeLiveData.removeObservers(this)
        initialize(null)
    }


    override fun onDestroy() {
        destroyWebView()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putString("player_state", binding.videoPlayerContent.player.state.name)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        initializeWebView()
        savedInstanceState.getString("player_state")?.let {
            when (PlayerState.valueOf(it)) {
                PlayerState.PLAYING -> binding.videoPlayerContent.player.play()
                PlayerState.BUFFERING -> binding.progress = -1
                PlayerState.PAUSED -> binding.videoPlayerContent.player.pause()
                PlayerState.COMPLETE -> binding.progress = 100
                else -> {}
            }
        }
        binding.videoPlayerContent.player?.play()
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
}