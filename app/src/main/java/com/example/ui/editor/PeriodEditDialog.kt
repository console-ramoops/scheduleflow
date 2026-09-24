package com.example.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PeriodEntry
import com.example.ui.components.ColorPickerRow
import com.example.ui.components.IconPickerRow
import com.example.ui.components.PresetSubjectChips
import com.example.ui.components.getIconVector
import com.example.ui.components.parseColor
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun PeriodEditDialog(
    period: PeriodEntry,
    dayName: String,
    onDismiss: () -> Unit,
    onSave: (PeriodEntry) -> Unit,
    onClear: () -> Unit
) {
    var subject by remember(period) { mutableStateOf(period.subject) }
    var teacher by remember(period) { mutableStateOf(period.teacher) }
    var room by remember(period) { mutableStateOf(period.room) }
    var startTime by remember(period) { mutableStateOf(period.startTime) }
    var endTime by remember(period) { mutableStateOf(period.endTime) }
    var colorHex by remember(period) { mutableStateOf(period.colorHex) }
    var iconName by remember(period) { mutableStateOf(period.iconName) }

    val isDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()

    // Smooth Spring Animation States for opening and closing
    val scaleAnim = remember { Animatable(0.72f) }
    val alphaAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(80f) }
    val scrimAnim = remember { Animatable(0f) }

    var isClosing by remember { mutableStateOf(false) }

    fun triggerDismiss(action: () -> Unit = onDismiss) {
        if (isClosing) return
        isClosing = true
        scope.launch {
            launch { scrimAnim.animateTo(0f, tween(180, easing = LinearEasing)) }
            launch { alphaAnim.animateTo(0f, tween(160, easing = LinearEasing)) }
            launch { scaleAnim.animateTo(0.85f, tween(180, easing = FastOutSlowInEasing)) }
            launch { offsetYAnim.animateTo(50f, tween(180, easing = FastOutSlowInEasing)) }
        }.invokeOnCompletion {
            action()
        }
    }

    LaunchedEffect(Unit) {
        launch {
            scrimAnim.animateTo(
                targetValue = 0.55f,
                animationSpec = tween(durationMillis = 260, easing = LinearEasing)
            )
        }
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 220, easing = LinearEasing)
            )
        }
        launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.68f,
                    stiffness = 380f
                )
            )
        }
        launch {
            offsetYAnim.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.72f,
                    stiffness = 350f
                )
            )
        }
    }

    BackHandler(enabled = true) {
        triggerDismiss(onDismiss)
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    // Fullscreen scrim + centered animated dialog
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        // Scrim backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAnim.value))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { triggerDismiss(onDismiss) }
                )
        )

        // Animated Dialog Box
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    alpha = alphaAnim.value
                    translationY = offsetYAnim.value
                }
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = Color.Black.copy(alpha = if (isDark) 0.6f else 0.25f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(MiuixTheme.colorScheme.surface)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* prevent click through to scrim */ }
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = screenHeight * 0.78f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row with Title and Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Period ${period.periodNumber} • $dayName",
                            style = MiuixTheme.textStyles.title2,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (period.isAssigned) "Edit subject and period details" else "Assign subject to period",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }

                    IconButton(
                        onClick = { triggerDismiss(onDismiss) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Subject Preview Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(parseColor(colorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIconVector(iconName),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (subject.isNotBlank()) subject else "Unassigned Subject",
                            style = MiuixTheme.textStyles.title4,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Selected accent & icon will appear in timetable",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }

                // Subject Name Field
                TextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = "Subject Name *",
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.School, contentDescription = null, tint = MiuixTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_input")
                )

                // Quick presets
                Text(
                    text = "Quick Select Subject:",
                    style = MiuixTheme.textStyles.footnote1,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                PresetSubjectChips(
                    onPresetSelected = { preset ->
                        subject = preset.name
                        colorHex = preset.colorHex
                        iconName = preset.iconName
                    }
                )

                // Start and End Times
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = "Start (e.g. 08:30 AM)",
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = MiuixTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_time_input")
                    )
                    TextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = "End (e.g. 09:15 AM)",
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = MiuixTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("end_time_input")
                    )
                }

                // Optional Teacher and Room
                TextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = "Teacher Name (Optional)",
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MiuixTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("teacher_input")
                )

                TextField(
                    value = room,
                    onValueChange = { room = it },
                    label = "Room / Classroom (Optional)",
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = MiuixTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("room_input")
                )

                // Color Picker Row
                Column {
                    Text(
                        text = "Accent Color:",
                        style = MiuixTheme.textStyles.footnote1,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    ColorPickerRow(
                        selectedColorHex = colorHex,
                        onColorSelected = { colorHex = it }
                    )
                }

                // Icon Picker Row
                Column {
                    Text(
                        text = "Subject Icon:",
                        style = MiuixTheme.textStyles.footnote1,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    IconPickerRow(
                        selectedIconName = iconName,
                        onIconSelected = { iconName = it }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        text = "Cancel",
                        onClick = { triggerDismiss(onDismiss) },
                        modifier = Modifier.weight(1f)
                    )

                    if (period.isAssigned) {
                        TextButton(
                            text = "Clear",
                            onClick = {
                                triggerDismiss { onClear() }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("clear_period_button"),
                            colors = ButtonDefaults.textButtonColors(
                                color = Color.Transparent,
                                textColor = Color(0xFFEF4444)
                            )
                        )
                    }

                    TextButton(
                        text = "Save",
                        onClick = {
                            triggerDismiss {
                                onSave(
                                    period.copy(
                                        subject = subject.trim(),
                                        teacher = teacher.trim(),
                                        room = room.trim(),
                                        startTime = startTime.trim(),
                                        endTime = endTime.trim(),
                                        colorHex = colorHex,
                                        iconName = iconName
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_period_button"),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }
        }
    }
}
