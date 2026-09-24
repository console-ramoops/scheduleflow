package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun PeriodEditDialog(
    period: PeriodEntry,
    dayName: String,
    onDismiss: () -> Unit,
    onSave: (PeriodEntry) -> Unit,
    onClear: () -> Unit
) {
    var subject by remember { mutableStateOf(period.subject) }
    var teacher by remember { mutableStateOf(period.teacher) }
    var room by remember { mutableStateOf(period.room) }
    var startTime by remember { mutableStateOf(period.startTime) }
    var endTime by remember { mutableStateOf(period.endTime) }
    var colorHex by remember { mutableStateOf(period.colorHex) }
    var iconName by remember { mutableStateOf(period.iconName) }

    WindowDialog(
        show = true,
        title = "Period ${period.periodNumber} • $dayName",
        summary = if (period.isAssigned) "Edit subject and period details" else "Assign subject to period",
        onDismissRequest = onDismiss,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Subject preview header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(parseColor(colorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIconVector(iconName),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (subject.isNotBlank()) subject else "Unassigned Subject",
                            style = MiuixTheme.textStyles.title4,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Color & Icon will appear in timetable",
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
                        label = "Start Time (e.g. 08:30 AM)",
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
                        label = "End Time (e.g. 09:15 AM)",
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

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    if (period.isAssigned) {
                        TextButton(
                            text = "Clear",
                            onClick = onClear,
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
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_period_button"),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }
        }
    )
}
