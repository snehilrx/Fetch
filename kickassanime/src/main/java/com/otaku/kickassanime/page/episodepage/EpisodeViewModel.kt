package com.otaku.kickassanime.page.episodepage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.EpisodeEntity
import com.otaku.kickassanime.utils.model.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class EpisodeViewModel @Inject constructor(
    private val episodeRepository: EpisodeRepository,
    @Named("io") private val dispatcher: CoroutineDispatcher
) :
    ViewModel() {

    private val episode = MutableLiveData<Response<EpisodeEntity>>()
    private val anime = MutableLiveData<Response<AnimeEntity>>()

    fun fetchAnime(id: Int) {
        viewModelScope.launch(dispatcher) {
            val animeFromEpisodeSlug = episodeRepository.getAnime(id)
            if (animeFromEpisodeSlug != null)
                anime.postValue(Response.Success(animeFromEpisodeSlug))
            else
                anime.postValue(Response.Error(Throwable("Anime not found")))
        }
    }

    fun fetchEpisode(id: Int, animeId: Int) {
        viewModelScope.launch(dispatcher) {
            val episodeEntity = episodeRepository.getEpisode(id, animeId)
            if (episodeEntity != null)
                episode.postValue(Response.Success(episodeEntity))
            else
                episode.postValue(Response.Error(Throwable("Episode not found")))
        }
    }

    fun getEpisode(): LiveData<Response<EpisodeEntity>> = episode
    fun getAnime(): LiveData<Response<AnimeEntity>> = anime

}