package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConfiguredDay
import com.example.ui.TimetableUiState
import java.time.format.DateTimeFormatter
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import com.example.ui.components.AnimatedPencilButton

@Composable
fun SettingsScreen(
    uiState: TimetableUiState,
    onUpdateCutoffTime: (hour: Int, minute: Int) -> Unit,
    onUpdateDayConfig: (ConfiguredDay) -> Unit,
    onUpdateTheme: (String) -> Unit,
    onResetTimetable: () -> Unit,
    onUpdateLiquidGlassBar: (Boolean) -> Unit = {},
    onUpdateMonet: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dialogDispatcherOwner = remember {
        object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher = NavigationEventDispatcher()
        }
    }

    var showCustomTimeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var editingDayName by remember { mutableStateOf<ConfiguredDay?>(null) }
    var dayNameInput by remember { mutableStateOf("") }

    val cutoffFormatted = uiState.settings.cutoffTime.format(DateTimeFormatter.ofPattern("h:mm a"))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MiuixTheme.textStyles.title1,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }

        // Section 1: Cutoff Time Setting
        item {
            SmallTitle(text = "Schedule Switching")
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(16.dp),
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Cutoff Time",
                            style = MiuixTheme.textStyles.title4,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Current switch time: $cutoffFormatted",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Before this time, the app displays Today's Timetable. At or after this time, the app automatically displays Tomorrow's Timetable (or next school day).",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick cutoff presets
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(Pair(15, 0), Pair(16, 0), Pair(17, 0), Pair(18, 0)).forEach { (h, m) ->
                            val label = if (h > 12) "${h - 12}:00 PM" else "$h:00 AM"
                            val isCurrent = uiState.settings.cutoffHour == h && uiState.settings.cutoffMinute == m
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isCurrent) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { onUpdateCutoffTime(h, m) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) Color.White else MiuixTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    TextButton(
                        text = "Custom",
                        onClick = { showCustomTimeDialog = true },
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }
        }

        // Section 2: Day Configurations & Period Counts
        item {
            SmallTitle(text = "Days & Periods Configuration")
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(16.dp),
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MiuixTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Weekly Timetable Days",
                            style = MiuixTheme.textStyles.title4,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Toggle active school days, rename, and set periods per day",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                uiState.dayConfigs.forEachIndexed { index, dayConfig ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Switch(
                                checked = dayConfig.isEnabled,
                                onCheckedChange = { isEnabled ->
                                    onUpdateDayConfig(dayConfig.copy(isEnabled = isEnabled))
                                }
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = dayConfig.displayName,
                                        style = MiuixTheme.textStyles.body2,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (dayConfig.isEnabled)
                                            MiuixTheme.colorScheme.onSurface
                                        else
                                            MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    AnimatedPencilButton(
                                        onClick = {
                                            editingDayName = dayConfig
                                            dayNameInput = dayConfig.displayName
                                        },
                                        size = 28.dp,
                                        iconSize = 14.dp,
                                        testTag = "rename_day_${dayConfig.dayOfWeek.name}"
                                    )
                                }
                                Text(
                                    text = if (dayConfig.isEnabled) "${dayConfig.periodCount} periods" else "Off",
                                    style = MiuixTheme.textStyles.footnote1,
                                    color = if (dayConfig.isEnabled)
                                        MiuixTheme.colorScheme.primary
                                    else
                                        MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.6f)
                                )
                            }
                        }

                        // Period stepper for this day
                        if (dayConfig.isEnabled) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (dayConfig.periodCount > 1) {
                                            onUpdateDayConfig(dayConfig.copy(periodCount = dayConfig.periodCount - 1))
                                        }
                                    },
                                    enabled = dayConfig.periodCount > 1,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MiuixTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease periods",
                                        tint = if (dayConfig.periodCount > 1) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.disabledOnSecondaryVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Text(
                                    text = "${dayConfig.periodCount}",
                                    style = MiuixTheme.textStyles.body2,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(26.dp),
                                    textAlign = TextAlign.Center
                                )

                                IconButton(
                                    onClick = {
                                        if (dayConfig.periodCount < 15) {
                                            onUpdateDayConfig(dayConfig.copy(periodCount = dayConfig.periodCount + 1))
                                        }
                                    },
                                    enabled = dayConfig.periodCount < 15,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MiuixTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase periods",
                                        tint = if (dayConfig.periodCount < 15) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.disabledOnSecondaryVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (index < uiState.dayConfigs.lastIndex) {
                        HorizontalDivider(color = MiuixTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }
                }
            }
        }

        // Section 3: App Theme
        item {
            SmallTitle(text = "Appearance")
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(16.dp),
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Theme Style",
                            style = MiuixTheme.textStyles.title4,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose interface theme mode",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf(
                        Triple("SYSTEM", "System", Icons.Default.SettingsBrightness),
                        Triple("LIGHT", "Light", Icons.Default.LightMode),
                        Triple("DARK", "Dark", Icons.Default.DarkMode)
                    )

                    modes.forEach { (modeKey, label, icon) ->
                        val isSelected = uiState.settings.themeMode == modeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceVariant
                                )
                                .clickable { onUpdateTheme(modeKey) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) Color.White else MiuixTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    style = MiuixTheme.textStyles.footnote1,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MiuixTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MiuixTheme.colorScheme.dividerLine)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dynamic Colors (Monet)",
                            style = MiuixTheme.textStyles.title4,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Extract dynamic accent palette from system wallpaper (Android 12+)",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }

                    Switch(
                        checked = uiState.settings.useMonet,
                        onCheckedChange = { onUpdateMonet(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MiuixTheme.colorScheme.dividerLine)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Liquid Glass Navigation Bar",
                            style = MiuixTheme.textStyles.title4,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Floating translucent pill bar with shadow & squircle curves",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }

                    Switch(
                        checked = uiState.settings.useLiquidGlassBar,
                        onCheckedChange = { onUpdateLiquidGlassBar(it) }
                    )
                }
            }
        }

        // Section 4: Data Management & Reset
        item {
            SmallTitle(text = "Data Management")
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(16.dp),
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
            ) {
                Text(
                    text = "Reset Timetable",
                    style = MiuixTheme.textStyles.title4,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Reset all periods, clear saved subjects, and reconfigure timetable setup from scratch.",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    text = "Reset & Reconfigure Timetable",
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_timetable_button"),
                    colors = ButtonDefaults.textButtonColors(
                        color = MiuixTheme.colorScheme.error.copy(alpha = 0.12f),
                        textColor = MiuixTheme.colorScheme.error
                    )
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Custom Cutoff Time Dialog
    if (showCustomTimeDialog) {
        var hourInput by remember { mutableIntStateOf(uiState.settings.cutoffHour) }
        var minuteInput by remember { mutableIntStateOf(uiState.settings.cutoffMinute) }

        CompositionLocalProvider(
            LocalNavigationEventDispatcherOwner provides dialogDispatcherOwner
        ) {
            WindowDialog(
                show = true,
            title = "Set Cutoff Time",
            summary = "Select the time when timetable automatically switches to tomorrow",
            onDismissRequest = { showCustomTimeDialog = false },
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        // Hour Stepper
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { hourInput = (hourInput + 1) % 24 }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Hour")
                            }
                            Text(
                                text = String.format("%02d", hourInput),
                                style = MiuixTheme.textStyles.title1,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.primary
                            )
                            IconButton(onClick = { hourInput = if (hourInput == 0) 23 else hourInput - 1 }) {
                                Icon(Icons.Default.Remove, contentDescription = "Subtract Hour")
                            }
                        }

                        Text(
                            text = ":",
                            style = MiuixTheme.textStyles.title1,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Minute Stepper
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { minuteInput = (minuteInput + 5) % 60 }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Minute")
                            }
                            Text(
                                text = String.format("%02d", minuteInput),
                                style = MiuixTheme.textStyles.title1,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.primary
                            )
                            IconButton(onClick = { minuteInput = if (minuteInput < 5) 55 else minuteInput - 5 }) {
                                Icon(Icons.Default.Remove, contentDescription = "Subtract Minute")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            text = "Cancel",
                            onClick = { showCustomTimeDialog = false },
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            text = "Save",
                            onClick = {
                                onUpdateCutoffTime(hourInput, minuteInput)
                                showCustomTimeDialog = false
                            },
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        )
        }
    }

    // Rename Day Dialog
    if (editingDayName != null) {
        val targetDay = editingDayName!!
        CompositionLocalProvider(
            LocalNavigationEventDispatcherOwner provides dialogDispatcherOwner
        ) {
            WindowDialog(
                show = true,
                title = "Rename Day",
                onDismissRequest = { editingDayName = null },
                content = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextField(
                            value = dayNameInput,
                            onValueChange = { dayNameInput = it },
                            label = "Display Name",
                            useLabelAsPlaceholder = true,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                text = "Cancel",
                                onClick = { editingDayName = null },
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                text = "Save",
                                onClick = {
                                    if (dayNameInput.isNotBlank()) {
                                        onUpdateDayConfig(targetDay.copy(displayName = dayNameInput.trim()))
                                    }
                                    editingDayName = null
                                },
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            )
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        CompositionLocalProvider(
            LocalNavigationEventDispatcherOwner provides dialogDispatcherOwner
        ) {
            WindowDialog(
                show = true,
                title = "Reset Timetable?",
                summary = "This will permanently clear all period assignments, subject names, and schedule settings. The setup wizard will reopen.",
                onDismissRequest = { showResetDialog = false },
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            text = "Cancel",
                            onClick = { showResetDialog = false },
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            text = "Reset All",
                            onClick = {
                                showResetDialog = false
                                onResetTimetable()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                color = MiuixTheme.colorScheme.error,
                                textColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            )
        }
    }
}
