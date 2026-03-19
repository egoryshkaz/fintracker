package com.fintracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ===== ЦВЕТА ДЛЯ СВЕТЛОЙ ТЕМЫ =====
private val LightColors = lightColorScheme(
    primary = Color(0xFF006874),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA1EEFF),
    onPrimaryContainer = Color(0xFF001F25),
    secondary = Color(0xFF4A5A5C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCDE4E6),
    onSecondaryContainer = Color(0xFF051F22),
    tertiary = Color(0xFF545D7E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDCE1FF),
    onTertiaryContainer = Color(0xFF0F1A37),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF9FDFE),
    onBackground = Color(0xFF001F25),
    surface = Color(0xFFF9FDFE),
    onSurface = Color(0xFF001F25),
    surfaceVariant = Color(0xFFDCE4E5),
    onSurfaceVariant = Color(0xFF40484A),
    outline = Color(0xFF70787A)
)

// ===== ЦВЕТА ДЛЯ ТЁМНОЙ ТЕМЫ =====
private val DarkColors = darkColorScheme(
    primary = Color(0xFF75E9FF),
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFFA1EEFF),
    secondary = Color(0xFFB1CBCD),
    onSecondary = Color(0xFF1C3436),
    secondaryContainer = Color(0xFF324B4D),
    onSecondaryContainer = Color(0xFFCDE4E6),
    tertiary = Color(0xFFB7C4FF),
    onTertiary = Color(0xFF262F4D),
    tertiaryContainer = Color(0xFF3C4565),
    onTertiaryContainer = Color(0xFFDCE1FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF001F25),
    onBackground = Color(0xFFA6EEFF),
    surface = Color(0xFF001F25),
    onSurface = Color(0xFFA6EEFF),
    surfaceVariant = Color(0xFF40484A),
    onSurfaceVariant = Color(0xFFC0C8CA),
    outline = Color(0xFF8A9395)
)

// ===== ТИПОГРАФИКА =====
// Переименовано, чтобы не конфликтовать с импортированным классом Typography
val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ===== ГЛАВНАЯ ФУНКЦИЯ ТЕМЫ =====
@Composable
fun FinTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,  // ← используем переименованную переменную
        content = content
    )
}