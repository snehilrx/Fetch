package com.otaku.kickassanime.page.episodepage

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.otaku.fetch.base.livedata.SingleLiveEvent
import com.otaku.fetch.base.livedata.State
import com.otaku.kickassanime.Strings
import com.otaku.kickassanime.Strings.KICKASSANIME_URL
import com.otaku.kickassanime.api.model.CommonSubtitle
import com.otaku.kickassanime.api.model.ServerLinks
import com.otaku.kickassanime.db.models.CommonVideoLink
import com.otaku.kickassanime.db.models.EpisodeAnime
import com.otaku.kickassanime.db.models.LinkVideoObject
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.page.favourtites.FavouritesRepository
import com.otaku.kickassanime.page.history.HistoryRepository
import com.otaku.kickassanime.pojo.CrunchyRoll
import com.otaku.kickassanime.utils.Utils
import com.otaku.kickassanime.utils.asVideoHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class EpisodeViewModel @Inject constructor(
    private val episodeRepository: EpisodeRepository,
    private val favoriteRepository: FavouritesRepository,
    private val historyRepository: HistoryRepository,
    @Named("io") private val io: CoroutineDispatcher,
    @Named("default") private val default: CoroutineDispatcher,
    private val gson: Gson
) : ViewModel() {

    private val loadState = MutableLiveData<State>()
    private val isPlaying = MutableLiveData<Boolean>()
    private val playTime = MutableLiveData<Long>()
    private val serverLinksLD = SingleLiveEvent<Set<ServerLinks>>()
    private val server = MutableLiveData<String>()
    private val videoLink = SingleLiveEvent<List<CommonVideoLink>>()
    private val thumbnailLink = SingleLiveEvent<String>()
    private val posterLink = SingleLiveEvent<String>()
    private val serverLinks = hashSetOf<ServerLinks>()
    private val timeSkips = MutableLiveData<List<Pair<Long, String?>>>()
    private val subtitleLinks = MutableLiveData<List<CommonSubtitle>>()

    fun fetchEpisode(
        animeSlug: String,
        episodeSlug: String,
        useOffline: Boolean,
        fetchTimeStamps: Boolean
    ) {
        viewModelScope.launch(io) {
            loadState.postValue(State.LOADING())
            try {
                val currentPlaytime = historyRepository.getCurrentPlaytime(episodeSlug)
                playTime.postValue(currentPlaytime)
            } catch (e: Exception) {
                //noop
            }
            try {
                val data = if (useOffline) {
                    episodeRepository.fetchLocal(animeSlug, episodeSlug)
                } else {
                    episodeRepository.fetchRemote(animeSlug, episodeSlug)
                }
                val episode = data?.first
                val anime = data?.second
                if (episode != null && anime != null) {
                    addToHistory(episode)
                    if (!useOffline) {
                        loadDustUrls("$KICKASSANIME_URL$animeSlug/$episodeSlug")
                    }
                    try {
                        if (fetchTimeStamps) {
                            fetchIntroTimestamp(anime.name, episode.episodeNumber ?: 1f)
                        }
                    } catch (ignored: Exception) {
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

    private fun fetchIntroTimestamp(displayTitle: String?, episodeNumber: Float) {
        viewModelScope.launch(io) {
            if (displayTitle != null) {
                try {
                    val fetchAnimeSkipTime = episodeRepository.fetchAnimeSkipTime(
                        displayTitle,
                        episodeNumber
                    )
                    viewModelScope.launch(default) {
                        fetchAnimeSkipTime?.let {
                            timeSkips.postValue(it)
                        }
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

    fun getThumbnailLink(): LiveData<String> = thumbnailLink

    fun getTimeSkip(): LiveData<List<Pair<Long, String?>>> = timeSkips

    fun getSubtitle(): LiveData<List<CommonSubtitle>> = subtitleLinks

    private fun addServerLinks(vararg links: CommonVideoLink) {
        val newList = ArrayList<CommonVideoLink>(videoLink.value ?: emptyList())
        newList.addAll(links)
        videoLink.postValue(newList)
    }

    private fun addServerLinks(links: List<CommonVideoLink>) {
        val newList = ArrayList<CommonVideoLink>(videoLink.value ?: emptyList())
        newList.addAll(links)
        videoLink.postValue(newList)
    }

    private fun fetchMaverickki(link: String) {
        viewModelScope.launch(io) {
            try {
                val data = Utils.parseMaverickkiLink(link, gson) ?: return@launch
                data.link()
                    ?.let { link -> addServerLinks(LinkVideoObject(link, CommonVideoLink.HLS)) }
                data.thumbnail.let { poster -> posterLink.postValue(poster) }
                data.timelineThumbnail.let { thumbnail -> thumbnailLink.postValue(thumbnail) }
                data.subtitles.let { subtitles -> subtitleLinks.postValue(subtitles) }
            } catch (e: Exception) {
                loadState.postValue(State.FAILED(e))
            }
        }
    }

    fun getLoadState(): LiveData<State> = loadState

    fun getIsPlaying(): LiveData<Boolean> = isPlaying

    fun setIsPlaying(playing: Boolean) {
        isPlaying.postValue(playing)
    }

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

    private fun loadDustUrls(link1: String?) {
        viewModelScope.launch {
            link1?.let { link ->
                val hashCode = link.hashCode()
                val host = Uri.parse(link).host ?: "UNKNOWN"
                if (!atomicHash.get().contains(hashCode)) {
                    serverLinksLD.postValue(serverLinks.apply {
                        add(
                            ServerLinks(host, link)
                        )
                    })
                }
                atomicHash.get().add(hashCode)
            }
        }
    }

    fun getServersLinks(): LiveData<Set<ServerLinks>> = serverLinksLD

    fun getCurrentServer(): LiveData<String> = server

    fun getPlaybackTime(): LiveData<Long> = playTime

    fun setCurrentServer(first: String) {
        server.postValue(first)
    }

    private val videoLinks = hashSetOf<Uri>()

    fun handleVideoLinks(uri: Uri) {
        if (videoLinks.contains(uri)) {
            return
        }
        videoLinks.add(uri)
        when (uri.host) {
            Strings.VRV -> {
                // hardcode video format
                addServerLinks(LinkVideoObject(uri.toString(), CommonVideoLink.HLS))
            }

            Strings.KAA_URL, Strings.KAA2_URL -> {
                // hardcode video format
                addServerLinks(LinkVideoObject(uri.toString(), CommonVideoLink.HLS))
            }

            Strings.MAVERICKKI_URL -> {
                fetchMaverickki(uri.toString())
            }
        }
    }

    fun clearServers() {
        loadState.value = State.LOADING()
        isPlaying.value = false
        playTime.value = 0
        serverLinksLD.value = LinkedHashSet()
        server.value = ""
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
        super.onCleared()
        clearServers()
    }

    fun handleCrunchyRoll(json: String) {
        viewModelScope.launch {
            val crunchyRoll = gson.fromJson(json, CrunchyRoll::class.java)
            addServerLinks(crunchyRoll.streams)
        }
    }
}