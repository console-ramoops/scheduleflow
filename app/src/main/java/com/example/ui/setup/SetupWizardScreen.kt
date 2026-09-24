package com.example.ui.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConfiguredDay
import java.time.DayOfWeek
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
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

data class DaySetupState(
    val dayOfWeek: DayOfWeek,
    var displayName: String,
    var isEnabled: Boolean,
    var periodCount: Int
)

@Composable
fun SetupWizardScreen(
    onSetupComplete: (normalPeriods: Int, days: List<ConfiguredDay>) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var normalPeriods by remember { mutableIntStateOf(7) }

    // Initialize days: Default 6 days (Mon-Sat enabled, Sun disabled), default 7 periods each
    var daysState by remember {
        mutableStateOf(
            listOf(
                DaySetupState(DayOfWeek.MONDAY, "Monday", true, 7),
                DaySetupState(DayOfWeek.TUESDAY, "Tuesday", true, 7),
                DaySetupState(DayOfWeek.WEDNESDAY, "Wednesday", true, 7),
                DaySetupState(DayOfWeek.THURSDAY, "Thursday", true, 7),
                DaySetupState(DayOfWeek.FRIDAY, "Friday", true, 7),
                DaySetupState(DayOfWeek.SATURDAY, "Saturday", true, 7),
                DaySetupState(DayOfWeek.SUNDAY, "Sunday", false, 7)
            )
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MiuixTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = "Timetable Setup",
                subtitle = "Step $currentStep of 3",
                navigationIcon = {
                    if (currentStep > 1) {
                        IconButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.testTag("setup_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MiuixTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                insideMargin = PaddingValues(12.dp),
                colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        TextButton(
                            text = "Previous",
                            onClick = { currentStep-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("wizard_prev_button")
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    TextButton(
                        text = if (currentStep < 3) "Next" else "Create Timetable",
                        onClick = {
                            if (currentStep < 3) {
                                if (currentStep == 1) {
                                    // Propagate normalPeriods to all days as base default
                                    daysState = daysState.map { it.copy(periodCount = normalPeriods) }
                                }
                                currentStep++
                            } else {
                                val configuredDays = daysState.mapIndexed { index, state ->
                                    ConfiguredDay(
                                        dayOfWeek = state.dayOfWeek,
                                        displayName = state.displayName.trim().ifBlank {
                                            ConfiguredDay.defaultDisplayName(state.dayOfWeek)
                                        },
                                        isEnabled = state.isEnabled,
                                        periodCount = state.periodCount.coerceAtLeast(1),
                                        orderIndex = index + 1
                                    )
                                }
                                onSetupComplete(normalPeriods, configuredDays)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("wizard_next_button"),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Step Progress Bar
            LinearProgressIndicator(
                progress = { currentStep / 3f },
                modifier = Modifier.fillMaxWidth(),
                color = MiuixTheme.colorScheme.primary,
                trackColor = MiuixTheme.colorScheme.surfaceVariant
            )

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "WizardStepTransition"
            ) { step ->
                when (step) {
                    1 -> Step1PeriodsCount(
                        normalPeriods = normalPeriods,
                        onPeriodsChange = { normalPeriods = it }
                    )
                    2 -> Step2DaysConfig(
                        days = daysState,
                        onDaysChange = { daysState = it }
                    )
                    3 -> Step3PeriodsPerDay(
                        days = daysState,
                        onDaysChange = { daysState = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun Step1PeriodsCount(
    normalPeriods: Int,
    onPeriodsChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Normal School Day",
            style = MiuixTheme.textStyles.title1,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "How many periods are there in a normal school day? You will customize individual days next.",
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Hero Period Stepper Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            insideMargin = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
            colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = { if (normalPeriods > 1) onPeriodsChange(normalPeriods - 1) },
                    enabled = normalPeriods > 1,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MiuixTheme.colorScheme.surfaceVariant)
                        .testTag("period_decrement_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease periods",
                        tint = if (normalPeriods > 1) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.disabledOnSecondaryVariant
                    )
                }

                Spacer(modifier = Modifier.width(36.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$normalPeriods",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.testTag("normal_period_count_text")
                    )
                    Text(
                        text = if (normalPeriods == 1) "Period" else "Periods",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }

                Spacer(modifier = Modifier.width(36.dp))

                IconButton(
                    onClick = { if (normalPeriods < 15) onPeriodsChange(normalPeriods + 1) },
                    enabled = normalPeriods < 15,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MiuixTheme.colorScheme.surfaceVariant)
                        .testTag("period_increment_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase periods",
                        tint = if (normalPeriods < 15) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.disabledOnSecondaryVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Preset Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            listOf(5, 6, 7, 8, 9).forEach { count ->
                val isSelected = count == normalPeriods
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onPeriodsChange(count) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "$count",
                        style = MiuixTheme.textStyles.body2,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MiuixTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun Step2DaysConfig(
    days: List<DaySetupState>,
    onDaysChange: (List<DaySetupState>) -> Unit
) {
    var editingDayIndex by remember { mutableStateOf<Int?>(null) }
    var editNameInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                Text(
                    text = "Weekly Schedule Days",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Text(
                    text = "Default: 6 days (Mon–Sat). Customize active days and names below.",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Presets Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        text = "Mon–Fri (5)",
                        onClick = {
                            onDaysChange(
                                days.map {
                                    it.copy(isEnabled = it.dayOfWeek != DayOfWeek.SATURDAY && it.dayOfWeek != DayOfWeek.SUNDAY)
                                }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        text = "Mon–Sat (6)",
                        onClick = {
                            onDaysChange(
                                days.map {
                                    it.copy(isEnabled = it.dayOfWeek != DayOfWeek.SUNDAY)
                                }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                    TextButton(
                        text = "All 7 Days",
                        onClick = {
                            onDaysChange(days.map { it.copy(isEnabled = true) })
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        items(days.size) { index ->
            val day = days[index]
            val isEditingThis = editingDayIndex == index

            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(14.dp),
                colors = CardDefaults.defaultColors(
                    color = if (day.isEnabled) MiuixTheme.colorScheme.surface else MiuixTheme.colorScheme.surface.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (day.isEnabled)
                                        MiuixTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else
                                        MiuixTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.dayOfWeek.name.take(2),
                                style = MiuixTheme.textStyles.body2,
                                fontWeight = FontWeight.Bold,
                                color = if (day.isEnabled) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = day.displayName,
                                    style = MiuixTheme.textStyles.title4,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (day.isEnabled) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                                IconButton(
                                    onClick = {
                                        if (isEditingThis) {
                                            editingDayIndex = null
                                        } else {
                                            editingDayIndex = index
                                            editNameInput = day.displayName
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Rename day",
                                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (day.isEnabled) "Active school day" else "Off / No classes",
                                style = MiuixTheme.textStyles.footnote,
                                color = if (day.isEnabled) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    }

                    Switch(
                        checked = day.isEnabled,
                        onCheckedChange = { checked ->
                            val updated = days.toMutableList()
                            updated[index] = day.copy(isEnabled = checked)
                            onDaysChange(updated)
                        }
                    )
                }

                // Inline rename field
                if (isEditingThis) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = editNameInput,
                            onValueChange = { editNameInput = it },
                            label = "Day Display Name",
                            useLabelAsPlaceholder = true,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            text = "Done",
                            onClick = {
                                if (editNameInput.isNotBlank()) {
                                    val updated = days.toMutableList()
                                    updated[index] = day.copy(displayName = editNameInput.trim())
                                    onDaysChange(updated)
                                }
                                editingDayIndex = null
                            },
                            colors = ButtonDefaults.textButtonColorsPrimary()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step3PeriodsPerDay(
    days: List<DaySetupState>,
    onDaysChange: (List<DaySetupState>) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                Text(
                    text = "Periods for Each Day",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface
                )
                Text(
                    text = "Customize periods individually for half-days or specific schedules (e.g. Wednesday 6, Saturday 5).",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        items(days.size) { index ->
            val day = days[index]
            if (day.isEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = day.displayName,
                                style = MiuixTheme.textStyles.title4,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${day.periodCount} periods scheduled",
                                style = MiuixTheme.textStyles.footnote,
                                color = MiuixTheme.colorScheme.primary
                            )
                        }

                        // Stepper
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (day.periodCount > 1) {
                                        val updated = days.toMutableList()
                                        updated[index] = day.copy(periodCount = day.periodCount - 1)
                                        onDaysChange(updated)
                                    }
                                },
                                enabled = day.periodCount > 1,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MiuixTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = if (day.periodCount > 1) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.disabledOnSecondaryVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "${day.periodCount}",
                                style = MiuixTheme.textStyles.title3,
                                fontWeight = FontWeight.ExtraBold,
                                color = MiuixTheme.colorScheme.primary,
                                modifier = Modifier
                                    .width(28.dp)
                                    .padding(horizontal = 2.dp),
                                textAlign = TextAlign.Center
                            )

                            IconButton(
                                onClick = {
                                    if (day.periodCount < 15) {
                                        val updated = days.toMutableList()
                                        updated[index] = day.copy(periodCount = day.periodCount + 1)
                                        onDaysChange(updated)
                                    }
                                },
                                enabled = day.periodCount < 15,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MiuixTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = if (day.periodCount < 15) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.disabledOnSecondaryVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
