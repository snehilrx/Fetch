package com.otaku.fetch.base.ui

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.snehilrx.shinebar.Shinebar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FetchScaffold(
    title: String,
    statusBarHeight: Float = 0f,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
    actions: @Composable (RowScope.() -> Unit) = {},
    content: @Composable () -> Unit,
) {
    val top = statusBarHeight.dp
    val windowInsets = WindowInsets(0.dp, top / LocalDensity.current.density, 0.dp, 0.dp)
    UpdateShineBar(-scrollBehavior.state.heightOffsetLimit, scrollBehavior.state.heightOffset.toInt())
    Box(Modifier.fillMaxSize()) {

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            contentColor = contentColorFor(backgroundColor = MaterialTheme.colorScheme.background),
            topBar = {
                LargeTopAppBar(
                    title = { Text(text = title, fontWeight = FontWeight.W800) },
                    windowInsets = windowInsets,
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                    actions = actions,
                )
            },
            contentWindowInsets = windowInsets,
        ) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .background(Color.Transparent)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun UpdateShineBar(totalScrollRange: Float, heightOffset: Int) {
    val shineBarInterface = LocalContext.current.getActivity() as? ShineBarInterface
    shineBarInterface?.shinebar?.redrawCurve(
        totalScrollRange,
        heightOffset
    )
}

fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}