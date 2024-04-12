package org.yrovas.linklater.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun RefreshIcon(
    modifier: Modifier = Modifier,
    refreshing: StateFlow<Boolean>,
    icon: ImageVector = Icons.Default.Refresh,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    val slowInFastOutEasing: Easing =
        remember { CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f) }
    val linearInFastOutEasing: Easing =
        remember { CubicBezierEasing(0.4f, 0.0f, 0.25f, 1.0f) }

    val isRefreshing by refreshing.collectAsState()
    var didRefresh by remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isRefreshing) {
        launch {
            val duration = 750
            val target = 360f
            if (isRefreshing) {
                didRefresh = true
                rotation.animateTo(
                    targetValue = 360f, animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = duration, easing = slowInFastOutEasing
                        ), repeatMode = RepeatMode.Restart
                    )
                )
            } else if (didRefresh) {
                val remainingMillis: Int = ((target - rotation.value) / target * duration).toInt()
                val easing = if (remainingMillis > (duration/2)) linearInFastOutEasing else LinearEasing
                rotation.animateTo(
                    targetValue = 360f,
                    initialVelocity = rotation.velocity,
                    animationSpec = tween(
                        durationMillis = remainingMillis, easing = easing
                    )
                )
                rotation.snapTo(0f)
            }
        }
    }

    Icon(
        imageVector = icon,
        tint = tint,
        modifier = modifier.rotate(rotation.value)
    )
}
