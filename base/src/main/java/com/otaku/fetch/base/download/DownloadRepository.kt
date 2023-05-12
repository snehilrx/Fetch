package com.otaku.fetch.base.download

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.media3.exoplayer.offline.Download
import com.otaku.fetch.ModuleRegistry
import javax.inject.Inject

class DownloadRepository @Inject constructor() {
    abstract class TreeNode(
        open val parent: TreeNode? = null,
        open val children: SnapshotStateList<in TreeNode> = SnapshotStateList(),
        val parentIndex: Int
    ) {
        // remove itself and children
        abstract fun removeSelf(downloadUtils: DownloadUtils, context: Context)

        abstract fun removeFromParent()

        abstract fun findChild(child: TreeNode): TreeNode?

        protected val indexMap = hashMapOf<Int, Int>()

        open fun add(node: TreeNode) {
            indexMap[node.hashCode()] = size++
        }

        fun removeAt(index: Int) {
            size--
            children.removeAt(index)
        }

        fun replaceAt(index: Int, node: TreeNode) {
            children[index] = node
        }

        fun invalidate() {
            if (size == 0) {
                parent?.removeAt(parentIndex)
                parent?.invalidate()
            }
        }

        var size = 0
    }

    data class Root(override val children: SnapshotStateList<in TreeNode> = SnapshotStateList()) :
        TreeNode(parentIndex = 0) {
        override fun removeSelf(downloadUtils: DownloadUtils, context: Context) {
            // Ignore
        }

        override fun removeFromParent() {
            // Ignore
        }

        override fun findChild(child: TreeNode): Anime? {
            val index = indexMap[child.hashCode()] ?: return null
            return children.getOrNull(index) as? Anime
        }

        override fun add(node: TreeNode) {
            if (node is Anime) {
                super.add(node)
                children.add(node)
            }
        }

        val leafReference = hashMapOf<String, Link>()
    }

    data class Anime(
        val anime: String,
        val animeName: String,
        override val parent: Root,
        override val children: SnapshotStateList<in TreeNode> = SnapshotStateList()
    ) : TreeNode(parentIndex = parent.size) {
        override fun removeSelf(downloadUtils: DownloadUtils, context: Context) {
            children.forEach { node ->
                (node as? TreeNode)?.removeSelf(downloadUtils, context)
            }
        }

        override fun removeFromParent() {
            parent.removeAt(parentIndex)
        }

        override fun findChild(child: TreeNode): Episode? {
            val index = indexMap[child.hashCode()] ?: return null
            return children.getOrNull(index) as? Episode
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Anime

            if (anime != other.anime) return false
            if (animeName != other.animeName) return false

            return true
        }

        override fun hashCode(): Int {
            var result = anime.hashCode()
            result = 31 * result + animeName.hashCode()
            return result
        }

        override fun add(node: TreeNode) {
            if (node is Episode) {
                super.add(node)
                children.add(node)
            }
        }
    }

    data class Episode(
        val episode: String,
        val episodeNumber: Float,
        override val parent: Anime,
        override val children: SnapshotStateList<in TreeNode> = SnapshotStateList()
    ) : TreeNode(parentIndex = parent.size) {

        override fun removeSelf(downloadUtils: DownloadUtils, context: Context) {
            children.forEach { node ->
                (node as? TreeNode)?.removeSelf(downloadUtils, context)
            }
        }

        override fun removeFromParent() {
            parent.removeAt(parentIndex)
        }

        override fun findChild(child: TreeNode): Link? {
            val index = indexMap[child.hashCode()] ?: return null
            return children.getOrNull(index) as? Link
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Episode

            if (episode != other.episode) return false
            if (episodeNumber != other.episodeNumber) return false
            if (children != other.children) return false

            return true
        }

        override fun hashCode(): Int {
            var result = episode.hashCode()
            result = 31 * result + episodeNumber.hashCode()
            return result
        }

        override fun add(node: TreeNode) {
            if (node is Link) {
                super.add(node)
                children.add(node)
            }
        }

    }

    data class Link(
        val download: DownloadWrapper,
        override val parent: Episode,
        val index: Int = parent.size
    ) : TreeNode(parentIndex = index) {

        override fun removeSelf(downloadUtils: DownloadUtils, context: Context) {
            downloadUtils.getDownloadTracker().deleteDownload(download.download, context)
            removeFromParent()
            parent.invalidate()
        }

        override fun removeFromParent() {
            parent.removeAt(parentIndex)
        }

        override fun findChild(child: TreeNode): TreeNode? {
            return null
        }

        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Link

            if (download != other.download) return false

            return true
        }

        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        override fun hashCode(): Int {
            return download.hashCode()
        }

        fun replace(downloadWrapper: DownloadWrapper) {
            parent.replaceAt(
                parentIndex, Link(
                    downloadWrapper, parent, parentIndex
                )
            )
        }
    }

    val root = Root()


    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    suspend fun findEpisodes(downloads: HashMap<Uri, Download>) {
        downloads.forEach loop@{ (t, u) ->
            ModuleRegistry.getModulesList().forEach moduleLoop@{
                val key = String(u.request.data)
                val item = it.appModule?.findEpisode(key, t.toString(), u.request.mimeType ?: "")
                    ?: return@moduleLoop
                // update tree
                var anime = Anime(item.animeKey, item.animeTitle, root)
                val findAnime = root.findChild(anime)
                val downloadWrapper = DownloadWrapper(u, item.launchBundle, item.launchActivity)
                if (findAnime == null) {
                    val episode = Episode(item.episodeKey, item.episodeNumber, anime)
                    val link = Link(downloadWrapper, episode)
                    episode.add(link)
                    anime.add(episode)
                    root.add(anime)
                    root.leafReference[downloadWrapper.download.request.id] = link
                    return@moduleLoop
                } else {
                    anime = findAnime
                }
                var episode = Episode(item.episodeKey, item.episodeNumber, anime)
                val findEpisode = anime.findChild(episode)
                if (findEpisode == null) {
                    val link = Link(downloadWrapper, episode)
                    episode.add(link)
                    anime.add(episode)
                    root.leafReference[downloadWrapper.download.request.id] = link
                    return@moduleLoop
                } else {
                    episode = findEpisode
                }
                val link = Link(downloadWrapper, episode)
                val findLink = episode.findChild(link)
                if (findLink == null) {
                    episode.add(link)
                    root.leafReference[downloadWrapper.download.request.id] = link
                } else {
                    findLink.replace(downloadWrapper)
                }
            }
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun update(download: Download) {
        val link = root.leafReference[download.request.id]
        link?.replace(
            DownloadWrapper(
                download,
                link.download.launchBundle,
                link.download.launchActivity
            )
        )
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    class DownloadWrapper(
        val download: Download,
        val launchBundle: Bundle,
        val launchActivity: Class<*>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DownloadWrapper

            if (download.state != other.download.state) return false
            if (download.bytesDownloaded != other.download.bytesDownloaded) return false
            if (download.startTimeMs != other.download.startTimeMs) return false
            if (download.stopReason != other.download.stopReason) return false
            if (download.failureReason != other.download.failureReason) return false
            if (download.percentDownloaded != other.download.percentDownloaded) return false
            if (download.contentLength != other.download.contentLength) return false
            if (download.request.id != other.download.request.id) return false

            return true
        }


        override fun hashCode(): Int {
            return download.request.id.hashCode()
        }

    }
}