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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
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
import com.example.ui.components.parseColor
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

import androidx.compose.runtime.remember

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

    val periods = remember(uiState.allPeriods, uiState.activeDayOfWeek, uiState.activeDayConfig?.periodCount) {
        uiState.periodsForActiveDay
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Clean header — just the essentials
        item {
            Column(
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = uiState.headerTitle,
                    style = MiuixTheme.textStyles.headline1,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("timetable_status_badge")
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = formattedDate,
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.testTag("current_date_display")
                    )
                    Text(
                        text = formattedTime,
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.testTag("current_time_display")
                    )
                }
            }
        }

        // Day chips only — filter mode is implicit via Smart Auto default
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                uiState.enabledDays.forEach { dayConfig ->
                    val isDaySelected = uiState.activeDayOfWeek == dayConfig.dayOfWeek
                    val shortName = ConfiguredDay.defaultShortName(dayConfig.dayOfWeek)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isDaySelected)
                                    MiuixTheme.colorScheme.primary
                                else
                                    MiuixTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onSelectDay(dayConfig.dayOfWeek) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("day_chip_${shortName.lowercase()}")
                    ) {
                        Text(
                            text = shortName,
                            style = MiuixTheme.textStyles.body2,
                            fontWeight = if (isDaySelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isDaySelected) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Period list — clean, minimal cards
        if (periods.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No periods for this day",
                            style = MiuixTheme.textStyles.body1,
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
                PeriodRow(
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
fun PeriodRow(
    entry: PeriodEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = remember(entry.colorHex, entry.isAssigned) {
        if (entry.isAssigned) parseColor(entry.colorHex) else Color(0xFF8E8E93)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("period_card_${entry.periodNumber}"),
        insideMargin = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        onClick = onClick,
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Period number — color-coded bar
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Period number
            Text(
                text = "${entry.periodNumber}",
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.width(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Subject name — the only thing that matters
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (entry.isAssigned) entry.subject else "Tap to assign",
                    style = MiuixTheme.textStyles.body1,
                    fontWeight = if (entry.isAssigned) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (entry.isAssigned) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Show time inline if set — nothing else
                if (entry.isAssigned && entry.startTime.isNotBlank()) {
                    Text(
                        text = "${entry.startTime} – ${entry.endTime}".trim(),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
            }

            // Compact teacher tag if present
            if (entry.isAssigned && entry.teacher.isNotBlank()) {
                Text(
                    text = entry.teacher,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
