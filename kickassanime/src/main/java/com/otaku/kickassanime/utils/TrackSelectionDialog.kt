package com.otaku.kickassanime.utils

import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.media3.common.*
import androidx.media3.common.C.TrackType
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.R
import androidx.media3.ui.TrackSelectionView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.common.collect.ImmutableList
import com.maxkeppeler.sheets.core.Sheet
import com.otaku.kickassanime.databinding.TrackSelectionDialogBinding

/**
 * Dialog to select tracks.
 */
@UnstableApi
class TrackSelectionDialog : Sheet() {
    override fun onCreateLayoutView(): View {
        val binding = TrackSelectionDialogBinding.inflate(LayoutInflater.from(requireActivity()))
        val tabLayout = binding.trackSelectionDialogTabLayout
        val viewPager = binding.trackSelectionDialogViewPager
        viewPager.adapter = FragmentAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getTrackTypeString(resources, tabTrackTypes[position])
        }.attach()
        tabLayout.visibility =
            if (tabFragments.size() > 1) View.VISIBLE else View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonPositiveListener {
            onClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            dismiss()
        }
        onDismiss {
            onDismissListener?.onDismiss(dialog)
        }
    }

    fun show(context: Context): TrackSelectionDialog {
        windowContext = context
        this.show()
        return this
    }

    /**
     * Called when tracks are selected.
     */
    interface TrackSelectionListener {
        /**
         * Called when tracks are selected.
         *
         * @param trackSelectionParameters A [TrackSelectionParameters] representing the selected
         * tracks. Any manual selections are defined by [                                 ][TrackSelectionParameters.disabledTrackTypes] and [                                 ][TrackSelectionParameters.overrides].
         */
        fun onTracksSelected(trackSelectionParameters: TrackSelectionParameters?)
    }

    private val tabFragments: SparseArray<TrackSelectionViewFragment> = SparseArray()
    private val tabTrackTypes: ArrayList<Int> = ArrayList()
    private var onClickListener: DialogInterface.OnClickListener? = null
    private var onDismissListener: DialogInterface.OnDismissListener? = null

    private fun init(
        tracks: Tracks,
        trackSelectionParameters: TrackSelectionParameters,
        allowAdaptiveSelections: Boolean,
        allowMultipleOverrides: Boolean,
        onClickListener: DialogInterface.OnClickListener,
        onDismissListener: DialogInterface.OnDismissListener
    ) {
        for (i in SUPPORTED_TRACK_TYPES.indices) {
            val trackType = SUPPORTED_TRACK_TYPES[i]
            val trackGroups = ArrayList<Tracks.Group>()
            for (trackGroup in tracks.groups) {
                if (trackGroup.type == trackType) {
                    trackGroups.add(trackGroup)
                }
            }
            if (trackGroups.isNotEmpty()) {
                val tabFragment = TrackSelectionViewFragment()
                tabFragment.init(
                    trackGroups,
                    trackSelectionParameters.disabledTrackTypes.contains(trackType),
                    trackSelectionParameters.overrides,
                    allowAdaptiveSelections,
                    allowMultipleOverrides
                )
                tabFragments.put(trackType, tabFragment)
                tabTrackTypes.add(trackType)
            }
        }
        this.onClickListener = onClickListener
        this.onDismissListener = onDismissListener
    }

    /**
     * Returns whether the disabled option is selected for the specified track type.
     *
     * @param trackType The track type.
     * @return Whether the disabled option is selected for the track type.
     */
    fun getIsDisabled(trackType: Int): Boolean {
        val trackView = tabFragments[trackType]
        return trackView != null && trackView.isDisabled
    }

    /**
     * Returns the selected track overrides for the specified track type.
     *
     * @param trackType The track type.
     * @return The track overrides for the track type.
     */
    fun getOverrides(trackType: Int): Map<TrackGroup, TrackSelectionOverride> {
        val trackView = tabFragments[trackType]
        return trackView?.overrides ?: emptyMap()
    }

    private inner class FragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {


        override fun getItemCount(): Int {
            return tabTrackTypes.size
        }

        override fun createFragment(position: Int): TrackSelectionViewFragment = tabFragments[tabTrackTypes[position]]
    }

    /**
     * Fragment to show a track selection in tab of the track selection dialog.
     */
    class TrackSelectionViewFragment : Fragment(), TrackSelectionView.TrackSelectionListener {
        private lateinit var trackGroups: List<Tracks.Group>
        private var allowAdaptiveSelections = false
        private var allowMultipleOverrides = false

        /* package */
        var isDisabled = false

        /* package */
        lateinit var overrides: Map<TrackGroup, TrackSelectionOverride>
        fun init(
            trackGroups: List<Tracks.Group>,
            isDisabled: Boolean,
            overrides: Map<TrackGroup, TrackSelectionOverride>,
            allowAdaptiveSelections: Boolean,
            allowMultipleOverrides: Boolean
        ) {
            this.trackGroups = trackGroups
            this.isDisabled = isDisabled
            this.allowAdaptiveSelections = allowAdaptiveSelections
            this.allowMultipleOverrides = allowMultipleOverrides
            // TrackSelectionView does this filtering internally, but we need to do it here as well to
            // handle the case where the TrackSelectionView is never created.
            this.overrides = HashMap(
                TrackSelectionView.filterOverrides(overrides, trackGroups, allowMultipleOverrides)
            )
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(
                R.layout.exo_track_selection_dialog, container,  /* attachToRoot= */false
            )
            val trackSelectionView = rootView.findViewById<TrackSelectionView>(
                R.id.exo_track_selection_view
            )
            trackSelectionView.setShowDisableOption(true)
            trackSelectionView.setAllowMultipleOverrides(allowMultipleOverrides)
            trackSelectionView.setAllowAdaptiveSelections(allowAdaptiveSelections)
            trackSelectionView.init(
                trackGroups,
                isDisabled,
                overrides,  /* trackFormatComparator= */
                null,  /* listener= */
                this
            )
            return rootView
        }

        override fun onTrackSelectionChanged(
            isDisabled: Boolean, overrides: Map<TrackGroup, TrackSelectionOverride>
        ) {
            this.isDisabled = isDisabled
            this.overrides = overrides
        }
    }

    companion object {
        val SUPPORTED_TRACK_TYPES: ImmutableList<Int> =
            ImmutableList.of(C.TRACK_TYPE_VIDEO, C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_TEXT)

        /**
         * Returns whether a track selection dialog will have content to display if initialized with the
         * specified [Player].
         */
        fun willHaveContent(player: Player): Boolean {
            return willHaveContent(player.currentTracks)
        }

        /**
         * Returns whether a track selection dialog will have content to display if initialized with the
         * specified [Tracks].
         */
        fun willHaveContent(tracks: Tracks): Boolean {
            for (trackGroup in tracks.groups) {
                if (SUPPORTED_TRACK_TYPES.contains(trackGroup.type)) {
                    return true
                }
            }
            return false
        }

        /**
         * Creates a dialog for a given [Player], whose parameters will be automatically updated
         * when tracks are selected.
         *
         * @param player            The [Player].
         * @param onDismissListener A [DialogInterface.OnDismissListener] to call when the dialog is
         * dismissed.
         */
        fun createForPlayer(
            player: Player, onDismissListener: DialogInterface.OnDismissListener
        ): TrackSelectionDialog {
            return createForTracksAndParameters(
                player.currentTracks,
                player.trackSelectionParameters,
                allowAdaptiveSelections = true,
                allowMultipleOverrides = false,
                trackSelectionListener = object : TrackSelectionListener {
                    override fun onTracksSelected(trackSelectionParameters: TrackSelectionParameters?) {
                        if (trackSelectionParameters != null) {
                            player.trackSelectionParameters = trackSelectionParameters
                        }
                    }
                },
                onDismissListener = onDismissListener
            )
        }

        /**
         * Creates a dialog for given [Tracks] and [TrackSelectionParameters].
         *
         * @param tracks                   The [Tracks] describing the tracks to display.
         * @param trackSelectionParameters The initial [TrackSelectionParameters].
         * @param allowAdaptiveSelections  Whether adaptive selections (consisting of more than one track)
         * can be made.
         * @param allowMultipleOverrides   Whether tracks from multiple track groups can be selected.
         * @param trackSelectionListener   Called when tracks are selected.
         * @param onDismissListener        [DialogInterface.OnDismissListener] called when the dialog is
         * dismissed.
         */
        fun createForTracksAndParameters(
            tracks: Tracks,
            trackSelectionParameters: TrackSelectionParameters,
            allowAdaptiveSelections: Boolean,
            allowMultipleOverrides: Boolean,
            trackSelectionListener: TrackSelectionListener,
            onDismissListener: DialogInterface.OnDismissListener
        ): TrackSelectionDialog {
            val trackSelectionDialog = TrackSelectionDialog()
            trackSelectionDialog.init(
                tracks,
                trackSelectionParameters,
                allowAdaptiveSelections,
                allowMultipleOverrides,
                onClickListener = { _: DialogInterface?, _: Int ->
                    val builder = trackSelectionParameters.buildUpon()
                    for (i in SUPPORTED_TRACK_TYPES.indices) {
                        val trackType = SUPPORTED_TRACK_TYPES[i]
                        builder.setTrackTypeDisabled(
                            trackType,
                            trackSelectionDialog.getIsDisabled(trackType)
                        )
                        builder.clearOverridesOfType(trackType)
                        val overrides = trackSelectionDialog.getOverrides(trackType)
                        for (override in overrides.values) {
                            builder.addOverride(override)
                        }
                    }
                    trackSelectionListener.onTracksSelected(builder.build())
                },
                onDismissListener
            )
            return trackSelectionDialog
        }

        @Throws(IllegalArgumentException::class)
        private fun getTrackTypeString(resources: Resources, trackType: @TrackType Int): String {
            return when (trackType) {
                C.TRACK_TYPE_VIDEO -> resources.getString(
                    R.string.exo_track_selection_title_video
                )
                C.TRACK_TYPE_AUDIO -> resources.getString(
                    R.string.exo_track_selection_title_audio
                )
                C.TRACK_TYPE_TEXT -> resources.getString(
                    R.string.exo_track_selection_title_text
                )
                else -> throw IllegalArgumentException()
            }
        }
    }
}