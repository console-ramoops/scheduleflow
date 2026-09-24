package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ConfiguredDay
import com.example.ui.TimetableViewModel
import com.example.ui.editor.PeriodEditDialog
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.example.ui.home.HomeScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.setup.SetupWizardScreen
import com.example.ui.theme.ScheduleFlowTheme
import com.example.ui.weekly.WeeklyViewScreen
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import com.example.ui.components.LiquidGlassNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val rootDispatcherOwner = remember {
                object : NavigationEventDispatcherOwner {
                    override val navigationEventDispatcher = NavigationEventDispatcher()
                }
            }
            CompositionLocalProvider(
                LocalNavigationEventDispatcherOwner provides rootDispatcherOwner
            ) {
                val viewModel: TimetableViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                ScheduleFlowTheme(
                    themeMode = uiState.settings.themeMode,
                    useMonet = uiState.settings.useMonet
                ) {
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MiuixTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MiuixTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    } else if (!uiState.isConfigured) {
                        SetupWizardScreen(
                            onSetupComplete = { normalPeriods, days ->
                                viewModel.completeSetup(normalPeriods, days)
                            }
                        )
                    } else {
                        MainAppScaffold(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    viewModel: TimetableViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val useLiquidGlass = uiState.settings.useLiquidGlassBar

    // Create a backdrop to capture main content for liquid glass blur
    val contentBackdrop = if (useLiquidGlass && isRuntimeShaderSupported()) {
        rememberLayerBackdrop()
    } else {
        null
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MiuixTheme.colorScheme.background,
        bottomBar = {
            if (!useLiquidGlass) {
                NavigationBar(
                    color = MiuixTheme.colorScheme.surface,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = if (selectedTab == 0) Icons.Filled.Schedule else Icons.Outlined.Schedule,
                        label = "Schedule",
                        modifier = Modifier.testTag("nav_item_schedule")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = if (selectedTab == 1) Icons.Filled.CalendarViewWeek else Icons.Outlined.CalendarViewWeek,
                        label = "Weekly",
                        modifier = Modifier.testTag("nav_item_weekly")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = if (selectedTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings,
                        label = "Settings",
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = if (useLiquidGlass) 0.dp else innerPadding.calculateBottomPadding()
                )
        ) {
            // Main content — wrapped in layerBackdrop so the nav bar can blur it
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(120))
                        .togetherWith(fadeOut(animationSpec = tween(90)))
                },
                label = "ScreenTransition",
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (contentBackdrop != null) {
                            Modifier.layerBackdrop(contentBackdrop)
                        } else {
                            Modifier
                        }
                    )
            ) { tab ->
                when (tab) {
                    0 -> HomeScreen(
                        uiState = uiState,
                        onFilterModeChange = { viewModel.setFilterMode(it) },
                        onSelectDay = { viewModel.selectSpecificDay(it) },
                        onEditPeriod = { viewModel.openPeriodEditor(it) },
                        onNavigateToWeekly = { selectedTab = 1 }
                    )
                    1 -> WeeklyViewScreen(
                        uiState = uiState,
                        onEditPeriod = { viewModel.openPeriodEditor(it) }
                    )
                    2 -> SettingsScreen(
                        uiState = uiState,
                        onUpdateCutoffTime = { h, m -> viewModel.updateCutoffTime(h, m) },
                        onUpdateDayConfig = { viewModel.updateDayConfig(it) },
                        onUpdateTheme = { viewModel.updateTheme(it) },
                        onResetTimetable = { viewModel.resetTimetable() },
                        onUpdateLiquidGlassBar = { viewModel.updateLiquidGlassBar(it) },
                        onUpdateMonet = { viewModel.updateMonet(it) }
                    )
                }
            }

            // Liquid Glass Floating Navigation Bar with real blur + lens refraction
            if (useLiquidGlass) {
                LiquidGlassNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    modifier = Modifier.align(Alignment.BottomCenter),
                    backdrop = contentBackdrop
                )
            }
        }

        // Period Edit Dialog (opens from Day view or Weekly view)
        if (uiState.isEditorOpen && uiState.editingPeriod != null) {
            val period = uiState.editingPeriod!!
            val dayConfig = uiState.dayConfigs.find { it.dayOfWeek == period.dayOfWeek }
            val dayName = dayConfig?.displayName ?: ConfiguredDay.defaultDisplayName(period.dayOfWeek)

            PeriodEditDialog(
                period = period,
                dayName = dayName,
                onDismiss = { viewModel.closePeriodEditor() },
                onSave = { updated -> viewModel.savePeriod(updated) },
                onClear = { viewModel.clearPeriod(period.dayOfWeek, period.periodNumber) }
            )
        }
    }
}
