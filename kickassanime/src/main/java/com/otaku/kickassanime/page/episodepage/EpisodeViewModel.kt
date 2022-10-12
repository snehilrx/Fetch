package com.otaku.kickassanime.page.episodepage

import androidx.lifecycle.*
import com.google.gson.Gson
import com.otaku.fetch.base.livedata.SingleLiveEvent
import com.otaku.fetch.base.livedata.State
import com.otaku.kickassanime.api.model.Maverickki
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class EpisodeViewModel @Inject constructor(
    private val episodeRepository: EpisodeRepository,
    @Named("io") private val dispatcher: CoroutineDispatcher,
    private val gson: Gson
) : ViewModel() {

    private val loadState = MutableLiveData<State>()

    private val maverickki = SingleLiveEvent<Maverickki>()
    private val kaaPlayerVideoLink = SingleLiveEvent<String>()

    fun fetchEpisode(animeSlugId: Int, episodeSlugId: Int) {
        viewModelScope.launch(dispatcher) {
            loadState.postValue(State.LOADING())
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

    fun removeObservers(activity: EpisodeActivity) {
        loadState.removeObservers(activity)
        maverickki.removeObservers(activity)
        kaaPlayerVideoLink.removeObservers(activity)
    }
}