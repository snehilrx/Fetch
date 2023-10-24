package com.otaku.fetch.base.download

import android.content.Intent
import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import com.mikepenz.iconics.compose.Image
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.otaku.fetch.base.R
import com.otaku.fetch.base.ui.FetchScaffold
import com.otaku.fetch.base.ui.lazytree.ItemPlacement
import com.otaku.fetch.base.ui.lazytree.ItemTree
import com.otaku.fetch.base.ui.lazytree.LazyTreeList
import io.github.snehilrx.shinebar.Shinebar
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import kotlin.math.abs
import kotlin.math.min


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@androidx.annotation.OptIn(UnstableApi::class)
fun DownloadScreen(
    downloadsVM: DownloadViewModel,
    statusBarHeight: Float? = null,
    setupShineBar: (Shinebar) -> Unit = { _ -> run {} }
) {

    val items = downloadsVM.anime()
    val context = LocalContext.current.applicationContext
    FetchScaffold(
        title = stringResource(id = R.string.downloads),
        statusBarHeight ?: 0f,
        actions = {
            PlayPauseButton(pauseAction = {
                downloadsVM.pause(context)
            }, playAction = {
                downloadsVM.resume(context)
            }, isPaused = downloadsVM.isDownloadPaused)
        },
        setupShineBar = setupShineBar
    ) {
        DownloadList(items.toItemTreeIndex(downloadsVM))
    }
    downloadsVM.attach()
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
            Image(
                asset = FontAwesome.Icon.faw_play,
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSurface)
            )
        } else {
            Image(
                asset = FontAwesome.Icon.faw_pause,
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}

@Composable
@androidx.annotation.OptIn(UnstableApi::class)
fun DownloadList(tree: ItemTree) {
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
                text = stringResource(R.string.no_download),
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W800
            )
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
private fun DownloadRepository.TreeNode<*>.toItemTreeIndex(
    downloadsVM: DownloadViewModel
): ItemTree {
    val node = this
    return object : ItemTree() {
        override val size: Int
            get() = node.size

        override fun expand(index: Int): ItemTree? {
            return when (val child = node[index]) {
                is DownloadRepository.Episode, is DownloadRepository.Anime ->
                    return (child as? DownloadRepository.TreeNode<*>)?.toItemTreeIndex(
                        downloadsVM
                    )

                else -> null
            }
        }

        @androidx.annotation.OptIn(UnstableApi::class)
        override fun key(index: Int): Any {
            return when (val child = node[index]) {
                is DownloadRepository.Node -> child.key
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
            val applicationContext = LocalContext.current.applicationContext
            val context = LocalContext.current
            DepthStyle(
                depth = depth
            ) {
                val modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement()
                    .animateContentSize()
                when (val item = node[index]) {
                    is DownloadRepository.Anime -> NodeItem(
                        name = item.animeName,
                        isExpanded = expanded,
                        onExpand = onExpand,
                        modifier = modifier,
                        color = it
                    ) {
                        downloadsVM.deleteAnime(item, applicationContext)
                    }

                    is DownloadRepository.Episode -> NodeItem(
                        name = "Episode ${item.episodeNumber.toInt()}",
                        isExpanded = expanded,
                        onExpand = onExpand,
                        modifier = modifier,
                        color = it
                    ) {
                        downloadsVM.deleteEpisode(item, applicationContext)
                    }

                    is DownloadRepository.Link -> LeafItem(
                        item,
                        color = it,
                        deleteAction = {
                            downloadsVM.deleteLink(item, applicationContext)
                        },
                        retryAction = {
                            downloadsVM.retry(item, applicationContext)
                        },
                        playAction = {
                            val bundle = item.download.launchBundle
                            val additionalData =
                                item.download.download.value.request.toOfflineBundle()
                            bundle.putBundle("mediaItem", additionalData)
                            context.startActivity(Intent(
                                context, item.download.launchActivity
                            ).apply {
                                putExtras(bundle)
                            })
                        },
                        pauseAction = {
                            downloadsVM.pause(
                                item,
                                applicationContext
                            )
                        },
                        resumeAction = {
                            downloadsVM.resume(
                                item,
                                applicationContext
                            )
                        }
                    )
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


@OptIn(ExperimentalFoundationApi::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun LeafItem(
    downloadLink: DownloadRepository.Link,
    color: Color,
    deleteAction: () -> Unit,
    retryAction: () -> Unit,
    playAction: () -> Unit,
    pauseAction: () -> Unit,
    resumeAction: () -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val download by remember { downloadLink.download.download }
    val (label, action) = getStateTextAndAction(
        download.state, retryAction, playAction, pauseAction, resumeAction,
    )

    var warn by remember {
        mutableStateOf(false)
    }
    if (warn) {
        DeleteWarning("Do you want to delete the selected episode?", deleteAction) {
            warn = false
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = { action?.invoke() },
                onLongClick = {
                    warn = true
                },
                onLongClickLabel = "Delete",
                onClickLabel = "Play"
            )
            .onSizeChanged {
                size = it
            }) {
        Box(contentAlignment = Alignment.Center) {
            LinearProgressIndicator(
                progress = download.percentDownloaded / 100f,
                modifier = Modifier
                    .matchParentSize()
                    .width(size.width.dp)
            )
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(16.dp)
                    .width(size.width.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val displayMedium = MaterialTheme.typography.displayMedium
                val style = displayMedium.copy(fontSize = displayMedium.fontSize * 0.6)
                Text(
                    text = label?.uppercase() ?: "",
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = style,
                )
                Text(
                    text = String.format("%.2f", download.percentDownloaded) + " %",
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = readableDownloadSize(download.bytesDownloaded),
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.bodySmall
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeleteButton(
                modifier = Modifier
                    .wrapContentWidth(),
                warning = "Do you want to delete $name",
                onClick = deleteAction
            )
            Text(
                name, modifier = Modifier.weight(1f)
            )
            Icon(
                if (!isExpanded) Icons.AutoMirrored.Filled.KeyboardArrowRight
                else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier
                    .wrapContentWidth(),
            )
        }
    }
}

@Composable
private fun DeleteButton(
    modifier: Modifier = Modifier, onClick: () -> Unit, warning: String
) {
    var warn by remember {
        mutableStateOf(false)
    }
    if (warn) {
        DeleteWarning(warning, onClick) {
            warn = false
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(4.dp)
            .clickable {
                warn = true
            }
    ) {
        Icon(
            Icons.Default.Delete, contentDescription = null
        )
    }
}

@Composable
private fun DeleteWarning(
    warning: String,
    onClick: () -> Unit,
    dismiss: () -> Unit
) {
    AlertDialog(title = {
        Text(text = stringResource(R.string.delete))
    }, text = {
        Text(text = warning)
    }, onDismissRequest = {
        dismiss()
    }, confirmButton = {
        Button(onClick = {
            dismiss()
            onClick()
        }) {
            Text(text = stringResource(R.string.okay))
        }
    }, dismissButton = {
        Button(onClick = {
            dismiss()
        }) {
            Text(text = stringResource(R.string.cancel))
        }
    })
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
    retryAction: () -> Unit,
    playAction: () -> Unit,
    pauseAction: () -> Unit,
    resumeAction: () -> Unit
): Pair<String?, (() -> Unit)?> {
    return when (state) {
        Download.STATE_COMPLETED -> {
            Pair("Play", playAction)
        }

        Download.STATE_FAILED -> {
            Pair("Retry", retryAction)
        }

        Download.STATE_DOWNLOADING,
        Download.STATE_QUEUED -> {
            Pair("Downloading", pauseAction)
        }

        Download.STATE_STOPPED -> {
            Pair("Paused", resumeAction)
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
