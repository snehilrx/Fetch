package com.otaku.kickassanime.page.animepage

import androidx.lifecycle.*
import com.otaku.fetch.base.livedata.State
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.page.adapters.EpisodeAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeViewModel @Inject constructor(private val animeRepository: AnimeRepository) :
    ViewModel() {

    val state = MutableLiveData<State>()

    fun fetchAnime(animeSlug: String) {
        state.postValue(State.LOADING())
        viewModelScope.launch(Dispatchers.IO) {
            animeRepository.invalidateAnime(animeSlug)
            state.postValue(State.SUCCESS())
        }
    }

    fun getAnime(animeSlugId: Int): LiveData<AnimeEntity?> {
        return animeRepository.getAnime(animeSlugId).asLiveData(viewModelScope.coroutineContext)
    }

    fun getEpisodeList(animeId: Int): LiveData<List<EpisodeAdapter.Episode>> {
        return animeRepository.getEpisodeList(animeId).asLiveData(viewModelScope.coroutineContext)
    }

}
