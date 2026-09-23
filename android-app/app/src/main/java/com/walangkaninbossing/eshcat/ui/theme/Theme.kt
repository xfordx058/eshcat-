package com.walangkaninbossing.eshcat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors: ColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = PrimaryDark,
    background = AppBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = AppSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    error = DangerAccent,
    errorContainer = DangerContainerAccent,
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = PrimaryDarkNight,
    onPrimary = Color(0xFF0B1220),
    primaryContainer = PrimaryContainerNight,
    onPrimaryContainer = OnPrimaryContainerNight,
    secondary = SecondaryNight,
    onSecondary = Color(0xFF062033),
    secondaryContainer = Color(0xFF0C4A6E),
    onSecondaryContainer = InfoContainerAccent,
    tertiary = PrimaryDarkNight,
    background = AppBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = AppSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = AppSurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    error = Color(0xFFF87171),
    errorContainer = Color(0xFF3B0D14),
)

val EshcatShapes: Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(EshcatRadius.sm),
    medium = RoundedCornerShape(EshcatRadius.md),
    large = RoundedCornerShape(EshcatRadius.lg),
    extraLarge = RoundedCornerShape(EshcatRadius.xl),
)

@Composable
fun ESHCATTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        shapes = EshcatShapes,
        content = content,
    )
}