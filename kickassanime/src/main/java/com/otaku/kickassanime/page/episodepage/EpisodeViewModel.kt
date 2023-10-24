package com.otaku.kickassanime.page.episodepage

import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.*
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.offline.Download
import com.google.gson.Gson
import com.otaku.fetch.ModuleRegistry
import com.otaku.fetch.base.download.DownloadUtils
import com.otaku.fetch.base.livedata.SingleLiveEvent
import com.otaku.fetch.base.livedata.State
import com.otaku.fetch.base.settings.Settings
import com.otaku.kickassanime.Strings
import com.otaku.kickassanime.Strings.KICKASSANIME_URL
import com.otaku.kickassanime.api.model.CommonSubtitle
import com.otaku.kickassanime.api.model.ServerLinks
import com.otaku.kickassanime.db.models.CommonVideoLink
import com.otaku.kickassanime.db.models.EpisodeAnime
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.page.favourtites.FavouritesRepository
import com.otaku.kickassanime.page.history.HistoryRepository
import com.otaku.kickassanime.pojo.PlayData
import com.otaku.kickassanime.pojo.Sources
import com.otaku.kickassanime.utils.asVideoHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.TreeSet
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class EpisodeViewModel @Inject constructor(
    private val episodeRepository: EpisodeRepository,
    private val favoriteRepository: FavouritesRepository,
    private val historyRepository: HistoryRepository,
    @Named("io") private val io: CoroutineDispatcher,
    private val gson: Gson,
    private val downloadUtils: DownloadUtils
) : ViewModel() {

    private val loadState = MutableLiveData<State>()
    private val playTime = MutableLiveData<Long>()
    private val serverLinksLD = SingleLiveEvent<Set<ServerLinks>>()
    private val server = MutableLiveData<ServerLinks?>()
    private val videoLink = SingleLiveEvent<List<CommonVideoLink>>()
    private val thumbnailLink = SingleLiveEvent<String>()
    private val posterLink = SingleLiveEvent<String>()
    private val serverLinks = TreeSet<ServerLinks>(Comparator { o1, o2 ->
        // offline items should always be on top
        val to1 = o1 is ServerLinks.OfflineServerLink
        val to2 = o2 is ServerLinks.OfflineServerLink
        return@Comparator if (to1 && !to2) {
            -1
        } else {
            1
        }
    })
    private val timeSkips = MutableLiveData<List<Pair<Long, String?>>>()
    private val subtitleLinks = MutableLiveData<List<CommonSubtitle>>()

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun fetchEpisode(
        animeSlug: String,
        episodeSlug: String,
        dataStore: DataStore<Preferences>
    ) {
        viewModelScope.launch(io) {
            // offline links

            val offlineDownloads = findOfflineDownloads(episodeSlug)

            val fetchTimeStamps = dataStore.data.first()[Settings.SKIP_ENABLED] ?: false

            loadState.postValue(State.LOADING())
            try {
                val currentPlaytime = historyRepository.getCurrentPlaytime(episodeSlug)
                playTime.postValue(currentPlaytime)
            } catch (e: Exception) {
                //noop
            }
            try {
                var data = episodeRepository.fetchLocal(animeSlug, episodeSlug)
                if (data?.second == null || data.first == null) {
                    data = episodeRepository.fetchRemote(animeSlug, episodeSlug)
                }
                val episode = data?.first
                val anime = data?.second
                if (episode != null && anime != null) {
                    addToHistory(episode)
                    launch {
                        loadOfflineFile(offlineDownloads)
                    }
                    launch {
                        val link = "$KICKASSANIME_URL$animeSlug/$episodeSlug"
                        try {
                            loadEpisodePage(link)
                        } catch (e: Exception) {
                            Log.e("Episode", "Failed to load episode from $link", e)
                        }
                    }
                    try {
                        if (fetchTimeStamps) {
                            fetchIntroTimestamp(anime.name, episode.episodeNumber ?: 1f)
                        }
                    } catch (ignored: Exception) {
                        Log.e("ANIME SKIP", "failed to retrieve anime timestamps", ignored)
                    }
                } else {
                    throw Exception("No episode found!")
                }
                loadState.postValue(State.SUCCESS())
            } catch (e: Exception) {
                Log.e("Episode", "cannot fetch", e)
                loadState.postValue(State.FAILED(e))
            }
        }
    }

    fun checkOfflineServers(episodeSlug: String) {
        viewModelScope.launch {
            val offlineDownloads = findOfflineDownloads(episodeSlug)
            loadOfflineFile(offlineDownloads)
        }
    }

    private fun loadOfflineFile(offlineDownloads: Map<Uri, Download>) {
        serverLinksLD.postValue(serverLinks.apply {
            addAll(
                offlineDownloads.map {
                    ServerLinks.OfflineServerLink("Offline - ${it.key.host}", it.value)
                }
            )
        })
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun findOfflineDownloads(episodeSlug: String): Map<Uri, Download> {
        val uriDownloadMap = downloadUtils.getDownloadTracker().downloads.filter { (_, u) ->
            episodeSlug.equals(
                String(u.request.data),
                true
            )
        }

        return uriDownloadMap
    }

    private fun fetchIntroTimestamp(displayTitle: String?, episodeNumber: Float) {
        viewModelScope.launch(io) {
            if (displayTitle != null) {
                try {
                    val fetchAnimeSkipTime = episodeRepository.fetchAnimeSkipTime(
                        displayTitle,
                        episodeNumber
                    )
                    fetchAnimeSkipTime?.let { list ->
                        timeSkips.postValue(list.sortedBy { it.first })
                    }
                } catch (e: Exception) {
                    Log.e("anime-skip", "api call failed", e)
                }
            }
        }
    }

    fun getEpisodeWithAnime(episodeSlugId: String, animeSlugId: String): LiveData<EpisodeAnime?> =
        episodeRepository.getEpisodeWithAnime(episodeSlugId, animeSlugId)
            .asLiveData(viewModelScope.coroutineContext)

    fun getVideoLink(): LiveData<List<CommonVideoLink>> = videoLink

    @Suppress("unused")
    fun getThumbnailLink(): LiveData<String> = thumbnailLink

    fun getTimeSkip(): LiveData<List<Pair<Long, String?>>> = timeSkips

    fun getSubtitle(): LiveData<List<CommonSubtitle>> = subtitleLinks

    private fun addServerLinks(vararg links: CommonVideoLink) {
        val newList = ArrayList<CommonVideoLink>(videoLink.value ?: emptyList())
        newList.addAll(links)
        videoLink.postValue(newList)
    }

    fun getLoadState(): LiveData<State> = loadState

    fun addToFavourites(animeSlug: String) {
        viewModelScope.launch {
            favoriteRepository.addToFavourites(animeSlug)
        }
    }

    private fun addToHistory(episode: EpisodeEntity) {
        viewModelScope.launch {
            historyRepository.addToHistory(episode.asVideoHistory())
        }
    }

    fun updatePlayBackTime(episodeSlug: String, time: Long) {
        viewModelScope.launch {
            historyRepository.setPlaytime(episodeSlug, time)
        }
    }

    private val atomicHash = AtomicReference<HashSet<Int>>(hashSetOf())

    private fun loadEpisodePage(link1: String?) {
        viewModelScope.launch {
            link1?.let { link ->
                val hashCode = link.hashCode()
                val host = Uri.parse(link).host ?: "UNKNOWN"
                if (!atomicHash.get().contains(hashCode)) {
                    serverLinksLD.postValue(serverLinks.apply {
                        add(
                            ServerLinks.OnlineServerLink(host, link)
                        )
                    })
                }
                atomicHash.get().add(hashCode)
            }
        }
    }

    fun getServersLinks(): LiveData<Set<ServerLinks>> = serverLinksLD

    fun getCurrentServer(): LiveData<ServerLinks?> = server

    fun getPlaybackTime(): LiveData<Long> = playTime

    fun setCurrentServer(link: ServerLinks) {
        server.postValue(link)
    }

    private val videoLinks = hashSetOf<Uri>()

    fun clearServers() {
        loadState.value = State.LOADING()
        playTime.value = 0
        serverLinksLD.value = LinkedHashSet()
        server.value = null
        atomicHash.get().clear()
        videoLink.value = emptyList()
        thumbnailLink.value = ""
        posterLink.value = ""
        serverLinks.clear()
        timeSkips.value = emptyList()
        subtitleLinks.value = emptyList()
        serverLinks.clear()
        videoLinks.clear()
    }

    override fun onCleared() {
        val webView =
            ModuleRegistry.modules[Strings.KICKASSANIME]?.appModule?.webView as? CustomWebView
        webView?.release()
        viewModelScope.cancel()
        super.onCleared()
        clearServers()
    }

    private fun processPageData(json: String) {
        viewModelScope.launch {
            val playData = gson.fromJson(json, PlayData::class.java)
            processPlayData(playData.allSources).forEach { (streams, captions) ->
                subtitleLinks.postValue(captions)
                addServerLinks(streams)
            }
        }
    }

    fun loadPage(url: String?) {
        if (url != null) {
            val ref = WeakReference(this)
            viewModelScope.launch {
                val webView =
                    ModuleRegistry.modules[Strings.KICKASSANIME]?.appModule?.webView as? CustomWebView
                val json = webView?.enqueue(url) ?: return@launch
                if (!coroutineContext.isActive) {
                    ref.clear()
                }
                ref.get()?.processPageData(json)
            }
        }
    }

    companion object {
        @JvmStatic
        fun processPlayData(sources: ArrayList<Sources>): List<Pair<CommonVideoLink, List<CommonSubtitle>>> {
            return sources.map { source ->
                val streams = object : CommonVideoLink {
                    override fun getLink(): String {
                        return "https:${source.file}"
                    }

                    override fun getLinkName(): String {
                        return "KAA ${source.label}"
                    }

                    override fun getVideoType(): Int {
                        return if (source.type?.equals("hls") == true) {
                            CommonVideoLink.HLS
                        } else {
                            CommonVideoLink.DASH
                        }
                    }
                }
                val captions = source.tracks.filter { it.kind == "captions" }
                    .map captionMap@{
                        return@captionMap object : CommonSubtitle {
                            override fun getLink(): String {
                                return "https:${it.file}"
                            }

                            override fun getLanguage(): String {
                                return it.label ?: ""
                            }

                            @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
                            override fun getFormat(): String {
                                return if (it.file?.endsWith(".vtt") == true) {
                                    MimeTypes.TEXT_VTT
                                } else {
                                    MimeTypes.TEXT_UNKNOWN
                                }
                            }
                        }
                    }
                Pair(streams, captions)
            }
        }
    }

}