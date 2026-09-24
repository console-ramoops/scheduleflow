// Adapted from compose-miuix-ui contributors & Kyant0/AndroidLiquidGlass (Apache 2.0).

package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.liquid.lens
import com.example.ui.components.liquid.vibrancy
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.highlight.BloomStroke
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.highlight.LightPosition
import top.yukonga.miuix.kmp.blur.highlight.LightSource
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt

data class NavTabItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
)

// High-fidelity liquid glass specular reflection (Apple dual-peak lighting model)
private val iosGlassSpecular: Highlight = Highlight(
    width = 1.2.dp,
    alpha = 0.8f,
    style = BloomStroke(
        color = Color.White.copy(alpha = 0.22f),
        innerBlurRadius = 2.0.dp,
        primaryLight = LightSource(
            position = LightPosition(0.35f, -0.6f, -0.1f),
            color = Color.White,
            intensity = 1.0f,
        ),
        secondaryLight = LightSource(
            position = LightPosition(0.65f, 0.7f, -0.4f),
            color = Color.White,
            intensity = 0.35f,
        ),
        dualPeak = true,
    ),
)

private val pillIndicatorSpecular: Highlight = Highlight(
    width = 1.0.dp,
    alpha = 0.6f,
    style = BloomStroke(
        color = Color.White.copy(alpha = 0.25f),
        innerBlurRadius = 1.5.dp,
        primaryLight = LightSource(
            position = LightPosition(0.5f, -0.5f, -0.1f),
            color = Color.White,
            intensity = 0.8f,
        ),
        dualPeak = false,
    ),
)

@Composable
fun LiquidGlassNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: LayerBackdrop? = null,
) {
    val items = remember {
        listOf(
            NavTabItem(
                selectedIcon = Icons.Filled.Schedule,
                unselectedIcon = Icons.Outlined.Schedule,
                label = "Schedule",
            ),
            NavTabItem(
                selectedIcon = Icons.Filled.CalendarViewWeek,
                unselectedIcon = Icons.Outlined.CalendarViewWeek,
                label = "Weekly",
            ),
            NavTabItem(
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                label = "Settings",
            )
        )
    }

    val isDark = isSystemInDarkTheme()
    val isBlurActive = isRuntimeShaderSupported()
    val pillShape = remember { CircleShape }
    val accentColor = MiuixTheme.colorScheme.primary
    val tabContentColor = MiuixTheme.colorScheme.onSurface
    val surfaceContainer = MiuixTheme.colorScheme.surfaceContainer
    val containerColor = if (isBlurActive) surfaceContainer.copy(alpha = 0.45f) else surfaceContainer

    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val animationScope = rememberCoroutineScope()
    val tabsCount = items.size

    var tabWidthPx by remember { mutableFloatStateOf(0f) }
    var totalWidthPx by remember { mutableFloatStateOf(0f) }

    // Silky smooth spring indicator position (deferred read in graphicsLayer = 0 recompositions)
    val indicatorOffset = remember { Animatable(selectedTab.toFloat()) }
    val onItemClickUpdated by rememberUpdatedState(onTabSelected)

    LaunchedEffect(selectedTab) {
        indicatorOffset.animateTo(
            targetValue = selectedTab.toFloat(),
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = 460f,
                visibilityThreshold = 0.001f
            )
        )
    }

    fun selectTab(index: Int) {
        val target = index.coerceIn(0, tabsCount - 1)
        if (target != selectedTab) {
            onItemClickUpdated(target)
        }
        animationScope.launch {
            indicatorOffset.animateTo(
                targetValue = target.toFloat(),
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = 460f,
                    visibilityThreshold = 0.001f
                )
            )
        }
    }

    val navBarBottomPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues().calculateBottomPadding()
    val bottomPaddingValue = if (navBarBottomPadding != 0.dp) 8.dp + navBarBottomPadding else 36.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(bottom = bottomPaddingValue, start = 24.dp, end = 24.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart,
        ) {
            CompositionLocalProvider(LocalContentColor provides tabContentColor) {
                // Glass Base Container
                Row(
                    modifier = Modifier
                        .selectableGroup()
                        .onSizeChanged { coords ->
                            totalWidthPx = coords.width.toFloat()
                            val contentWidthPx = totalWidthPx - with(density) { 8.dp.toPx() }
                            tabWidthPx = (contentWidthPx / tabsCount).coerceAtLeast(0f)
                        }
                        .dropShadow(
                            shape = pillShape,
                            shadow = Shadow(
                                radius = 12.dp,
                                color = Color.Black,
                                alpha = if (isDark) 0.28f else 0.12f,
                            ),
                        )
                        .then(
                            if (isBlurActive && backdrop != null) {
                                Modifier.drawBackdrop(
                                    backdrop = backdrop,
                                    shape = { pillShape },
                                    effects = {
                                        padding = maxOf(padding, 32.dp.toPx())
                                        vibrancy()
                                        blur(
                                            5.dp.toPx(),
                                            5.dp.toPx(),
                                        )
                                        lens(
                                            refractionHeight = 20.dp.toPx(),
                                            refractionAmount = 20.dp.toPx(),
                                        )
                                    },
                                    highlight = { iosGlassSpecular },
                                    onDrawSurface = { drawRect(containerColor) },
                                )
                            } else {
                                Modifier
                                    .background(containerColor, pillShape)
                                    .border(
                                        width = 1.dp,
                                        color = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.35f),
                                        shape = pillShape
                                    )
                            },
                        )
                        .pointerInput(tabsCount) {
                            detectDragGestures(
                                onDragEnd = {
                                    val finalIndex = indicatorOffset.targetValue.roundToInt().coerceIn(0, tabsCount - 1)
                                    selectTab(finalIndex)
                                },
                                onDragCancel = {
                                    selectTab(selectedTab)
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                if (tabWidthPx > 0f) {
                                    val delta = (dragAmount.x / tabWidthPx) * if (isLtr) 1f else -1f
                                    val newOffset = (indicatorOffset.value + delta).coerceIn(0f, (tabsCount - 1).toFloat())
                                    animationScope.launch {
                                        indicatorOffset.snapTo(newOffset)
                                    }
                                }
                            }
                        }
                        .height(64.dp)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = index == selectedTab
                        val itemColor by animateColorAsState(
                            targetValue = if (isSelected) accentColor else tabContentColor.copy(alpha = if (isDark) 0.65f else 0.75f),
                            animationSpec = tween(durationMillis = 180),
                            label = "tab_color_$index"
                        )

                        Column(
                            modifier = Modifier
                                .semantics(mergeDescendants = true) {
                                    selected = isSelected
                                    role = Role.Tab
                                    onClick {
                                        selectTab(index)
                                        true
                                    }
                                }
                                .onKeyEvent { event ->
                                    val isActivationKey = event.key == Key.Enter ||
                                        event.key == Key.NumPadEnter ||
                                        event.key == Key.Spacebar
                                    if (isActivationKey) {
                                        if (event.type == KeyEventType.KeyUp) selectTab(index)
                                        true
                                    } else {
                                        false
                                    }
                                }
                                .focusable()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { selectTab(index) }
                                )
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                modifier = Modifier.size(22.dp),
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = null,
                                tint = itemColor,
                            )
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = itemColor,
                            )
                        }
                    }
                }
            }

            // Liquid Glass Sliding Indicator Pill (Hardware-accelerated translation, 0 recomposition)
            if (tabWidthPx > 0f) {
                val tabWidthDp = with(density) { tabWidthPx.toDp() }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .graphicsLayer {
                            val currentOffset = indicatorOffset.value * tabWidthPx
                            translationX = if (isLtr) currentOffset else -currentOffset
                        }
                        .clip(pillShape)
                        .then(
                            if (isBlurActive && backdrop != null) {
                                Modifier.drawBackdrop(
                                    backdrop = backdrop,
                                    shape = { pillShape },
                                    effects = {
                                        vibrancy()
                                        blur(3.dp.toPx(), 3.dp.toPx())
                                        lens(
                                            refractionHeight = 8.dp.toPx(),
                                            refractionAmount = 8.dp.toPx(),
                                            depthEffect = false,
                                            chromaticAberration = 0f,
                                        )
                                    },
                                    highlight = { pillIndicatorSpecular },
                                    onDrawSurface = {
                                        drawRect(accentColor.copy(alpha = if (isDark) 0.22f else 0.16f))
                                    }
                                )
                            } else {
                                Modifier
                                    .background(accentColor.copy(alpha = 0.16f), pillShape)
                                    .border(
                                        width = 1.dp,
                                        color = accentColor.copy(alpha = 0.35f),
                                        shape = pillShape
                                    )
                            }
                        )
                        .height(56.dp)
                        .width(tabWidthDp),
                )
            }
        }
    }
}
