package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Today
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConfiguredDay
import com.example.model.PeriodEntry
import com.example.ui.TimetableUiState
import com.example.ui.ViewFilterMode
import com.example.ui.components.getIconVector
import com.example.ui.components.parseColor
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

import androidx.compose.runtime.remember
import com.example.ui.components.AnimatedPencilButton

@Composable
fun HomeScreen(
    uiState: TimetableUiState,
    onFilterModeChange: (ViewFilterMode) -> Unit,
    onSelectDay: (DayOfWeek) -> Unit,
    onEditPeriod: (PeriodEntry) -> Unit,
    onNavigateToWeekly: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }
    val formattedDate = remember(uiState.activeDate) { uiState.activeDate.format(dateFormatter) }
    val formattedTime = remember(uiState.currentDateTime.hour, uiState.currentDateTime.minute) {
        uiState.currentDateTime.format(timeFormatter)
    }
    val cutoffFormatted = remember(uiState.settings.cutoffHour, uiState.settings.cutoffMinute) {
        uiState.settings.cutoffTime.format(timeFormatter)
    }

    val periods = remember(uiState.allPeriods, uiState.activeDayOfWeek, uiState.activeDayConfig?.periodCount) {
        uiState.periodsForActiveDay
    }
    val assignedCount = remember(periods) { periods.count { it.isAssigned } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Decluttered Minimal Hero Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .testTag("home_hero_card"),
                insideMargin = PaddingValues(18.dp),
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Top: Timetable Title + Live Clock Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (uiState.filterMode == ViewFilterMode.SMART_AUTO)
                                    Icons.Default.AutoAwesome
                                else
                                    Icons.Default.Today,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.headerTitle,
                                style = MiuixTheme.textStyles.title3,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.primary,
                                modifier = Modifier.testTag("timetable_status_badge")
                            )
                        }

                        // Compact Clock Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = formattedTime,
                                style = MiuixTheme.textStyles.footnote1,
                                fontWeight = FontWeight.SemiBold,
                                color = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("current_time_display")
                            )
                        }
                    }

                    // Middle: Prominent Actual Date
                    Text(
                        text = formattedDate,
                        style = MiuixTheme.textStyles.title1,
                        fontWeight = FontWeight.ExtraBold,
                        color = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("current_date_display")
                    )

                    // Bottom: Compact Progress & Switch Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$assignedCount / ${periods.size} periods assigned",
                            style = MiuixTheme.textStyles.footnote1,
                            fontWeight = FontWeight.Medium,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )

                        Text(
                            text = "Auto-switch at $cutoffFormatted",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }
            }
        }

        // Unified Single Row: Filter Modes & Day Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val smartSelected = uiState.filterMode == ViewFilterMode.SMART_AUTO
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (smartSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onFilterModeChange(ViewFilterMode.SMART_AUTO) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("filter_smart_auto")
                ) {
                    Text(
                        text = "Auto",
                        style = MiuixTheme.textStyles.body2,
                        fontWeight = if (smartSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (smartSelected) Color.White else MiuixTheme.colorScheme.onSurface
                    )
                }

                val todaySelected = uiState.filterMode == ViewFilterMode.TODAY
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (todaySelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onFilterModeChange(ViewFilterMode.TODAY) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("filter_today")
                ) {
                    Text(
                        text = "Today",
                        style = MiuixTheme.textStyles.body2,
                        fontWeight = if (todaySelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (todaySelected) Color.White else MiuixTheme.colorScheme.onSurface
                    )
                }

                val tomorrowSelected = uiState.filterMode == ViewFilterMode.TOMORROW
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (tomorrowSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onFilterModeChange(ViewFilterMode.TOMORROW) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("filter_tomorrow")
                ) {
                    Text(
                        text = "Tomorrow",
                        style = MiuixTheme.textStyles.body2,
                        fontWeight = if (tomorrowSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (tomorrowSelected) Color.White else MiuixTheme.colorScheme.onSurface
                    )
                }

                // Vertical separator
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(MiuixTheme.colorScheme.outline.copy(alpha = 0.2f))
                )

                // Day Selector Chips (Mon, Tue, Wed, ...)
                uiState.enabledDays.forEach { dayConfig ->
                    val isDaySelected = uiState.activeDayOfWeek == dayConfig.dayOfWeek
                    val shortName = ConfiguredDay.defaultShortName(dayConfig.dayOfWeek)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isDaySelected)
                                    MiuixTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else
                                    MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .then(
                                if (isDaySelected)
                                    Modifier.border(1.5.dp, MiuixTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                else
                                    Modifier
                            )
                            .clickable { onSelectDay(dayConfig.dayOfWeek) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("day_chip_${shortName.lowercase()}")
                    ) {
                        Text(
                            text = shortName,
                            style = MiuixTheme.textStyles.body2,
                            fontWeight = if (isDaySelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isDaySelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Section Title: Schedule
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SmallTitle(text = "Schedule (${periods.size} Periods)")

                if (periods.none { it.isAssigned }) {
                    Text(
                        text = "No subjects assigned yet",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.error
                    )
                }
            }
        }

        // Period Entries List
        if (periods.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    insideMargin = PaddingValues(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No periods scheduled for this day",
                            style = MiuixTheme.textStyles.title3,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "You can add periods to this day in Settings",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }
            }
        } else {
            items(
                items = periods,
                key = { it.periodNumber }
            ) { entry ->
                PeriodCard(
                    entry = entry,
                    onClick = { onEditPeriod(entry) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun PeriodCard(
    entry: PeriodEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = remember(entry.colorHex, entry.isAssigned) {
        if (entry.isAssigned) parseColor(entry.colorHex) else Color(0xFF8E8E93)
    }
    val isFilled = entry.isAssigned

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("period_card_${entry.periodNumber}"),
        insideMargin = PaddingValues(14.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        onClick = onClick,
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Period Number Squircle Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isFilled) accentColor.copy(alpha = 0.15f) else MiuixTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${entry.periodNumber}",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isFilled) accentColor else MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isFilled) entry.subject else "Unassigned Period ${entry.periodNumber}",
                        style = MiuixTheme.textStyles.title4,
                        fontWeight = if (isFilled) FontWeight.Bold else FontWeight.Normal,
                        color = if (isFilled) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isFilled) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconVector(entry.iconName),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time Badge, Teacher & Room details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (entry.startTime.isNotBlank() || entry.endTime.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${entry.startTime} - ${entry.endTime}".trim().removePrefix("-").removeSuffix("-").trim(),
                                style = MiuixTheme.textStyles.footnote1,
                                fontWeight = FontWeight.Medium,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    }

                    if (entry.room.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.secondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = entry.room,
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.secondary
                            )
                        }
                    }

                    if (entry.teacher.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = entry.teacher,
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            AnimatedPencilButton(
                onClick = onClick,
                size = 36.dp,
                iconSize = 18.dp,
                testTag = "pencil_button_${entry.periodNumber}"
            )
        }
    }
}
