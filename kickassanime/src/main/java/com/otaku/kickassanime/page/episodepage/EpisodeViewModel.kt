package com.otaku.kickassanime.page.episodepage

import android.content.res.Configuration
import android.net.Uri
import androidx.lifecycle.*
import com.google.gson.Gson
import com.otaku.fetch.base.livedata.SingleLiveEvent
import com.otaku.fetch.base.livedata.State
import com.otaku.kickassanime.Strings
import com.otaku.kickassanime.api.model.Maverickki
import com.otaku.kickassanime.api.model.ServerLinks
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.page.favourtites.FavouritesRepository
import com.otaku.kickassanime.page.history.HistoryRepository
import com.otaku.kickassanime.utils.asVideoHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class EpisodeViewModel @Inject constructor(
    private val episodeRepository: EpisodeRepository,
    private val favoriteRepository: FavouritesRepository,
    private val historyRepository: HistoryRepository,
    @Named("io") private val dispatcher: CoroutineDispatcher,
    private val gson: Gson
) : ViewModel() {

    private val loadState = MutableLiveData<State>()
    private val isPlaying = MutableLiveData<Boolean>()
    private val playTime = MutableLiveData<Long>()
    private val serverLinksLD = SingleLiveEvent<Set<ServerLinks>>()
    private val server = MutableLiveData<String>()
    private val videoLink = SingleLiveEvent<String>()
    private val thumbnailLink = SingleLiveEvent<String>()
    private val subtitleLink = SingleLiveEvent<List<String>>()
    private val posterLink = SingleLiveEvent<String>()
    private val serverLinks = hashSetOf<ServerLinks>()

    var orientation = Configuration.ORIENTATION_UNDEFINED

    fun fetchEpisode(animeSlugId: Int, episodeSlugId: Int) {
        viewModelScope.launch(dispatcher) {
            loadState.postValue(State.LOADING())
            try {
                val currentPlaytime = historyRepository.getCurrentPlaytime(episodeSlugId)
                playTime.postValue(currentPlaytime)
            } catch (e: Exception) {
                //noop
            }
            try {
                val episode = episodeRepository.fetchRemote(animeSlugId, episodeSlugId)
                if(episode != null) {
                    addToHistory(episode)
                    loadDustUrls(episode.link1)
                    loadMobile2Urls(episode.link4)
                } else {
                    throw Exception("No episode found!")
                }
                loadState.postValue(State.SUCCESS())
            } catch (e: Exception) {
                loadState.postValue(State.FAILED(e))
            }
        }
    }

    fun getEpisode(episodeSlugId: Int): LiveData<EpisodeEntity?> =
        episodeRepository.getEpisode(episodeSlugId).asLiveData(viewModelScope.coroutineContext)

    fun getAnime(animeSlugId: Int): LiveData<AnimeEntity?> =
        episodeRepository.getAnime(animeSlugId).asLiveData(viewModelScope.coroutineContext)

    fun getVideoLink(): LiveData<String> = videoLink

    fun getThumbnailLink(): LiveData<String> = thumbnailLink

    private fun fetchKaaPlayer(url: String) {
        viewModelScope.launch(dispatcher) {
            videoLink.postValue(url)
        }
    }

    private fun fetchMaverickki(link: String) {
        viewModelScope.launch(dispatcher) {
            link.toHttpUrlOrNull()?.let {
                // read text from url
                try {
                    val data = gson.fromJson(it.toUrl().readText(), Maverickki::class.java)
                    data.link()?.let { link -> videoLink.postValue(link) }
                    data.subtitles.let { sub -> subtitleLink.postValue(sub) }
                    data.thumbnail.let { poster -> posterLink.postValue(poster) }
                    data.timelineThumbnail.let { thumbnail -> thumbnailLink.postValue(thumbnail) }
                } catch (e: Exception) {
                    loadState.postValue(State.FAILED(e, false))
                }
            }
        }
    }

    fun getLoadState(): LiveData<State> = loadState

    fun getIsPlaying(): LiveData<Boolean> = isPlaying

    fun removeObservers(activity: EpisodeActivity) {
        loadState.removeObservers(activity)
        isPlaying.removeObservers(activity)
        playTime.removeObservers(activity)
        serverLinksLD.removeObservers(activity)
        server.removeObservers(activity)
        videoLink.removeObservers(activity)
        thumbnailLink.removeObservers(activity)
        subtitleLink.removeObservers(activity)
        posterLink.removeObservers(activity)
        serverLinks.clear()
    }

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

    private fun loadMobile2Urls(link4: String?) {

    }

    private val atomicHash = AtomicReference<HashSet<Int>>(hashSetOf())

    private fun loadDustUrls(link1: String?) {
        viewModelScope.launch {
            link1?.let { link ->
                val hashCode = link1.hashCode()
                if(!atomicHash.get().contains(hashCode)){
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

    fun handleVideoLinks(uri: Uri) {
        when(uri.host) {
            Strings.KAA_URL, Strings.KAA2_URL -> {
                fetchKaaPlayer(uri.toString())
            }
            Strings.MAVERICKKI_URL -> {
                fetchMaverickki(uri.toString())
            }
        }
    }

    fun clearServers() {
        serverLinks.clear()
    }
}