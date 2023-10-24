package com.otaku.kickassanime.ui.composables

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.valentinilk.shimmer.shimmer
import eu.wewox.textflow.TextFlow
import eu.wewox.textflow.TextFlowObstacleAlignment

@Composable
fun TextAroundImage(
    text: String, imageUrl: String,
    imageHeightInDp: Dp,
    imageWidthInDp: Dp,
    maxLine: Int,
    modifier: Modifier
) {
    TextFlow(
        text = text,
        maxLines = maxLine,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier,
        obstacleAlignment = TextFlowObstacleAlignment.TopStart,
        obstacleContent = {
            KickassLoadingImage(
                url = imageUrl,
                description = text,
                modifier = Modifier
                    .height(imageHeightInDp)
                    .width(imageWidthInDp)
                    .padding(0.dp, 0.dp, 16.dp, 0.dp)
            )
        },
        softWrap = true,
        overflow = TextOverflow.Ellipsis,
        color = LocalContentColor.current
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun KickassLoadingImage(
    url: String?, description: String, modifier: Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onDrawable: (Drawable) -> Unit = {}
) {
    if (url?.isNotEmpty() == true) {
        val urls = arrayOf(
            "${url}-hq.webp",
            "${url}-hq.jpg",
            "${url}-hq.jpeg",
            "${url}-hq.webp",
            "${url}-hq.jpg",
            "${url}-hq.jpeg"
        )
        val padding = modifier.padding(4.dp)
        var shimmer by remember {
            mutableStateOf(padding.shimmer())
        }

        GlideImage(
            model = urls[0],
            contentDescription = description,
            modifier = shimmer,
            contentScale = contentScale
        ) {
            it.error {
                it.load(urls[1]).error {
                    it.load(urls[2]).error {
                        it.load(urls[3]).error {
                            it.load(urls[4]).error {
                                it.load(urls[5]).error(android.R.drawable.stat_notify_error)
                            }
                        }
                    }
                }
            }.listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    shimmer = padding
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    onDrawable(resource)
                    shimmer = padding
                    return false
                }
            }).centerCrop()
                .transform(RoundedCorners(24.dp.value.toInt()))
        }
    }
}


@Preview("Text around image")
@Composable
fun TextAroundImagePreview() {
    TextAroundImage(
        text = "dfgdfsgsdfg".repeat(200),
        imageUrl = "https://www2.kickassanime.ro/image/poster/my-hero-academia-dubs-",
        imageHeightInDp = 120.dp,
        imageWidthInDp = 90.dp,
        modifier = Modifier.fillMaxSize(),
        maxLine = 10
    )
}