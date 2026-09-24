package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun ScheduleFlowTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val miuixMode = when (themeMode) {
        "LIGHT" -> ColorSchemeMode.Light
        "DARK" -> ColorSchemeMode.Dark
        else -> ColorSchemeMode.System
    }

    val themeController = remember(miuixMode) {
        ThemeController(colorSchemeMode = miuixMode)
    }

    val materialColors = if (darkTheme) {
        darkColorScheme(
            primary = PrimaryDark,
            onPrimary = OnPrimaryDark,
            primaryContainer = PrimaryContainerDark,
            onPrimaryContainer = OnPrimaryContainerDark,
            secondary = SecondaryDark,
            onSecondary = OnSecondaryDark,
            surface = SurfaceDark,
            onSurface = OnSurfaceDark,
            surfaceVariant = SurfaceVariantDark,
            onSurfaceVariant = OnSurfaceVariantDark,
            background = BackgroundDark,
            onBackground = OnBackgroundDark,
            outline = OutlineDark
        )
    } else {
        lightColorScheme(
            primary = PrimaryLight,
            onPrimary = OnPrimaryLight,
            primaryContainer = PrimaryContainerLight,
            onPrimaryContainer = OnPrimaryContainerLight,
            secondary = SecondaryLight,
            onSecondary = OnSecondaryLight,
            surface = SurfaceLight,
            onSurface = OnSurfaceLight,
            surfaceVariant = SurfaceVariantLight,
            onSurfaceVariant = OnSurfaceVariantLight,
            background = BackgroundLight,
            onBackground = OnBackgroundLight,
            outline = OutlineLight
        )
    }

    MiuixTheme(controller = themeController) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = Typography,
            content = content
        )
    }
}
