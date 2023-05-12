package com.otaku.kickassanime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

val md_theme_light_primary = Color(0xFF7A5900)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFFFDEA2)
val md_theme_light_onPrimaryContainer = Color(0xFF261900)
val md_theme_light_secondary = Color(0xFF785A00)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFFFDF9D)
val md_theme_light_onSecondaryContainer = Color(0xFF251A00)
val md_theme_light_tertiary = Color(0xFF00696E)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFF6FF6FF)
val md_theme_light_onTertiaryContainer = Color(0xFF002022)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFFFBFF)
val md_theme_light_onBackground = Color(0xFF221B00)
val md_theme_light_surface = Color(0xFFFFFBFF)
val md_theme_light_onSurface = Color(0xFF221B00)
val md_theme_light_surfaceVariant = Color(0xFFEDE1CF)
val md_theme_light_onSurfaceVariant = Color(0xFF4D4639)
val md_theme_light_outline = Color(0xFF7F7667)
val md_theme_light_inverseOnSurface = Color(0xFFFFF0C0)
val md_theme_light_inverseSurface = Color(0xFF3A3000)
val md_theme_light_inversePrimary = Color(0xFFF4BE48)
val md_theme_light_shadow = Color(0xFF000000)
val md_theme_light_surfaceTint = Color(0xFF7A5900)
val md_theme_light_outlineVariant = Color(0xFFD1C5B4)
val md_theme_light_scrim = Color(0xFF000000)

val md_theme_dark_primary = Color(0xFFF4BE48)
val md_theme_dark_onPrimary = Color(0xFF402D00)
val md_theme_dark_primaryContainer = Color(0xFF5C4200)
val md_theme_dark_onPrimaryContainer = Color(0xFFFFDEA2)
val md_theme_dark_secondary = Color(0xFFF1BF48)
val md_theme_dark_onSecondary = Color(0xFF3F2E00)
val md_theme_dark_secondaryContainer = Color(0xFF5B4300)
val md_theme_dark_onSecondaryContainer = Color(0xFFFFDF9D)
val md_theme_dark_tertiary = Color(0xFF4CD9E2)
val md_theme_dark_onTertiary = Color(0xFF00373A)
val md_theme_dark_tertiaryContainer = Color(0xFF004F53)
val md_theme_dark_onTertiaryContainer = Color(0xFF6FF6FF)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF000000)
val md_theme_dark_onBackground = Color(0xFFFFE264)
val md_theme_dark_surface = Color(0xFF000000)
val md_theme_dark_onSurface = Color(0xFFFFE264)
val md_theme_dark_surfaceVariant = Color(0xFF4D4639)
val md_theme_dark_onSurfaceVariant = Color(0xFFD1C5B4)
val md_theme_dark_outline = Color(0xFF9A8F80)
val md_theme_dark_inverseOnSurface = Color(0xFF221B00)
val md_theme_dark_inverseSurface = Color(0xFFFFE264)
val md_theme_dark_inversePrimary = Color(0xFF7A5900)
val md_theme_dark_shadow = Color(0xFF000000)
val md_theme_dark_surfaceTint = Color(0xFFF4BE48)
val md_theme_dark_outlineVariant = Color(0xFF4D4639)
val md_theme_dark_scrim = Color(0xFF000000)

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)


private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

val defaultTypography = Typography()


val sohenFont = FontFamily(
    Font(
        weight = FontWeight.W100,
        style = FontStyle.Normal,
        resId = com.otaku.fetch.base.R.font.sohne_extraleicht
    ),
    Font(
        weight = FontWeight.W100,
        style = FontStyle.Italic,
        resId = com.otaku.fetch.base.R.font.sohne_extraleicht_kursiv
    ),
    Font(
        weight = FontWeight.W200,
        style = FontStyle.Normal,
        resId = com.otaku.fetch.base.R.font.sohne_leicht
    ),
    Font(
        weight = FontWeight.W200,
        style = FontStyle.Italic,
        resId = com.otaku.fetch.base.R.font.sohne_leicht_kursiv
    ),
    Font(
        weight = FontWeight.W300,
        style = FontStyle.Normal,
        resId = com.otaku.fetch.base.R.font.sohne_buch
    ),
    Font(
        weight = FontWeight.W300,
        style = FontStyle.Italic,
        resId = com.otaku.fetch.base.R.font.sohne_buchkursiv
    ),
    Font(
        weight = FontWeight.W400,
        style = FontStyle.Normal,
        resId = com.otaku.fetch.base.R.font.sohne_kraftig
    ),
    Font(
        weight = FontWeight.W400,
        style = FontStyle.Italic,
        resId = com.otaku.fetch.base.R.font.sohne_kraftig_kursiv
    ),
    Font(
        weight = FontWeight.W600,
        style = FontStyle.Normal,
        resId = com.otaku.fetch.base.R.font.sohne_halbfett
    ),
    Font(
        weight = FontWeight.W600,
        style = FontStyle.Italic,
        resId = com.otaku.fetch.base.R.font.sohne_halbfett_kursiv
    ),
    Font(
        weight = FontWeight.W700,
        style = FontStyle.Normal,
        resId = com.otaku.fetch.base.R.font.sohne_dreiviertelfett
    ),
    Font(
        weight = FontWeight.W700,
        style = FontStyle.Italic,
        resId = com.otaku.fetch.base.R.font.sohne_dreiviertelfett_kursiv
    ),
    Font(
        weight = FontWeight.W800,
        style = FontStyle.Normal,
        resId = com.otaku.fetch.base.R.font.sohne_fett
    ),
    Font(
        weight = FontWeight.W800,
        style = FontStyle.Italic,
        resId = com.otaku.fetch.base.R.font.sohne_fett_kursiv
    ),
    Font(
        weight = FontWeight.W900,
        style = FontStyle.Normal,
        resId = com.otaku.fetch.base.R.font.sohne_extrafett
    ),
    Font(
        weight = FontWeight.W900,
        style = FontStyle.Italic,
        resId = com.otaku.fetch.base.R.font.sohne_extrafett_kursiv
    ),
)

val sohenTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = sohenFont),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = sohenFont),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = sohenFont),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = sohenFont),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = sohenFont),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = sohenFont),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = sohenFont),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = sohenFont),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = sohenFont),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = sohenFont),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = sohenFont),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = sohenFont),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = sohenFont),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = sohenFont),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = sohenFont),
)


@Composable
fun KickassAnimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colorSchemeColors = when {
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorSchemeColors,
        content = content,
        typography = sohenTypography
    )
}
