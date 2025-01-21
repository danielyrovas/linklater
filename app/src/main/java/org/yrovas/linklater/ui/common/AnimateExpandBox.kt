package org.yrovas.linklater.ui.common

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AnimateExpandBox(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .background(colorScheme.background),
    isExpanded: Boolean,
    height: Pair<Dp, Dp> = 64.dp to 128.dp,
    content: @Composable () -> Unit,
) {
    val h by animateDpAsState(
        targetValue = if (isExpanded) height.second else height.first, label = "row height"
    )
    Box(modifier = modifier.height(h), contentAlignment = Alignment.Center) {
        content()
    }
}
