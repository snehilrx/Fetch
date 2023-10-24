package com.otaku.fetch.base.settings

import android.content.Context
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.otaku.fetch.base.R
import com.otaku.fetch.base.askNotificationPermission
import com.otaku.fetch.base.ui.BindingActivity
import com.otaku.fetch.base.ui.FetchScaffold
import com.otaku.fetch.base.ui.composepref.GroupHeader
import com.otaku.fetch.base.ui.composepref.PrefsScreen
import com.otaku.fetch.base.ui.composepref.prefs.ListPref
import com.otaku.fetch.base.ui.composepref.prefs.SwitchPref
import dagger.hilt.android.internal.managers.ViewComponentManager.FragmentContextWrapper
import io.github.snehilrx.shinebar.Shinebar
import java.util.Locale


val Context.dataStore by preferencesDataStore(name = "settings_v2")

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
    val NOTIFICATION_ENABLED = booleanPreferencesKey("pref_notif_enabled")

    @JvmStatic
    val STREAM_VIDEO_QUALITY = stringPreferencesKey("stream_video_quality")

    @JvmStatic
    val AUDIO_LANGUAGE = stringPreferencesKey("audio_language")

    @JvmStatic
    val SUBTITLE_LANGUAGE = stringPreferencesKey("sub_language")

    @JvmStatic
    val DOWNLOADS_VIDEO_QUALITY = stringPreferencesKey("download_video_quality")

    @JvmStatic
    val AUTO_RESUME = booleanPreferencesKey("auto_resume")

    @JvmStatic
    val PREF_DEFAULTS_SET = booleanPreferencesKey("is_defaults_set")

    @JvmStatic
    val PREF_NEW_UPDATE_FOUND = booleanPreferencesKey("new_update")

    @JvmStatic
    val PREF_NEW_UPDATE_SHOWN_COUNT = intPreferencesKey("new_update_shown")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    statusBarHeight: Float? = null, setupShineBar: (Shinebar) -> Unit = { _ -> run {} }
) {
    FetchScaffold(title = stringResource(id = R.string.settings),
        statusBarHeight = statusBarHeight ?: 0f,
        setupShineBar = setupShineBar,
        content = {
            val context = LocalContext.current
            val dataStore = context.dataStore
            val pref by dataStore.data.collectAsStateWithLifecycle(initialValue = null)
            if (pref?.get(Settings.NOTIFICATION_ENABLED) == true) {
                val askNotificationPermission =
                    ((context as? FragmentContextWrapper)?.baseContext as? BindingActivity<*>)?.askNotificationPermission()
                if (askNotificationPermission == false) {
                    LaunchedEffect(key1 = Settings.NOTIFICATION_ENABLED, block = {
                        dataStore.edit {
                            it[Settings.NOTIFICATION_ENABLED] = false
                        }
                    })
                }
            }
            PrefsScreen(dataStore = dataStore) {
                prefsGroup({
                    GroupHeader(
                        title = stringResource(id = R.string.feature),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }) {
                    prefsItem {
                        SwitchPref(
                            key = Settings.SKIP_ENABLED.name,
                            title = stringResource(id = R.string.episode_skip),
                            summary = stringResource(id = R.string.episode_skip_desc)
                        )
                    }
                    prefsItem {
                        SwitchPref(
                            key = Settings.NOTIFICATION_ENABLED.name,
                            title = stringResource(id = R.string.notifications),
                            summary = stringResource(id = R.string.notifications_desc)
                        )
                    }
                    prefsItem {
                        SwitchPref(
                            key = Settings.AUTO_RESUME.name,
                            title = stringResource(id = R.string.auto_resume_videos),
                            summary = stringResource(id = R.string.auto_resume_videos_desc)
                        )
                    }
                }
                prefsGroup({
                    GroupHeader(
                        title = stringResource(id = R.string.language),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }) {
                    prefsItem {
                        LanguageSetting(
                            stringResource(id = R.string.subtitle_language),
                            Settings.SUBTITLE_LANGUAGE,
                            pref
                        )
                    }
                    prefsItem {
                        LanguageSetting(
                            stringResource(id = R.string.audio_language),
                            Settings.AUDIO_LANGUAGE,
                            pref
                        )
                    }
                }
                prefsGroup({
                    GroupHeader(
                        title = stringResource(id = R.string.streaming),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }) {
                    prefsItem {
                        VideoSetting(Settings.STREAM_VIDEO_QUALITY, pref)
                    }
                }
                prefsGroup({
                    GroupHeader(
                        title = stringResource(id = R.string.downloading),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }) {
                    prefsItem {
                        VideoSetting(Settings.DOWNLOADS_VIDEO_QUALITY, pref)
                    }
                }
            }
        })
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun VideoSetting(
    key: Preferences.Key<String>,
    pref: Preferences?
) {
    val qualities = stringArrayResource(id = R.array.video_qualities)

    val entries by remember(qualities) {
        derivedStateOf {
            qualities.mapIndexed { index, s ->
                index.toString() to s
            }.toMap()
        }
    }
    val summary = pref?.get(key)
    Log.e(key.name, summary ?: "NULL")
    ListPref(
        key = key.name,
        title = stringResource(
            id = R.string.video_quality
        ),
        summary = summary,
        entries = entries,
        defaultValue = "0",
        useSelectedAsSummary = true
    )
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun LanguageSetting(title: String, key: Preferences.Key<String>, pref: Preferences?) {
    val languages by remember {
        derivedStateOf {
            val locales: Array<Locale> = Locale.getAvailableLocales()
            val localCountries = HashMap<String, String>()
            for (l in locales) {
                localCountries[l.language] = l.displayLanguage.toString()
            }
            return@derivedStateOf localCountries
        }
    }
    ListPref(
        key = key.name,
        title = title,
        summary = pref?.get(key),
        entries = languages,
        defaultValue = androidx.compose.ui.text.intl.Locale.current.language,
        useSelectedAsSummary = true
    )
}

