package com.otaku.kickassanime.page.animepage

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.mikepenz.iconics.compose.Image
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.otaku.fetch.base.livedata.State
import com.otaku.fetch.base.ui.composepref.prefs.DialogHeader
import com.otaku.fetch.base.utils.UiUtils
import com.otaku.kickassanime.R
import com.otaku.kickassanime.Strings
import com.otaku.kickassanime.db.models.entity.AnimeEntity
import com.otaku.kickassanime.db.models.entity.AnimeLanguageEntity
import com.otaku.kickassanime.ui.composables.KickassLoadingImage
import com.otaku.kickassanime.ui.composables.MarqueeText
import com.otaku.kickassanime.ui.composables.TextAroundImage
import com.otaku.kickassanime.utils.slugToEpisodeLink
import com.otaku.kickassanime.work.DownloadAllEpisodeTask
import com.otaku.kickassanime.work.ListAllEpisodeTask
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.flow.Flow


@Composable
fun AnimeScreen(
    modifier: Modifier,
    state: State,
    getAnime: () -> Flow<AnimeEntity?>,
    getLanguage: () -> Flow<List<AnimeLanguageEntity>>,
    getEpisodeList: (String?) -> Flow<PagingData<EpisodeTile>>,
    isDownloadClicked: MutableState<Boolean>,
    onEpisodeClick: (EpisodeTile) -> Unit,
) {
    val anime = getAnime().collectAsStateWithLifecycle(null)
    anime.value?.let { animeEntity ->
        var nextModifier: Modifier = modifier

        if (state is State.LOADING) {
            nextModifier = nextModifier.shimmer()
        } else if (state is State.FAILED) {
            UiUtils.showError(
                state.exception, LocalContext.current as AppCompatActivity
            )
            return
        }

        val languages by getLanguage().collectAsStateWithLifecycle(
            initialValue = emptyList()
        )

        var language by remember { mutableStateOf(languages.firstOrNull()) }

        val episodes = getEpisodeList(language?.language).collectAsLazyPagingItems()

        DownloadAlert(getEpisodeList, animeEntity, language?.language, isDownloadClicked)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = nextModifier
                .padding(12.dp, 0.dp, 24.dp, 0.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(key = "details") {
                AnimeDetails(animeEntity = animeEntity)
            }
            item(key = "language") {
                LanguageChooser(languages, language) {
                    language = it
                }
            }
            items(episodes.itemCount,
                key = episodes.itemKey { it.slug ?: "" }) {
                val episodeTile = episodes[it]
                if (episodeTile != null) {
                    Episode(episodeTile, onEpisodeClick)
                    if (it < episodes.itemCount - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(0.dp, 8.dp)
                        )
                    }
                }
            }
            val loadState = episodes.loadState.mediator
            item(key = "load_state") {
                if (loadState?.refresh == LoadState.Loading || loadState?.append == LoadState.Loading) {
                    Loading()
                }

                if (loadState?.refresh is LoadState.Error || loadState?.append is LoadState.Error) {
                    PaginationError(loadState, episodes)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadAlert(
    getEpisodeList: (String?) -> Flow<PagingData<EpisodeTile>>,
    animeEntity: AnimeEntity,
    language: String?,
    isDownloadClicked: MutableState<Boolean>
) {
    val episodes = getEpisodeList(language).collectAsLazyPagingItems()
    if (isDownloadClicked.value) {
        val applicationContext = LocalContext.current.applicationContext
        val selectedEpisodes = remember { mutableStateMapOf<String, EpisodeTile>() }
        var selectedAll by remember { mutableStateOf(false) }
        // TriStateCheckbox state reflects state of dependent checkboxes
        val triState = remember(selectedEpisodes, selectedAll) {
            if (selectedAll) ToggleableState.On
            else when (selectedEpisodes.size) {
                episodes.itemCount -> {
                    selectedAll = true
                    ToggleableState.On
                }

                0 -> ToggleableState.Off
                else -> ToggleableState.Indeterminate
            }
        }
        if (language == null) {
            AlertDialog(
                title = { Text(text = stringResource(R.string.error)) },
                text = { Text(text = stringResource(R.string.please_select_a_language_first_to_download_episode)) },
                onDismissRequest = { isDownloadClicked.value = false },
                confirmButton = {
                    Button(onClick = { isDownloadClicked.value = false }) {
                        Text(text = stringResource(R.string.okay))
                    }
                })
            return
        }
        AlertDialog(title = { Text(text = stringResource(R.string.download_episodes)) }, text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    TriStateCheckbox(state = triState, onClick = {
                        if (triState == ToggleableState.Off) {
                            selectedAll = true
                        } else {
                            selectedAll = false
                            selectedEpisodes.clear()
                        }
                    })
                    Text(text = "Select All Episode")

                }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(64.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    items(episodes.itemCount,
                        key = episodes.itemKey { it.slug ?: "" },
                        span = { GridItemSpan(1) }) {
                        val episode = episodes[it] ?: return@items
                        FilterChip(
                            modifier = Modifier.padding(2.dp, 2.dp),
                            selected = selectedAll.or(selectedEpisodes.containsKey(episode.slug)),
                            onClick = {
                                if (selectedEpisodes.containsKey(episode.slug)) {
                                    selectedAll = false
                                    selectedEpisodes.remove(episode.slug)
                                } else if (episode.slug != null) {
                                    selectedEpisodes[episode.slug] = episode
                                }
                            },
                            label = {
                                Text(text = "${episode.episodeNumber}")
                            })
                    }
                }
            }
        }, onDismissRequest = { isDownloadClicked.value = false }, confirmButton = {
            Button(onClick = {
                isDownloadClicked.value = false
                enqueueDownloadEpisodeWork(
                    selectedEpisodes, selectedAll, animeEntity, language, applicationContext
                )
            }) {
                Text(text = stringResource(R.string.download))
            }
        }, dismissButton = {
            Button(onClick = { isDownloadClicked.value = false }) {
                Text(text = "Cancel")
            }
        })
    }

}

fun enqueueDownloadEpisodeWork(
    selectedEpisodes: SnapshotStateMap<String, EpisodeTile>,
    downloadAll: Boolean,
    animeEntity: AnimeEntity,
    language: String,
    context: Context
) {
    val animeSlug = animeEntity.animeSlug
    val listEpisodes = OneTimeWorkRequest.Builder(ListAllEpisodeTask::class.java).setInputData(
        ListAllEpisodeTask.createNewInput(
            animeSlug, language
        )
    ).setConstraints(
        Constraints.Builder().setRequiredNetworkType(networkType = NetworkType.CONNECTED).build()
    )
    val download = OneTimeWorkRequest.Builder(DownloadAllEpisodeTask::class.java).setConstraints(
        Constraints.Builder().setRequiredNetworkType(networkType = NetworkType.CONNECTED).build()
    )
    val workManager = WorkManager.getInstance(context)
    if (downloadAll) {
        workManager.beginWith(listEpisodes.build()).then(download.build()).enqueue()
    } else {
        val slugs = selectedEpisodes.map { it.value.slug }
        workManager.enqueue(
            download.setInputData(
                DownloadAllEpisodeTask.createNewInput(
                    slugs.mapNotNull { it?.slugToEpisodeLink(animeSlug) }.toTypedArray(),
                    slugs.mapNotNull { it }.toTypedArray(),
                    animeSlug
                )
            ).build()
        )
    }
}

@Composable
private fun Loading() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier.padding(8.dp), text = "Refreshing"
        )
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun PaginationError(
    loadState: LoadStates, episodes: LazyPagingItems<EpisodeTile>
) {
    val isPaginatingError = (loadState.append is LoadState.Error) || episodes.itemCount > 1
    val error = if (loadState.append is LoadState.Error) (loadState.append as LoadState.Error).error
    else (loadState.refresh as LoadState.Error).error

    Column(
        modifier = if (isPaginatingError) {
            Modifier.padding(8.dp)
        } else {
            Modifier.fillMaxSize()
        },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!isPaginatingError) {
            Icon(
                modifier = Modifier.size(64.dp),
                imageVector = Icons.Rounded.Warning,
                contentDescription = null
            )
        }

        Text(
            modifier = Modifier.padding(8.dp),
            text = error.message ?: error.toString(),
            textAlign = TextAlign.Center,
        )

        Button(onClick = {
            episodes.refresh()
        }, content = {
            Text(text = "Refresh")
        }, colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        )
        )
    }
}

@Composable
private fun LanguageChooser(
    languages: List<AnimeLanguageEntity>,
    language: AnimeLanguageEntity?,
    onChange: (AnimeLanguageEntity?) -> Unit
) {
    if (languages.isNotEmpty()) {
        ComboBox(label = "Language",
            options = languages,
            modifier = Modifier.wrapContentWidth(),
            toString = { it?.language ?: "Select language" },
            getItem = { language }) {
            onChange(it)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ComboBox(
    label: String,
    options: List<T>,
    modifier: Modifier = Modifier,
    getItem: () -> T,
    toString: (T) -> String,
    selectItem: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        modifier = modifier,
        onExpandedChange = {
            expanded = !expanded
        }) {
        OutlinedTextField(
            readOnly = true,
            value = toString(getItem()),
            onValueChange = { },
            label = {
                MarqueeText(
                    label,
                    Modifier.background(Color.Transparent)
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            singleLine = true,
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            modifier = modifier,
            onDismissRequest = {
                expanded = false
            }) {
            options.forEach { selectionOption ->
                DropdownMenuItem(onClick = {
                    selectItem(selectionOption)
                    expanded = false
                }, text = {
                    Text(text = toString(selectionOption))
                })
            }
        }
    }
}

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun Episode(episodeTile: EpisodeTile, onEpisodeClick: (EpisodeTile) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(0.dp)
            .fillMaxSize()
            .clickable {
                onEpisodeClick(episodeTile)
            },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val image = Modifier
            .height(120.dp)
            .width(180.dp)
        if (!episodeTile.thumbnail.isNullOrBlank()) {
            KickassLoadingImage(
                url = "${Strings.KICKASSANIME_URL}image/thumbnail/${episodeTile.thumbnail}",
                description = episodeTile.episodeNumber.toString(),
                modifier = image,
                contentScale = ContentScale.FillBounds,
            )
        } else {
            Image(
                modifier = image,
                asset = FontAwesome.Icon.faw_image,
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSurface)
            )
        }
        Text(
            text = buildAnnotatedString {
                val text = stringResource(id = R.string.episode_tile_text)
                append(text)
                val episodeNumber = episodeTile.episodeNumber.toString()
                append(episodeNumber)
                appendLine()
                append(episodeTile.title)
                appendLine()
                val readableDuration = episodeTile.readableDuration()
                append(readableDuration)
                val start = text.length + episodeNumber.length
                val end = (episodeTile.title?.length ?: 0) + readableDuration.length
                addStyle(
                    SpanStyle(
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    ), start, start + end
                )
            },
            modifier = Modifier
                .padding(18.dp, 16.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
        )
    }

}

@Composable
fun AnimeDetails(animeEntity: AnimeEntity) {
    var showFullText by remember {
        mutableStateOf(false)
    }
    Column {
        TextAroundImage(
            imageUrl = animeEntity.getImageUrl(),
            text = animeEntity.description ?: "",
            modifier = Modifier.fillMaxWidth(),
            imageWidthInDp = 190.dp,
            imageHeightInDp = 240.dp,
            maxLine = 12
        )
        if (showFullText) {
            Dialog(onDismissRequest = { showFullText = false }) {
                Card(modifier = Modifier.padding(12.dp)) {
                    DialogHeader(
                        dialogTitle = animeEntity.name, dialogMessage = animeEntity.description
                    )
                }
            }
        }
        Text(text = stringResource(id = R.string.read_more),
            color = Color(0xff64B5F6),
            modifier = Modifier
                .padding(vertical = 10.dp)
                .clickable { showFullText = true })
    }
}
