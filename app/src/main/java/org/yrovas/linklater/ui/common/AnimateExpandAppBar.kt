package org.yrovas.linklater.ui.common

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AnimateExpandAppBar(
    isExpanded: Boolean,
    content: @Composable () -> Unit,
) {
    val height by animateDpAsState(
        targetValue = if (isExpanded) 128.dp else 64.dp, label = "row height"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(colorScheme.background),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

