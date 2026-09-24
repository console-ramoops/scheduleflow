package com.example.ui.weekly

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConfiguredDay
import com.example.model.PeriodEntry
import com.example.ui.TimetableUiState
import com.example.ui.components.getIconVector
import com.example.ui.components.parseColor
import com.example.ui.home.PeriodCard
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

enum class WeeklyLayoutMode {
    GRID_TABLE,
    DAY_CARDS
}

@Composable
fun WeeklyViewScreen(
    uiState: TimetableUiState,
    onEditPeriod: (PeriodEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val enabledDays = uiState.enabledDays
    var layoutMode by remember { mutableStateOf(WeeklyLayoutMode.GRID_TABLE) }
    var selectedDayTab by remember { mutableIntStateOf(0) }

    if (enabledDays.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No days configured in timetable",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
        }
        return
    }

    val maxPeriodsAcrossDays = enabledDays.maxOfOrNull { it.periodCount } ?: 7

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // Layout switcher row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallTitle(text = "Weekly Timetable")

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val isGrid = layoutMode == WeeklyLayoutMode.GRID_TABLE
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isGrid) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceVariant)
                        .clickable { layoutMode = WeeklyLayoutMode.GRID_TABLE }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("weekly_mode_grid")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarViewMonth,
                            contentDescription = null,
                            tint = if (isGrid) Color.White else MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Grid",
                            style = MiuixTheme.textStyles.footnote,
                            fontWeight = if (isGrid) FontWeight.Bold else FontWeight.Normal,
                            color = if (isGrid) Color.White else MiuixTheme.colorScheme.onSurface
                        )
                    }
                }

                val isCards = layoutMode == WeeklyLayoutMode.DAY_CARDS
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCards) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceVariant)
                        .clickable { layoutMode = WeeklyLayoutMode.DAY_CARDS }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("weekly_mode_cards")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ViewAgenda,
                            contentDescription = null,
                            tint = if (isCards) Color.White else MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Day List",
                            style = MiuixTheme.textStyles.footnote,
                            fontWeight = if (isCards) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCards) Color.White else MiuixTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        when (layoutMode) {
            WeeklyLayoutMode.GRID_TABLE -> {
                WeeklyGridTable(
                    enabledDays = enabledDays,
                    maxPeriods = maxPeriodsAcrossDays,
                    allPeriods = uiState.allPeriods,
                    onEditPeriod = onEditPeriod
                )
            }
            WeeklyLayoutMode.DAY_CARDS -> {
                val safeIndex = selectedDayTab.coerceIn(0, enabledDays.lastIndex)
                val activeDay = enabledDays[safeIndex]
                val tabTexts = remember(enabledDays) {
                    enabledDays.map { ConfiguredDay.defaultShortName(it.dayOfWeek) }
                }

                TabRow(
                    tabs = tabTexts,
                    selectedTabIndex = safeIndex,
                    onTabSelected = { selectedDayTab = it },
                    listState = rememberLazyListState(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )

                val dayPeriods = (1..activeDay.periodCount).map { pNum ->
                    uiState.allPeriods.find { it.dayOfWeek == activeDay.dayOfWeek && it.periodNumber == pNum }
                        ?: PeriodEntry(dayOfWeek = activeDay.dayOfWeek, periodNumber = pNum)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activeDay.displayName} Schedule",
                                style = MiuixTheme.textStyles.title4,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${activeDay.periodCount} Periods",
                                style = MiuixTheme.textStyles.footnote,
                                color = MiuixTheme.colorScheme.primary
                            )
                        }
                    }

                    items(dayPeriods) { entry ->
                        PeriodCard(
                            entry = entry,
                            onClick = { onEditPeriod(entry) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyGridTable(
    enabledDays: List<ConfiguredDay>,
    maxPeriods: Int,
    allPeriods: List<PeriodEntry>,
    onEditPeriod: (PeriodEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    val columnWidth = 140.dp
    val headerHeight = 52.dp
    val rowHeight = 92.dp
    val periodLabelWidth = 52.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = "Scroll horizontally for all days • Tap any period cell to edit",
            style = MiuixTheme.textStyles.footnote,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Table Container Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            insideMargin = PaddingValues(0.dp),
            colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Fixed Left Column: Period Numbers Header & Labels
                Column(
                    modifier = Modifier
                        .width(periodLabelWidth)
                        .background(MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    // Top-left corner cell
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(headerHeight)
                            .border(0.5.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pd",
                            style = MiuixTheme.textStyles.footnote,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }

                    // Vertically scrollable period labels (P1..Pn)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(verticalScrollState)
                    ) {
                        for (p in 1..maxPeriods) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(rowHeight)
                                    .border(0.5.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "P$p",
                                    style = MiuixTheme.textStyles.title4,
                                    fontWeight = FontWeight.Bold,
                                    color = MiuixTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Scrollable Grid Area (Columns = Days)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    enabledDays.forEach { dayConfig ->
                        Column(modifier = Modifier.width(columnWidth)) {
                            // Day Column Header
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(headerHeight)
                                    .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.10f))
                                    .border(0.5.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = dayConfig.displayName,
                                        style = MiuixTheme.textStyles.footnote,
                                        fontWeight = FontWeight.Bold,
                                        color = MiuixTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${dayConfig.periodCount} periods",
                                        fontSize = 10.sp,
                                        color = MiuixTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Day Period Cells
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(verticalScrollState)
                            ) {
                                for (p in 1..maxPeriods) {
                                    if (p <= dayConfig.periodCount) {
                                        val entry = allPeriods.find {
                                            it.dayOfWeek == dayConfig.dayOfWeek && it.periodNumber == p
                                        } ?: PeriodEntry(
                                            dayOfWeek = dayConfig.dayOfWeek,
                                            periodNumber = p
                                        )

                                        WeeklyGridCell(
                                            entry = entry,
                                            onClick = { onEditPeriod(entry) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(rowHeight)
                                                .border(0.5.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.2f))
                                        )
                                    } else {
                                        // Clean disabled cell for days with fewer periods
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(rowHeight)
                                                .background(MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                                .border(0.5.dp, MiuixTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "—",
                                                style = MiuixTheme.textStyles.body2,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.35f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
fun WeeklyGridCell(
    entry: PeriodEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFilled = entry.isAssigned
    val accentColor = if (isFilled) parseColor(entry.colorHex) else Color.Transparent

    Box(
        modifier = modifier
            .background(if (isFilled) accentColor.copy(alpha = 0.12f) else MiuixTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isFilled) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIconVector(entry.iconName),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }

                    if (entry.room.isNotBlank()) {
                        Text(
                            text = entry.room,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.secondary,
                            maxLines = 1
                        )
                    }
                }

                Text(
                    text = entry.subject,
                    style = MiuixTheme.textStyles.body2,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (entry.startTime.isNotBlank()) {
                    Text(
                        text = entry.startTime,
                        fontSize = 9.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Assign subject",
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.45f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Assign",
                    fontSize = 10.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.5f)
                )
            }
        }
    }
}
