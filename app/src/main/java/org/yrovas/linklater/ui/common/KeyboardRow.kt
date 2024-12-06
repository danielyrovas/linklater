package org.yrovas.linklater.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeyboardRow(content: @Composable () -> Unit) {
    val isImeVisible = WindowInsets.isImeVisible
    val density = LocalDensity.current

    val offsetY = WindowInsets.ime.getBottom(density)
            // take into account padding from system bars (navigation pill/buttons)
            // No longer need to as global box is inside padding
            // - WindowInsets.systemBars.getBottom(density)

    var previousOffset by remember { mutableIntStateOf(0) }

    val isKeyboardGoingDown by remember(offsetY) {
        derivedStateOf {
            val isGoingDown = previousOffset - offsetY > 0
            previousOffset = offsetY
            isGoingDown
        }
    }
    val showKeyboardRow by remember(
        isImeVisible, isKeyboardGoingDown, offsetY
    ) {
        mutableStateOf(isImeVisible && !isKeyboardGoingDown && offsetY != 0)
    }

    if (showKeyboardRow) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(3f),
            contentAlignment = Alignment.BottomStart
        ) {
            Box(modifier = Modifier
                .offset { IntOffset(0, -offsetY) }
                .fillMaxWidth()) {
                content()
            }
        }
    }
}
