package com.otaku.kickassanime.page.animepage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.otaku.fetch.base.livedata.State
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.AnimeLanguageEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeViewModel @Inject constructor(private val animeRepository: AnimeRepository) :
    ViewModel() {

    var animeLanguageState by mutableStateOf<State>(State.LOADING())

    fun getAnime(animeSlug: String): Flow<AnimeEntity?> {
        return animeRepository.getAnime(animeSlug)
    }


    private val pagers = HashMap<Pair<String, String>, Flow<PagingData<EpisodeTile>>>()

    @OptIn(FlowPreview::class)
    fun getEpisodeList(
        animeSlug: String,
        language: String?
    ): Flow<PagingData<EpisodeTile>> {
        return if (language == null) {
            emptyFlow()
        } else {
            pagers.getOrPut(Pair(animeSlug, language)) {
                flow {
                    emit(animeRepository.getPagesAndSaveFirstPage(animeSlug, language))
                }
                    .flatMapConcat {
                        animeRepository.getEpisodes(animeSlug, language, it).flow
                            .cachedIn(viewModelScope)
                    }
            }
        }
    }

    fun setFavourite(animeSlug: String, checked: Boolean) {
        viewModelScope.launch {
            animeRepository.setFavourite(animeSlug, checked)
        }
    }


    fun getLanguages(animeSlug: String?): Flow<List<AnimeLanguageEntity>> {
        return animeSlug?.let { animeRepository.getAnimeLanguage(it) } ?: emptyFlow()
    }

    fun fetchLanguages(animeSlug: String) {
        viewModelScope.launch {
            animeLanguageState = try {
                animeRepository.fetchLanguage(animeSlug)
                State.SUCCESS()
            } catch (e: Exception) {
                State.FAILED(e)
            }
        }
    }

}
