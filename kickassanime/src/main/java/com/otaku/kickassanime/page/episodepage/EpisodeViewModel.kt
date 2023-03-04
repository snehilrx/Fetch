package com.otaku.kickassanime.page.episodepage

import android.net.Uri
import androidx.lifecycle.*
import com.google.gson.Gson
import com.otaku.fetch.base.livedata.SingleLiveEvent
import com.otaku.fetch.base.livedata.State
import com.otaku.kickassanime.Strings
import com.otaku.kickassanime.Strings.KAAST1
import com.otaku.kickassanime.api.model.CommonSubtitle
import com.otaku.kickassanime.api.model.ServerLinks
import com.otaku.kickassanime.db.models.CommonVideoLink
import com.otaku.kickassanime.db.models.EpisodeAnime
import com.otaku.kickassanime.db.models.LinkVideoObject
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.page.favourtites.FavouritesRepository
import com.otaku.kickassanime.page.history.HistoryRepository
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
    private val timeSkips = MutableLiveData<List<Long>>()
    private val subtitleLinks = MutableLiveData<List<CommonSubtitle>>()

    fun fetchEpisode(animeSlugId: Int, episodeSlugId: Int) {
        viewModelScope.launch(io) {
            loadState.postValue(State.LOADING())
            try {
                val currentPlaytime = historyRepository.getCurrentPlaytime(episodeSlugId)
                playTime.postValue(currentPlaytime)
            } catch (e: Exception) {
                //noop
            }
            try {
                val data = episodeRepository.fetchRemote(animeSlugId, episodeSlugId)
                val episode = data?.first
                val anime = data?.second
                if (episode != null && anime != null) {
                    addToHistory(episode)
                    loadDustUrls(episode.link1)
                    loadMobile2Urls(episode.link4)
                    fetchIntroTimestamp(anime.name, episode.name)
                } else {
                    throw Exception("No episode found!")
                }
                loadState.postValue(State.SUCCESS())
            } catch (e: Exception) {
                loadState.postValue(State.FAILED(e))
            }
        }
    }

    private fun fetchIntroTimestamp(displayTitle: String?, episodeNumber: String?) {
        viewModelScope.launch(io) {
            if (displayTitle != null && episodeNumber != null) {
                val fetchAnimeSkipTime = episodeRepository.fetchAnimeSkipTime(
                    displayTitle,
                    Integer.valueOf(episodeNumber)
                )
                viewModelScope.launch(default) {
                    fetchAnimeSkipTime?.filterNotNull()?.map { it.toLong() * 1000 }?.let {
                        timeSkips.postValue(it)
                    }
                }
            }
        }
    }

    fun getEpisodeWithAnime(episodeSlugId: Int, animeSlugId: Int): LiveData<EpisodeAnime?> =
        episodeRepository.getEpisodeWithAnime(episodeSlugId, animeSlugId).asLiveData(viewModelScope.coroutineContext)

    fun getVideoLink(): LiveData<List<CommonVideoLink>> = videoLink

    fun getThumbnailLink(): LiveData<String> = thumbnailLink

    fun getTimeSkip(): LiveData<List<Long>> = timeSkips

    fun getSubtitle(): LiveData<List<CommonSubtitle>> = subtitleLinks

    private fun addServerLinks(vararg links: CommonVideoLink) {
        val newList = ArrayList<CommonVideoLink>(videoLink.value ?: emptyList())
        newList.addAll(links)
        videoLink.postValue(newList)
    }

    private fun addServerLinks( links: List<CommonVideoLink>) {
        val newList = ArrayList<CommonVideoLink>(videoLink.value ?: emptyList())
        newList.addAll(links)
        videoLink.postValue(newList)
    }

    private fun fetchMaverickki(link: String) {
        viewModelScope.launch(io) {
            try {
                val data = Utils.parseMaverickkiLink(link, gson) ?: return@launch
                data.link()?.let { link -> addServerLinks(LinkVideoObject(link, CommonVideoLink.HLS)) }
                data.thumbnail.let { poster -> posterLink.postValue(poster) }
                data.timelineThumbnail.let { thumbnail -> thumbnailLink.postValue(thumbnail) }
                data.subtitles.let { subtitles -> subtitleLinks.postValue(subtitles) }
            } catch (e: Exception) {
                loadState.postValue(State.FAILED(e, false))
            }
        }
    }

    fun getLoadState(): LiveData<State> = loadState

    fun getIsPlaying(): LiveData<Boolean> = isPlaying

    fun setIsPlaying(playing: Boolean) {
        isPlaying.postValue(playing)
    }

    fun addToFavourites(animeSlugId: Int) {
        viewModelScope.launch {
            favoriteRepository.addToFavourites(animeSlugId)
        }
    }

    private fun addToHistory(episode: EpisodeEntity) {
        viewModelScope.launch {
            historyRepository.addToHistory(episode.asVideoHistory())
        }
    }

    fun updatePlayBackTime(episodeSlugId: Int, time: Long) {
        viewModelScope.launch {
            historyRepository.setPlaytime(episodeSlugId, time)
        }
    }

    private fun loadMobile2Urls(link: String?) {
        // TODO
    }

    private val atomicHash = AtomicReference<HashSet<Int>>(hashSetOf())

    private fun loadDustUrls(link1: String?) {
        viewModelScope.launch {
            link1?.let { link ->
                val hashCode = link1.hashCode()
                if (!atomicHash.get().contains(hashCode)) {
                    val dustLinks = episodeRepository.fetchDustLinks(link) ?: return@launch
                    val map =
                        dustLinks.data.filter { !it.src.isNullOrEmpty() && !it.name.isNullOrEmpty() }
                            .map {
                                ServerLinks(it.name!!, it.src!!)
                            }
                    serverLinksLD.postValue(serverLinks.apply { addAll(map) })
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
        if  (videoLinks.contains(uri)) {
            return
        }
        videoLinks.add(uri)
        when (uri.host) {
            Strings.KAA_URL, Strings.KAA2_URL -> {
                addServerLinks(LinkVideoObject(uri.toString(), CommonVideoLink.HLS))
            }
            Strings.MAVERICKKI_URL -> {
                fetchMaverickki(uri.toString())
            }
            Strings.ADD_KAA, KAAST1 -> {
                fetchADDKAA(uri.toString())
            }
        }
    }

    private fun fetchADDKAA(url: String) {
        viewModelScope.launch(io) {
            try {
                val data = Utils.parseAddKaaLink(url, gson) ?: return@launch

                viewModelScope.launch(default) {
                    val videoLinks = data.streams.filter { link ->
                        val linkUrl = link.url
                        linkUrl != null && linkUrl.isNotBlank() && linkUrl.isNotEmpty()
                    }
                    addServerLinks(videoLinks)
                }
                data.thumbnail?.url?.let { poster -> posterLink.postValue(poster) }
                data.subtitles.let { subtitles -> subtitleLinks.postValue(subtitles) }
            } catch (e: Exception) {
                loadState.postValue(State.FAILED(e, false))
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
}