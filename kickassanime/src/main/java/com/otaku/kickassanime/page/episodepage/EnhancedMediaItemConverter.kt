package com.otaku.kickassanime.page.episodepage

import androidx.media3.cast.DefaultMediaItemConverter
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaTrack
import com.google.common.collect.ImmutableList

@UnstableApi
internal class EnhancedMediaItemConverter(private val defaultConverter: DefaultMediaItemConverter = DefaultMediaItemConverter()) :
    MediaItemConverter {

    override fun toMediaItem(item: MediaQueueItem): MediaItem {
        if (item.customData != null) {
            return defaultConverter.toMediaItem(item)
        }
        return MediaItem.Builder().build()
    }

    override fun toMediaQueueItem(item: MediaItem): MediaQueueItem {
        val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
        movieMetadata.putString(MediaMetadata.KEY_TITLE, item.mediaMetadata.title.toString())

        val mediaInfoBuilder = MediaInfo.Builder(item.localConfiguration?.uri.toString())
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType(item.localConfiguration?.mimeType ?: MimeTypes.VIDEO_UNKNOWN)
            .setMetadata(movieMetadata)

        item.localConfiguration?.subtitleConfigurations?.let { subtitles ->
            mediaInfoBuilder.setMediaTracks(createSubtitleMediaTracks(subtitles))
        }

        return MediaQueueItem.Builder(mediaInfoBuilder.build()).build()
    }

    companion object {
        private fun createSubtitleMediaTracks(subtitles: ImmutableList<MediaItem.SubtitleConfiguration>): ArrayList<MediaTrack> {
            val subtitleMediaTracks = ArrayList<MediaTrack>()

            subtitles.forEachIndexed { index, subtitle ->
                val subtitleTrack = MediaTrack.Builder(index.toLong(), MediaTrack.TYPE_TEXT)
                    .setName(subtitle.language)
                    .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
                    .setContentId(subtitle.uri.toString())
                    .setLanguage(subtitle.language)
                    .build()

                subtitleMediaTracks.add(subtitleTrack)
            }

            return subtitleMediaTracks
        }
    }
}