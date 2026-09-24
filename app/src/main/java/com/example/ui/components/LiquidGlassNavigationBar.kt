package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

data class NavTabItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
    val testTag: String
)

@Composable
fun LiquidGlassNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember {
        listOf(
            NavTabItem(
                selectedIcon = Icons.Filled.Schedule,
                unselectedIcon = Icons.Outlined.Schedule,
                label = "Schedule",
                testTag = "nav_item_schedule"
            ),
            NavTabItem(
                selectedIcon = Icons.Filled.CalendarViewWeek,
                unselectedIcon = Icons.Outlined.CalendarViewWeek,
                label = "Weekly",
                testTag = "nav_item_weekly"
            ),
            NavTabItem(
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                label = "Settings",
                testTag = "nav_item_settings"
            )
        )
    }

    val isDark = isSystemInDarkTheme()
    val navBarBottomInset = WindowInsets.navigationBars
        .only(WindowInsetsSides.Bottom)
        .asPaddingValues()
        .calculateBottomPadding()

    // 3 items: calculate total width and item width
    val totalWidth = 310.dp
    val itemWidth = totalWidth / items.size
    val indicatorPadding = 4.dp
    val indicatorWidth = itemWidth - (indicatorPadding * 2)

    val targetOffsetX = itemWidth * selectedTab + indicatorPadding
    val animatedOffsetX by animateDpAsState(
        targetValue = targetOffsetX,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 320f
        ),
        label = "indicator_offset"
    )

    // Glass capsule container background & specular highlight border
    val glassBg = if (isDark) Color(0xD91C1C24) else Color(0xEBFCFCFE)
    val glassBorder = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = if (isDark) 0.28f else 0.75f),
            Color.White.copy(alpha = if (isDark) 0.05f else 0.20f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = navBarBottomInset + 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating liquid glass capsule
        Box(
            modifier = Modifier
                .width(totalWidth)
                .height(64.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color.Black.copy(alpha = if (isDark) 0.50f else 0.15f),
                    ambientColor = Color.Black.copy(alpha = if (isDark) 0.30f else 0.10f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(glassBg)
                .border(
                    BorderStroke(1.dp, glassBorder),
                    RoundedCornerShape(32.dp)
                )
                .padding(4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Liquid Sliding Active Indicator Pill
            Box(
                modifier = Modifier
                    .offset { IntOffset(animatedOffsetX.roundToPx(), 0) }
                    .width(indicatorWidth)
                    .height(56.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = MiuixTheme.colorScheme.primary.copy(alpha = 0.25f),
                        ambientColor = Color.Transparent
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        MiuixTheme.colorScheme.primary.copy(alpha = if (isDark) 0.20f else 0.14f)
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            MiuixTheme.colorScheme.primary.copy(alpha = if (isDark) 0.38f else 0.28f)
                        ),
                        RoundedCornerShape(28.dp)
                    )
            )

            // Nav Tabs Row
            Row(
                modifier = Modifier
                    .width(totalWidth - 8.dp)
                    .height(56.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    val iconScale by animateFloatAsState(
                        targetValue = when {
                            isPressed -> 0.85f
                            isSelected -> 1.15f
                            else -> 1.0f
                        },
                        animationSpec = spring(
                            dampingRatio = 0.55f,
                            stiffness = 380f
                        ),
                        label = "tab_icon_scale"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected)
                            MiuixTheme.colorScheme.primary
                        else
                            MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        animationSpec = tween(durationMillis = 200),
                        label = "tab_color"
                    )

                    Column(
                        modifier = Modifier
                            .width(itemWidth)
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onTabSelected(index) }
                            )
                            .testTag(item.testTag),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = contentColor,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}
