package com.otaku.fetch.base.download

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadProgress
import com.otaku.fetch.ModuleRegistry
import javax.inject.Inject

class DownloadRepository @Inject constructor(
    val downloadUtils: DownloadUtils
) {

    abstract class Node(val parent: Node? = null, val key: String) {
        abstract fun delete(downloadUtils: DownloadUtils, context: Context)
        abstract val size: Int
        abstract fun remove(key: String)
        abstract fun sort()
    }

    open class TreeNode<T : Node>(parent: Node? = null, key: String) : Node(parent, key) {

        private val children = SnapshotStateMap<String, T>()
        protected val list = ArrayList<T>()

        override val size: Int
            get() = children.size

        override fun delete(downloadUtils: DownloadUtils, context: Context) {
            children.forEach { (key, node) ->
                node.delete(downloadUtils, context)
                remove(key)
            }

            var upNode: Node? = this
            while (upNode != null) {
                if (upNode.size == 0)
                    upNode.parent?.remove(upNode.key)
                else
                    break
                upNode = upNode.parent
            }
        }

        fun putIfAbsent(id: String, value: T) {
            if (!children.containsKey(id)) {
                children[id] = value
                list.add(value)
            }
        }

        operator fun get(key: String): T? = children[key]
        operator fun get(key: Int): T? = list.getOrNull(key)

        fun isEmpty(): Boolean = children.isEmpty()

        override fun remove(key: String) {
            val remove = children.remove(key)
            list.remove(remove)
        }

        override fun sort() {
            children.forEach { (_, u) -> u.sort() }
        }
    }

    class Root : TreeNode<Anime>(key = "root")

    class Anime(
        anime: String,
        val animeName: String,
        val root: Root
    ) : TreeNode<Episode>(parent = root, key = anime) {


        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Anime) return false

            if (key != other.key) return false

            return true
        }

        override fun hashCode(): Int {
            return key.hashCode()
        }

        override fun sort() {
            list.sortedBy { it.episodeNumber }
        }
    }

    class Episode(
        episode: String,
        val episodeNumber: Float,
        val anime: Anime
    ) : TreeNode<Link>(anime, episode) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Episode) return false

            if (key != other.key) return false

            return true
        }

        override fun hashCode(): Int {
            return key.hashCode()
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    data class Link(
        var download: DownloadWrapper,
        val episode: Episode
    ) : TreeNode<Nothing>(episode, download.download.value.request.id) {
        override fun delete(downloadUtils: DownloadUtils, context: Context) {
            super.delete(downloadUtils, context)
            downloadUtils.getDownloadTracker().deleteDownload(download.download.value, context)
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    class DownloadWrapper(
        val download: MutableState<Download>,
        val launchBundle: Bundle,
        val launchActivity: Class<*>
    ) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DownloadWrapper
            if (download.value.state != other.download.value.state) return false
            if (download.value.bytesDownloaded != other.download.value.bytesDownloaded) return false
            if (download.value.startTimeMs != other.download.value.startTimeMs) return false
            if (download.value.stopReason != other.download.value.stopReason) return false
            if (download.value.failureReason != other.download.value.failureReason) return false
            if (download.value.percentDownloaded != other.download.value.percentDownloaded) return false
            if (download.value.contentLength != other.download.value.contentLength) return false
            if (download.value.request.id != other.download.value.request.id) return false

            return true
        }


        override fun hashCode(): Int {
            return download.value.request.id.hashCode()
        }

    }

    val root = Root()

    private val links = HashMap<String, Link>()

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    suspend fun findEpisodes(downloads: HashMap<Uri, Download>) {
        downloads.forEach loop@{ (t, u) ->
            ModuleRegistry.getModulesList().forEach moduleLoop@{
                val key = String(u.request.data)
                // Check if an item with the given key exists in the downloadItems map
                val item = createItem(key, u, t, it) ?: return@moduleLoop

                val anime = root[item.animeKey] ?: Anime(item.animeKey, item.animeTitle, root)
                val episode = anime[item.episodeKey] ?: Episode(
                    item.episodeKey,
                    item.episodeNumber,
                    anime
                )
                val downloadWrapper =
                    DownloadWrapper(mutableStateOf(u), item.launchBundle, item.launchActivity)
                val link = Link(downloadWrapper, episode)
                links.putIfAbsent(link.download.download.value.request.id, link)
                episode.putIfAbsent(link.download.download.value.request.id, link)
                anime.putIfAbsent(episode.key, episode)
                root.putIfAbsent(anime.key, anime)
            }
            root.sort()
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private suspend fun createItem(
        key: String, u: Download, t: Uri, moduleData: ModuleRegistry.ModuleData
    ): DownloadItem? {
        return (moduleData.appModule?.findEpisode(key, t.toString(), u.request.mimeType ?: ""))
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun update(id: String, contentLength: Long, bytesDownloaded: Long, percentDownloaded: Float) {
        val link = links[id] ?: return
        val download = link.download.download
        val oldDownload = download.value
        download.value = Download(
            oldDownload.request,
            oldDownload.state,
            oldDownload.startTimeMs,
            oldDownload.updateTimeMs,
            contentLength,
            oldDownload.failureReason,
            oldDownload.stopReason,
            DownloadProgress().apply {
                this.percentDownloaded = percentDownloaded
                this.bytesDownloaded = bytesDownloaded
            }
        )
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun update(download: Download) {
        val link = links[download.request.id] ?: return
        link.download.download.value = download

    }

    fun delete(item: Node?, downloadUtils: DownloadUtils, context: Context) {
        item?.delete(downloadUtils, context)
    }

}
