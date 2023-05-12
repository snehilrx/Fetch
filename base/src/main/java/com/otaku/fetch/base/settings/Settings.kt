package com.otaku.fetch.base.settings

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController
import com.otaku.fetch.base.ui.composepref.GroupHeader
import com.otaku.fetch.base.ui.composepref.PrefsScreen
import com.otaku.fetch.base.ui.composepref.prefs.ListPref
import com.otaku.fetch.base.ui.composepref.prefs.SwitchPref

private val Context.dataStore by preferencesDataStore(name = "settings")

@Composable
@Preview(heightDp = 700, widthDp = 300)
fun Preview() {
    MaterialTheme {
        Settings()
    }
}

object Settings {
    @JvmStatic
    val SKIP_ENABLED = booleanPreferencesKey("pref_skip_enabled")

    @JvmStatic
    val SKIP_PROVIDER = stringSetPreferencesKey("skip_provider")

    @JvmStatic
    val NOTIFICATION_ENABLED = booleanPreferencesKey("pref_notif_enabled")

    @JvmStatic
    val STREAM_VIDEO_QUALITY = stringSetPreferencesKey("stream_video_quality")

    @JvmStatic
    val STREAM_AUDIO_QUALITY = stringSetPreferencesKey("stream_audio_quality")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun Settings() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = "Setting", fontWeight = FontWeight.W800) },
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
                scrollBehavior = scrollBehavior
            )
        },
    ) {

        val dataStore = LocalContext.current.dataStore
        val pref by dataStore.data.collectAsState(initial = null)
        PrefsScreen(modifier = Modifier.padding(it), dataStore = dataStore) {
            prefsGroup({
                GroupHeader(
                    title = "Features",
                    color = MaterialTheme.colorScheme.secondary
                )
            }) {
                prefsItem {
                    SwitchPref(
                        key = Settings.SKIP_ENABLED.name,
                        title = "Episode skip",
                        summary = "Skips intro, filler, etc"
                    )
                }
                if (pref?.get(Settings.SKIP_ENABLED) == true) {
                    prefsItem {
                        ListPref(key = Settings.SKIP_PROVIDER.name, title = "Skip service")
                    }
                }
                prefsItem {
                    SwitchPref(
                        key = Settings.NOTIFICATION_ENABLED.name,
                        title = "New videos notification",
                        summary = "Get new video releases notification"
                    )
                }
            }
            prefsGroup({
                GroupHeader(
                    title = "Streaming",
                    color = MaterialTheme.colorScheme.secondary
                )
            }) {
                prefsItem {
                    ListPref(
                        key = Settings.STREAM_VIDEO_QUALITY.name,
                        title = "Video Quality"
                    )
                }
                prefsItem {
                    ListPref(
                        key = Settings.STREAM_AUDIO_QUALITY.name,
                        title = "Audio Quality"
                    )
                }
            }
            prefsGroup({
                GroupHeader(
                    title = "Downloading",
                    color = MaterialTheme.colorScheme.secondary
                )
            }) {
                prefsItem {
                    ListPref(
                        key = Settings.STREAM_VIDEO_QUALITY.name,
                        title = "Video Quality"
                    )
                }
                prefsItem {
                    ListPref(
                        key = Settings.STREAM_AUDIO_QUALITY.name,
                        title = "Audio Quality"
                    )
                }
            }
        }
    }
}