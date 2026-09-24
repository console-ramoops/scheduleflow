package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun ScheduleFlowTheme(
    themeMode: String = "SYSTEM",
    useMonet: Boolean = true,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val miuixMode = if (useMonet) {
        when (themeMode) {
            "LIGHT" -> ColorSchemeMode.MonetLight
            "DARK" -> ColorSchemeMode.MonetDark
            else -> ColorSchemeMode.MonetSystem
        }
    } else {
        when (themeMode) {
            "LIGHT" -> ColorSchemeMode.Light
            "DARK" -> ColorSchemeMode.Dark
            else -> ColorSchemeMode.System
        }
    }

    val themeController = remember(miuixMode) {
        ThemeController(colorSchemeMode = miuixMode)
    }

    val context = LocalContext.current
    val materialColors = if (useMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (darkTheme) {
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

    val navigationEventDispatcherOwner = remember {
        object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher = NavigationEventDispatcher()
        }
    }

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navigationEventDispatcherOwner
    ) {
        MiuixTheme(controller = themeController) {
            MaterialTheme(
                colorScheme = materialColors,
                typography = Typography,
                content = content
            )
        }
    }
}
