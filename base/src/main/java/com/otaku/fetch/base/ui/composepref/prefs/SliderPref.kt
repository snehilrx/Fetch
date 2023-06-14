package com.otaku.fetch.base.ui.composepref.prefs

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.otaku.fetch.base.ui.composepref.LocalPrefsDataStore
import com.otaku.fetch.base.ui.composepref.roundToDP
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Preference which shows a slider just below the [title]
 *
 * @param key Key used to identify this Pref in the DataStore
 * @param title Main text which describes the Pref
 * @param modifier Modifier applied to the Text aspect of this Pref
 * @param defaultValue Default value of the slider. Used if this key is not found in the DataStore
 * @param onValueChangeFinished Same as onValueChangeFinished parameter in [Slider]
 * @param valueRange Same as valueRange parameter in [Slider]
 * @param showValue If the current slider value should be shown on the side of the slider.
 * @param enabled If false, this slider cannot be interacted with.
 * @param steps Same as steps parameter in [Slider]
 * @param textColor Text colour of the [title] and shown value if [showValue] is true.
 * @param enabled If [enabled] is not true, this Pref cannot be checked/unchecked
 * @param leadingIcon Icon which is positioned at the start of the Pref
 */
@Composable
@SuppressLint("UNUSED")
fun SliderPref(
    key: String,
    title: String,
    modifier: Modifier = Modifier,
    defaultValue: Float = 0f,
    onValueChangeFinished: ((Float) -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    showValue: Boolean = false,
    steps: Int = 0,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
) {

    val selectionKey = floatPreferencesKey(key)
    val scope = rememberCoroutineScope()

    val datastore = LocalPrefsDataStore.current
    val prefs by remember { datastore.data }.collectAsState(initial = null)

    var value by remember { mutableFloatStateOf(defaultValue) }

    LaunchedEffect(Unit) {
        prefs?.get(selectionKey)?.also { value = it }
    }

    LaunchedEffect(datastore.data) {
        datastore.data.collectLatest { pref ->
            pref[selectionKey]?.also {
                value = it
            }
        }
    }

    fun edit() = run {
        scope.launch {
            try {
                datastore.edit { preferences ->
                    preferences[selectionKey] = value
                }
                onValueChangeFinished?.invoke(value)
            } catch (e: Exception) {
                Log.e("SliderPref", "Could not write pref $key to database. ${e.printStackTrace()}")
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
    ) {

        TextPref(
            title = title,
            modifier = modifier,
            textColor = textColor,
            minimalHeight = true,
            leadingIcon = leadingIcon,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        )
        {
            Slider(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier
                    .weight(2.1f)
                    .padding(start = 16.dp, end = 16.dp),
                valueRange = valueRange,
                steps = steps,
                onValueChangeFinished = { edit() },
                enabled = enabled
            )

            if (showValue) {
                Text(
                    text = roundToDP(value, 2).toString(),
                    color = textColor,
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(start = 8.dp)
                )
            }
        }

    }


}