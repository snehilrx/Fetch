package com.otaku.fetch.base.download

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.StreamKey
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.ui.R
import androidx.navigation.compose.rememberNavController
import com.otaku.fetch.base.ui.lazytree.ItemPlacement
import com.otaku.fetch.base.ui.lazytree.ItemTree
import com.otaku.fetch.base.ui.lazytree.LazyTreeList
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import kotlin.math.abs
import kotlin.math.min


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@androidx.annotation.OptIn(UnstableApi::class)
fun DownloadScreen(
    downloadsVM: DownloadViewModel,
) {
    val navController = rememberNavController()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val items = downloadsVM.anime()
    downloadsVM.refreshDownloadState()
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(title = { Text(text = "Downloads", fontWeight = FontWeight.W800) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
                modifier = Modifier.statusBarsPadding(),
                scrollBehavior = scrollBehavior,
                actions = {
                    PlayPauseButton(pauseAction = {
                        downloadsVM.pause(context)
                    }, playAction = {
                        downloadsVM.resume(context)
                    }, isPaused = downloadsVM.isDownloadPaused)
                })
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            DownloadList(
                items.toItemTreeIndex(downloadsVM), downloadsVM
            )
        }
    }
}

@Composable
@androidx.annotation.OptIn(UnstableApi::class)
@Preview
fun PlayPauseButton(
    playAction: () -> Unit = {},
    pauseAction: () -> Unit = {},
    isPaused: MutableState<Boolean> = mutableStateOf(false)
) {
    var toggleableState by remember { isPaused }
    IconButton(onClick = {
        if (toggleableState) {
            playAction()
        } else {
            pauseAction()
        }
        toggleableState = !toggleableState
    }) {
        if (toggleableState) {
            Icon(
                painter = painterResource(id = R.drawable.exo_icon_play),
                contentDescription = "Localized description"
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.exo_icon_pause),
                contentDescription = "Localized description"
            )
        }
    }
}

@Composable
@androidx.annotation.OptIn(UnstableApi::class)
fun DownloadList(tree: ItemTree, downloadsVM: DownloadViewModel) {
    LazyTreeList(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp),
        items = tree
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "NO DOWNLOAD",
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W800
            )
        }
    }
    downloadsVM.attach()
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
private fun DownloadRepository.TreeNode.toItemTreeIndex(
    downloadsVM: DownloadViewModel
): ItemTree {
    return object : ItemTree() {
        override val size: Int
            get() = children.size

        override fun expand(index: Int): ItemTree? {
            return when (children[index]) {
                is DownloadRepository.Anime -> {
                    return (children[index] as? DownloadRepository.TreeNode)?.toItemTreeIndex(
                        downloadsVM
                    )
                }

                is DownloadRepository.Episode -> {
                    return (children[index] as? DownloadRepository.TreeNode)?.toItemTreeIndex(
                        downloadsVM
                    )
                }

                else -> null
            }
        }

        @androidx.annotation.OptIn(UnstableApi::class)
        override fun key(index: Int): Any {
            return when (children.getOrNull(index)) {
                is DownloadRepository.Anime -> (children[index] as DownloadRepository.Anime).anime
                is DownloadRepository.Episode -> (children[index] as DownloadRepository.Episode).episode
                is DownloadRepository.Link -> (children[index] as DownloadRepository.Link).download.download.request.id
                else -> 0
            }
        }

        @Composable
        @androidx.annotation.OptIn(UnstableApi::class)
        override fun LazyItemScope.Item(
            index: Int,
            depth: Int,
            placement: ItemPlacement,
            expanded: Boolean,
            onExpand: () -> Unit
        ) {
            val localContext = LocalContext.current
            DepthStyle(
                depth = depth
            ) {
                val modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement()
                    .animateContentSize()
                when (val item = children.getOrNull(index)) {
                    is DownloadRepository.Anime -> NodeItem(
                        name = item.animeName,
                        isExpanded = expanded,
                        onExpand = onExpand,
                        modifier = modifier,
                        color = it
                    ) {
                        downloadsVM.deleteAnime(
                            children[index] as DownloadRepository.Anime, localContext
                        )
                    }

                    is DownloadRepository.Episode -> NodeItem(
                        name = "Episode ${item.episodeNumber.toInt()}",
                        isExpanded = expanded,
                        onExpand = onExpand,
                        modifier = modifier,
                        color = it
                    ) {
                        downloadsVM.deleteEpisode(
                            children[index] as DownloadRepository.Episode, localContext
                        )
                    }

                    is DownloadRepository.Link -> LeafItem(children[index] as DownloadRepository.Link,
                        color = it,
                        deleteAction = {
                            downloadsVM.deleteLink(
                                children[index] as DownloadRepository.Link, localContext
                            )
                        },
                        resumeAction = {
                            downloadsVM.resume(localContext)
                        },
                        playAction = {
                            val bundle = item.download.launchBundle
                            val additionalData =
                                item.download.download.request.toOfflineBundle()
                            bundle.putBundle("mediaItem", additionalData)
                            localContext.startActivity(Intent(
                                localContext, item.download.launchActivity
                            ).apply {
                                putExtras(bundle)
                            })
                        })
                }
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
private fun DownloadRequest.toOfflineBundle(): Bundle {
    return bundleOf(
        "id" to id,
        "uri" to uri,
        "key" to customCacheKey,
        "mime" to mimeType,
        "streamKey" to streamKeys
    )
}


@Suppress("deprecation")
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun Bundle.toMediaItem(): MediaItem {
    val keys = ArrayList<StreamKey>()
    return MediaItem.Builder()
        .setMediaId(getString("id", ""))
        .setUri(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelable("uri", Uri::class.java)
            } else {
                getParcelable("uri")
            }
        )
        .setCustomCacheKey(getString("key"))
        .setMimeType(getString("mime"))
        .setStreamKeys(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelableArray("streamKey", StreamKey::class.java)
            } else {
                getParcelableArray("streamKey")
            }?.mapNotNull { it as? StreamKey }?.toCollection(keys)
        )
        .build()
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun LeafItem(
    downloadLink: DownloadRepository.Link,
    color: Color,
    deleteAction: () -> Unit,
    resumeAction: () -> Unit,
    playAction: () -> Unit,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val download = downloadLink.download.download
    Card(colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                size = it
            }) {
        Box(contentAlignment = Alignment.Center) {
            LinearProgressIndicator(
                progress = download.percentDownloaded / 100f,
                modifier = Modifier
                    .height(42.dp)
                    .width(size.width.dp)
            )
            Text(
                text = String.format("%.2f", download.percentDownloaded) + " %",
                color = MaterialTheme.colorScheme.inverseOnSurface
            )
        }
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SuggestionChip(label = { Text(text = "Episode") }, onClick = {}, enabled = false
            )
            Column(modifier = Modifier.weight(2f), horizontalAlignment = Alignment.End) {
                DeleteButton(
                    onClick = {
                        deleteAction()
                    }, warning = "Do you want to delete the selected episode"
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(16.dp)
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = "Downloaded : ${readableDownloadSize(download.bytesDownloaded)}",
                textAlign = TextAlign.Start
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                val (label, action) = getStateTextAndAction(
                    download.state, resumeAction, playAction
                )
                SuggestionChip(label = {
                    Text(label)
                }, onClick = { action?.invoke() }, enabled = action != null
                )
            }
        }
    }
}

@Composable
private fun NodeItem(
    name: String,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    modifier: Modifier,
    color: Color,
    deleteAction: () -> Unit
) {
    Card(
        modifier = modifier
            .height(50.dp)
            .fillMaxWidth()
            .clickable { onExpand() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            Modifier
                .padding(horizontal = 8.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                name, modifier = Modifier.weight(6f)
            )

            DeleteButton(
                modifier = Modifier.weight(3f),
                warning = "Do you want to delete $name",
                onClick = deleteAction
            )
            Icon(
                if (!isExpanded) Icons.Default.KeyboardArrowRight
                else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteButton(
    modifier: Modifier = Modifier, onClick: () -> Unit, warning: String
) {
    var warn by remember {
        mutableStateOf(false)
    }
    if (warn) {
        AlertDialog(title = {
            Text(text = "Delete")
        }, text = {
            Text(text = warning)
        }, onDismissRequest = { warn = false }, confirmButton = {
            Button(onClick = {
                warn = false
                onClick()
            }) {
                Text(text = "Okay")
            }
        }, dismissButton = {
            Button(onClick = {
                warn = false
            }) {
                Text(text = "Cancel")
            }
        })
    }
    SuggestionChip(label = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Delete, contentDescription = null
            )
            Text(text = "DELETE")
        }
    }, onClick = {
        warn = true
    }, modifier = modifier
    )
}

@Composable
inline fun DepthStyle(
    depth: Int,
    crossinline content: @Composable (Color) -> Unit,
) {
    content(MaterialTheme.colorScheme.surfaceVariant.darker(((depth + 12) / 15f)))
}

fun Color.darker(factor: Float): Color {
    return Color(
        min(red * factor, 255f),
        min(green * factor, 255f),
        min(blue * factor, 255f),
        alpha,
    )
}

@androidx.annotation.OptIn(UnstableApi::class)
fun getStateTextAndAction(
    @Download.State state: Int,
    resumeAction: () -> Unit,
    playAction: () -> Unit,
): Pair<String, (() -> Unit)?> {
    return when (state) {
        Download.STATE_COMPLETED -> {
            Pair("Play", playAction)
        }

        Download.STATE_DOWNLOADING -> {
            Pair("Downloading", null)
        }

        Download.STATE_FAILED -> {
            Pair("Failed", resumeAction)
        }

        Download.STATE_QUEUED -> {
            Pair("Queued", null)
        }

        Download.STATE_REMOVING -> {
            Pair("Deleting", null)
        }

        Download.STATE_RESTARTING -> {
            Pair("Restarting", null)
        }

        Download.STATE_STOPPED -> {
            Pair("Stopped", resumeAction)
        }

        else -> {
            Pair("", null)
        }
    }
}

private fun readableDownloadSize(bytes: Long): String {
    val absB = if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else abs(bytes)
    if (absB < 1024) {
        return "$bytes B"
    }
    var value = absB
    val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
    var i = 40
    while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
        value = value shr 10
        ci.next()
        i -= 10
    }
    value *= java.lang.Long.signum(bytes).toLong()
    return String.format("%.1f %ciB", value / 1024.0, ci.current())
}
