package com.otaku.kickassanime.page.animepage

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
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
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.flow.Flow


@Composable
fun AnimeScreen(
    modifier: Modifier,
    state: State,
    getAnime: () -> Flow<AnimeEntity?>,
    getLanguage: () -> Flow<List<AnimeLanguageEntity>>,
    getEpisodeList: (String?) -> Flow<PagingData<EpisodeTile>>,
    onEpisodeClick: (EpisodeTile) -> Unit
) {
    val anime = getAnime().collectAsStateWithLifecycle(null)
    anime.value?.let { animeEntity ->
        var nextModifier: Modifier = modifier

        if (state is State.LOADING) {
            nextModifier = Modifier.shimmer()
        } else if (state is State.FAILED) {
            UiUtils.showError(
                state.exception,
                LocalContext.current as AppCompatActivity
            )
            return
        }

        val languages by getLanguage().collectAsStateWithLifecycle(
            initialValue = emptyList()
        )

        var language by remember { mutableStateOf(languages.firstOrNull()) }

        val episodes = getEpisodeList(language?.language)
            .collectAsLazyPagingItems()

        LazyVerticalGrid(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = nextModifier
                .padding(12.dp, 0.dp, 24.dp, 0.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            columns = GridCells.Adaptive(140.dp)
        ) {
            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                AnimeDetails(animeEntity = animeEntity)
            }
            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                if (languages.isNotEmpty()) {
                    ComboBox(label = "Language",
                        options = languages,
                        toString = { it?.language ?: "Select language" },
                        getItem = { language }
                    ) {
                        language = it
                    }
                }
            }
            items(episodes.itemCount, span = {
                GridItemSpan(1)
            }) {
                val episodeTile = episodes[it]
                if (episodeTile != null) {
                    Episode(episodeTile, onEpisodeClick)
                }
            }
            val loadState = episodes.loadState.mediator
            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                if (loadState?.refresh == LoadState.Loading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            modifier = Modifier.padding(8.dp), text = "Refresh Loading"
                        )
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                if (loadState?.append == LoadState.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                if (loadState?.refresh is LoadState.Error || loadState?.append is LoadState.Error) {
                    val isPaginatingError =
                        (loadState.append is LoadState.Error) || episodes.itemCount > 1
                    val error =
                        if (loadState.append is LoadState.Error) (loadState.append as LoadState.Error).error
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ComboBox(
    label: String, options: List<T>,
    getItem: () -> T,
    toString: (T) -> String,
    selectItem: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }


    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = {
        expanded = !expanded
    }) {
        OutlinedTextField(
            readOnly = true,
            value = toString(getItem()),
            onValueChange = { },
            label = { MarqueeText(label, Modifier.fillMaxWidth()) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            singleLine = true,
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = {
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
    Box(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp)
            .clickable {
                onEpisodeClick(episodeTile)
            },
        contentAlignment = Alignment.BottomStart
    ) {
        KickassLoadingImage(
            url = "${Strings.KICKASSANIME_URL}image/thumbnail/${episodeTile.thumbnail}",
            description = episodeTile.episodeNumber.toString(),
            modifier = Modifier.fillMaxSize()
        )
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
                        fontSize = MaterialTheme.typography.labelSmall.fontSize
                    ),
                    start, start + end
                )
            },
            color = Color.White,
            modifier = Modifier
                .padding(18.dp, 16.dp)
                .fillMaxWidth()
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
            modifier = Modifier
                .fillMaxWidth(),
            imageWidthInDp = 190.dp,
            imageHeightInDp = 240.dp,
            maxLine = 12
        )
        if (showFullText) {
            Dialog(onDismissRequest = { showFullText = false }) {
                Card(modifier = Modifier.padding(12.dp)) {
                    DialogHeader(
                        dialogTitle = animeEntity.name,
                        dialogMessage = animeEntity.description
                    )
                }
            }
        }
        Text(
            text = stringResource(id = R.string.read_more),
            color = Color(0xff64B5F6),
            modifier = Modifier
                .padding(vertical = 10.dp)
                .clickable { showFullText = true }
        )
    }
}
