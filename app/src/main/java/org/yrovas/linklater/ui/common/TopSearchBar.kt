package org.yrovas.linklater.ui.common

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarScrollBehavior
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.awaitCancellation

// Pulled directly from Material3 components

internal val SearchBarAsTopBarPadding = 8.dp

@ExperimentalMaterial3Api
@Composable
fun FloatingTopSearchBar(
    state: SearchBarState,
    inputField: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = SearchBarDefaults.inputFieldShape,
    colors: SearchBarColors = SearchBarDefaults.colors(),
    tonalElevation: Dp = SearchBarDefaults.TonalElevation,
    shadowElevation: Dp = SearchBarDefaults.ShadowElevation,
    windowInsets: WindowInsets = SearchBarDefaults.windowInsets,
    scrollBehavior: SearchBarScrollBehavior? = null,
) {
    SearchBar(
        state = state,
        inputField = inputField,
        modifier = modifier
            .then((scrollBehavior?.let { with(it) { Modifier.searchBarScrollBehavior() } }
                ?: Modifier))
            .windowInsetsPadding(windowInsets)
            .padding(SearchBarAsTopBarPadding)
            .fillMaxWidth()
            .wrapContentWidth(),
        shape = shape,
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DisableSoftKeyboard(content: @Composable () -> Unit) {
    InterceptPlatformTextInput(interceptor = { _, _ -> awaitCancellation() }, content = content)
}

@ExperimentalMaterial3Api
@Composable
fun SearchBar(
    state: SearchBarState,
    inputField: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = SearchBarDefaults.inputFieldShape,
    colors: SearchBarColors = SearchBarDefaults.colors(),
    tonalElevation: Dp = SearchBarDefaults.TonalElevation,
    shadowElevation: Dp = SearchBarDefaults.ShadowElevation,
) {
    Surface(
        modifier = modifier.onGloballyPositioned { state.collapsedCoords = it },
        shape = shape,
        color = colors.containerColor,
        contentColor = contentColorFor(colors.containerColor),
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        content = inputField,
    )
}
