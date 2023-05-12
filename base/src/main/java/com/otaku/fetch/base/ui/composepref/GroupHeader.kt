package com.otaku.fetch.base.ui.composepref

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GroupHeader(
    title: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        Modifier
            .padding(
                start = StartPadding,
            )
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            title,
            color = color,
            fontSize = LocalTextStyle.current.fontSize.times(FontSizeMultiplier),
            fontWeight = FontWeight.SemiBold
        )
    }
}


private val StartPadding = 16.dp
private const val FontSizeMultiplier = 0.85f