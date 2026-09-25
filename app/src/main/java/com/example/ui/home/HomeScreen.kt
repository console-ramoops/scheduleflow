package com.example.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Clean Minimal Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = uiState.headerTitle,
                    style = MiuixTheme.textStyles.headline1,
                    fontWeight = FontWeight.ExtraBold,
                    color = MiuixTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("timetable_status_badge")
                )
                Text(
                    text = "$formattedDate  •  $formattedTime",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.testTag("current_date_display")
                )
            }
        }

        // Sleek Day Selector Bar (Today, Tomorrow, Day Chips)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Today Quick Switch
                val isTodayActive = uiState.filterMode == ViewFilterMode.TODAY
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isTodayActive) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onFilterModeChange(ViewFilterMode.TODAY) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("filter_today")
                ) {
                    Text(
                        text = "Today",
                        style = MiuixTheme.textStyles.body2,
                        fontWeight = if (isTodayActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isTodayActive) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface
                    )
                }

                // Tomorrow Quick Switch
                val isTomorrowActive = uiState.filterMode == ViewFilterMode.TOMORROW
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isTomorrowActive) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onFilterModeChange(ViewFilterMode.TOMORROW) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("filter_tomorrow")
                ) {
                    Text(
                        text = "Tomorrow",
                        style = MiuixTheme.textStyles.body2,
                        fontWeight = if (isTomorrowActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isTomorrowActive) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface
                    )
                }

                // Thin vertical separator
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(MiuixTheme.colorScheme.outline.copy(alpha = 0.2f))
                )

                // Day Chips (Mon, Tue, Wed, ...)
                uiState.enabledDays.forEach { dayConfig ->
                    val isDaySelected = uiState.activeDayOfWeek == dayConfig.dayOfWeek &&
                        uiState.filterMode != ViewFilterMode.TODAY &&
                        uiState.filterMode != ViewFilterMode.TOMORROW
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
                            .padding(horizontal = 14.dp, vertical = 8.dp)
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

        // Period list — minimal, modern, uncluttered cards
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
                            text = "No periods scheduled for this day",
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

/**
 * Clean, uncluttered PeriodCard used in both HomeScreen and WeeklyViewScreen.
 */
@Composable
fun PeriodCard(
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
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        onClick = onClick,
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Period Number Colored Indicator Strip
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

            // Subject name & optional details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (entry.isAssigned) entry.subject else "Tap to assign period",
                    style = MiuixTheme.textStyles.body1,
                    fontWeight = if (entry.isAssigned) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (entry.isAssigned) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Subtitle details: Time, Room, Teacher cleanly formatted in one line
                val details = buildList {
                    if (entry.startTime.isNotBlank() || entry.endTime.isNotBlank()) {
                        add("${entry.startTime} – ${entry.endTime}".trim())
                    }
                    if (entry.room.isNotBlank()) add(entry.room)
                    if (entry.teacher.isNotBlank()) add(entry.teacher)
                }.joinToString("  •  ")

                if (entry.isAssigned && details.isNotBlank()) {
                    Text(
                        text = details,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
