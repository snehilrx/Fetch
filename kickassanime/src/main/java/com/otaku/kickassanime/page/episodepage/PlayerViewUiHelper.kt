package com.otaku.kickassanime.page.episodepage

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.view.*
import android.widget.*
import androidx.annotation.Dimension
import androidx.annotation.GravityInt
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import com.anggrayudi.materialpreference.PreferenceManager
import com.google.android.material.textview.MaterialTextView
import com.otaku.fetch.base.media.TrackSelectionDialog
import com.otaku.fetch.base.ui.setOnClick
import com.otaku.fetch.base.utils.UiUtils.toPxInt
import com.otaku.kickassanime.R
import com.otaku.kickassanime.page.settings.Pref


@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
internal class PlayerViewUiHelper(
    activity: EpisodeActivity,
    playerView: PlayerView,
    fullscreenContainer: AspectRatioFrameLayout,
    isFullscreen: Boolean
) {

    val skipIntroButton: Button
    private val chooseServerButton: Button
    val title: TextView
    val subtitle: TextView
    val next: View
    val prev: View
    private val progress: ProgressBar

    var onSelectStream: (() -> Unit)? = null

    private val mFullScreenDialog: Dialog =
        Dialog(playerView.context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    private val controlsContainer: ViewGroup =
        playerView.children.filter { it is PlayerControlView }.first() as ViewGroup

    init {
        val skipContainer =
            playerView.findViewById<FrameLayout>(androidx.media3.ui.R.id.exo_ad_overlay)
        skipIntroButton = createButton("Skip", skipContainer.context, Gravity.BOTTOM)
        chooseServerButton = createButton("Choose Server", controlsContainer.context, Gravity.TOP)

        skipContainer.addView(skipIntroButton)
        skipIntroButton.isVisible = false
        controlsContainer.addView(chooseServerButton)
        chooseServerButton.setOnClick {
            onSelectStream?.invoke()
        }

        // enabling full screen feature
        val fullscreen = playerView.findViewById<View>(androidx.media3.ui.R.id.exo_fullscreen)
        fullscreen.isVisible = true
        playerView.setFullscreenButtonClickListener {
            if (it)
                enterFullScreen(activity, playerView)
            else
                exitFullScreen(activity, playerView, fullscreenContainer)
        }
        // exist fullscreen when device back button is pressed
        mFullScreenDialog.setOnDismissListener {
            exitFullScreen(activity, playerView, fullscreenContainer)
        }

        // configuring track selection
        val settingButton =
            playerView.findViewById<ImageButton>(androidx.media3.ui.R.id.exo_settings)
        settingButton.setOnClick {
            playerView.player?.let { it1 ->
                TrackSelectionDialog.createForPlayer(it1) {
                    // on dismiss no-op
                }.show(activity)
            }
        }

        playerView.setShowSubtitleButton(true)

        if (activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT && isFullscreen) {
            exitFullScreen(activity, playerView, fullscreenContainer)
        } else if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && !isFullscreen) {
            enterFullScreen(activity, playerView)
        }

        // Setting title and subtitle
        title = createTitle(controlsContainer.context)
        subtitle = createSubTitle(controlsContainer.context)
        val linearLayout = createTitleLayout(controlsContainer.context, title, subtitle)
        controlsContainer.addView(linearLayout)
        next = playerView.findViewById(androidx.media3.ui.R.id.exo_next)
        prev = playerView.findViewById(androidx.media3.ui.R.id.exo_prev)

        val setting = PreferenceManager.getDefaultSharedPreferences(activity)
        playerView.subtitleView?.setStyle(
            CaptionStyleCompat(
                setting.getInt(
                    Pref.SUBTITLE_FOREGROUND_COLOR,
                    activity.getColor(R.color.subtitleForgroundColor)
                ),
                setting.getInt(
                    Pref.SUBTITLE_BACKGROUND_COLOR,
                    activity.getColor(R.color.subtitleBackgroundColor)
                ),
                Color.TRANSPARENT,
                CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                setting.getInt(
                    Pref.SUBTITLE_EDGE_COLOR,
                    activity.getColor(R.color.subtitleEdgeColor)
                ),
                Typeface.DEFAULT_BOLD
            )
        )

        playerView.subtitleView?.setPadding(
            0, 0, 0,
            activity.resources.getDimension(com.lapism.search.R.dimen.search_dp_8)
                .toInt()
        )
        playerView.subtitleView?.setFixedTextSize(
            Dimension.SP,
            24f
        )
        progress = playerView.findViewById(androidx.media3.ui.R.id.exo_buffering)
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
    }

    private fun createButton(text: String, context: Context, @GravityInt gravityV: Int): Button {
        val button = Button(context)
        button.isVisible = true
        button.text = text
        button.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 24.toPxInt
            rightMargin = 24.toPxInt
            bottomMargin = 24.toPxInt
            gravity = gravityV or Gravity.END
        }
        return button
    }


    @SuppressLint("SourceLockedOrientationActivity")
    fun exitFullScreen(
        activity: EpisodeActivity,
        playerView: PlayerView,
        fullscreenContainer: AspectRatioFrameLayout
    ) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        (playerView.parent as ViewGroup).removeView(playerView)
        fullscreenContainer.addView(
            playerView,
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
        val window = mFullScreenDialog.window
        val decorView = window?.decorView ?: return
        val windowInsetsController = WindowCompat.getInsetsController(window, decorView)
        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            view.onApplyWindowInsets(windowInsets)
        }

    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        val window = mFullScreenDialog.window
        val decorView = window?.decorView ?: return
        val windowInsetsController = WindowCompat.getInsetsController(window, decorView)
        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            view.onApplyWindowInsets(windowInsets)
        }
    }

    @SuppressLint("PrivateResource")
    fun enterFullScreen(
        activity: EpisodeActivity,
        playerView: PlayerView
    ) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        (playerView.parent as ViewGroup).removeView(playerView)
        mFullScreenDialog.addContentView(
            playerView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        playerView.findViewById<ImageButton>(androidx.media3.ui.R.id.exo_fullscreen)
            ?.setImageDrawable(
                ContextCompat.getDrawable(
                    playerView.context,
                    androidx.media3.ui.R.drawable.exo_ic_fullscreen_exit
                )
            )
        mFullScreenDialog.show()
        hideSystemUI()
    }

    private fun createTitleLayout(context: Context, title: TextView, subtitle: TextView): View {
        val linearLayout = LinearLayout(context)
        title.setPadding(24.toPxInt, 24.toPxInt, 0, 0)
        subtitle.setPadding(24.toPxInt, 16.toPxInt, 0, 0)
        linearLayout.addView(subtitle)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.addView(title)
        linearLayout.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 24.toPxInt
            rightMargin = 24.toPxInt
            gravity = Gravity.START or Gravity.TOP
        }
        return linearLayout
    }

    private fun createTitle(context: Context): TextView {
        val materialTextView = MaterialTextView(context)
        materialTextView.setTextAppearance(R.style.TextAppearance_Video_SubTitle)
        return materialTextView
    }

    private fun createSubTitle(context: Context): TextView {
        val materialTextView = MaterialTextView(context)
        materialTextView.setTextAppearance(R.style.TextAppearance_Video_Title)
        return materialTextView
    }

    fun enableNextPrevEpisodeButtons(next: String?, prev: String?) {
        this.prev.isEnabled = prev != null
        this.next.isEnabled = next != null
    }

    fun showLoading() {
        progress.isVisible = true
    }

}