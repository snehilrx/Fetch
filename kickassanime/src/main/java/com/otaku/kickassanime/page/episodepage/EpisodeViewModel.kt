package com.otaku.kickassanime.page.episodepage

import androidx.lifecycle.*
import com.google.gson.Gson
import com.otaku.fetch.base.livedata.SingleLiveEvent
import com.otaku.fetch.base.livedata.State
import com.otaku.kickassanime.api.model.Maverickki
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.page.favourtites.FavouritesRepository
import com.otaku.kickassanime.page.history.HistoryRepository
import com.otaku.kickassanime.utils.asVideoHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
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

    private val maverickki = SingleLiveEvent<Maverickki>()
    private val kaaPlayerVideoLink = SingleLiveEvent<String>()

    fun fetchEpisode(animeSlugId: Int, episodeSlugId: Int) {
        viewModelScope.launch(dispatcher) {
            loadState.postValue(State.LOADING())
            try {
                val currentPlaytime = historyRepository.getCurrentPlaytime(episodeSlugId)
                playTime.postValue(currentPlaytime)
            } catch (e: Exception){
                //noop
            }
            try {
                episodeRepository.fetchRemote(animeSlugId, episodeSlugId)
                loadState.postValue(State.SUCCESS())
            } catch (e: Exception){
                loadState.postValue(State.FAILED(e))
            }
        }
    }

    fun getEpisode(episodeSlugId: Int): LiveData<EpisodeEntity?> = episodeRepository.getEpisode(episodeSlugId).asLiveData(viewModelScope.coroutineContext)
    fun getAnime(animeSlugId: Int): LiveData<AnimeEntity?> = episodeRepository.getAnime(animeSlugId).asLiveData(viewModelScope.coroutineContext)

    fun getKaaPlayerVideoLink(): LiveData<String> = kaaPlayerVideoLink

    fun addKaaPlayer(url: String) {
        viewModelScope.launch(dispatcher) {
            kaaPlayerVideoLink.postValue(url)
        }
    }

    fun addMaverickki(link: String) {
        viewModelScope.launch(dispatcher) {
            link.toHttpUrlOrNull()?.let {
                // read text from url
                try{
                    val fromJson = gson.fromJson(it.toUrl().readText(), Maverickki::class.java)
                    maverickki.postValue(fromJson)
                }catch (e: Exception){
                    loadState.postValue(State.FAILED(e, false))
                }
            }
        }
    }

    fun getMaverickkiVideo(): LiveData<Maverickki> = maverickki

    fun getLoadState(): LiveData<State> = loadState

    fun getIsPlaying(): LiveData<Boolean> = isPlaying

    fun removeObservers(activity: EpisodeActivity) {
        loadState.removeObservers(activity)
        maverickki.removeObservers(activity)
        kaaPlayerVideoLink.removeObservers(activity)
    }

    fun setIsPlaying(playing: Boolean) {
        isPlaying.postValue(playing)
    }

    fun addToFavourites(animeSlugId: Int) {
        viewModelScope.launch {
            favoriteRepository.addToFavourites(animeSlugId)
        }
    }

    fun addToHistory(episode: EpisodeEntity) {
        viewModelScope.launch {
            historyRepository.addToHistory(episode.asVideoHistory())
        }
    }

    fun updatePlayBackTime(episodeSlugId: Int, time: Long){
        viewModelScope.launch {
            historyRepository.setPlaytime(episodeSlugId, time)
        }
    }

    fun getPlaybackTime() : LiveData<Long> = playTime
}