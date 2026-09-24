package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AnimatedPencilButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    iconSize: Dp = 18.dp,
    testTag: String = "animated_pencil_button"
) {
    val coroutineScope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.78f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.50f,
            stiffness = 450f
        ),
        label = "pencil_scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isPressed) -25f else 0f,
        animationSpec = spring(
            dampingRatio = 0.45f,
            stiffness = 400f
        ),
        label = "pencil_rotation"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed)
            MiuixTheme.colorScheme.primary.copy(alpha = 0.22f)
        else
            MiuixTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
        animationSpec = tween(durationMillis = 150),
        label = "pencil_bg"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isPressed)
            MiuixTheme.colorScheme.primary
        else
            MiuixTheme.colorScheme.onSurfaceVariantSummary,
        animationSpec = tween(durationMillis = 150),
        label = "pencil_tint"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                coroutineScope.launch {
                    isPressed = true
                    delay(130)
                    isPressed = false
                    onClick()
                }
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}
