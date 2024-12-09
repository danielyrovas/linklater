package org.yrovas.linklater.ui.screens.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.yrovas.linklater.ThemePreview
import org.yrovas.linklater.ui.common.AnimateExpandAppBar
import org.yrovas.linklater.ui.common.Icon
import org.yrovas.linklater.ui.common.RefreshIcon
import org.yrovas.linklater.ui.common.StyledTextField
import org.yrovas.linklater.ui.screens.preferences.PreferencesDestination
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding

@Composable
fun HomeAppBar(
    nav: NavController,
    bookmarkCount: Int,
    filteredCount: Int,
    isExpanded: Boolean,
    searchBarActive: Boolean,
    isRefreshing: StateFlow<Boolean>,
    onRefreshClick: () -> Unit,
    searchState: TextFieldState,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
) {
    AnimateExpandAppBar(isExpanded = isExpanded) {
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxHeight()
        ) {
            if (searchBarActive) {
                SearchRow(searchState = searchState, onSearchChange = onSearchChange, onClearSearch = onClearSearch )
            }
            Row(horizontalArrangement = Arrangement.Start) {
                Spacer(modifier = Modifier.width(padding.standard))
                Text(
                    text = "Bookmarks ",
                    style = typography.titleLarge,
                    color = colorScheme.primary,
                )
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(222)),
                    exit = fadeOut(animationSpec = tween(222)),
                ) {
                    Text(
                        text = "(${if (searchBarActive) "$filteredCount of $bookmarkCount" else "$bookmarkCount"})",
                        style = typography.titleLarge,
                        color = colorScheme.primary,
                    )
                }
            }
        }
        Column(
            verticalArrangement = Arrangement.Top, modifier = Modifier.fillMaxHeight()
        ) {
            if (searchBarActive) {
            } else {
                Spacer(modifier = Modifier.height(padding.standard))
                Row(
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onRefreshClick) {
                        RefreshIcon(isRefreshing = isRefreshing)
                    }
                    IconButton(onClick = { nav.navigate(PreferencesDestination) }) {
                        Icon(
                            imageVector = Icons.Default.Settings, tint = colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchRow(
    searchState: TextFieldState,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.width(padding.standard))
        StyledTextField(
            modifier = Modifier
                .height(40.dp)
                .weight(1f),
            state = searchState
        )
//        Spacer(Modifier.width(padding.standard))
        IconButton(onClick = onClearSearch) {
            Icon(
                imageVector = Icons.Default.Close, tint = colorScheme.primary
            )
        }
    }
}

@ThemePreview
@Composable
private fun SearchRowPreview() {
    AppTheme {
        Surface {
            SearchRow(rememberTextFieldState(), {}, {})
        }
    }
}

@ThemePreview
@Composable
private fun HomeAppBarPreview() {
    var isExpanded by remember { mutableStateOf(true) }
    var searchBarActive by remember { mutableStateOf(true) }

    AppTheme {
        HomeAppBar(nav = NavController(LocalContext.current),
            isExpanded = isExpanded,
            searchBarActive = searchBarActive,
            isRefreshing = MutableStateFlow(false),
            bookmarkCount = 403,
            onRefreshClick = { isExpanded = !isExpanded },
            onSearchChange = {},
            searchState = rememberTextFieldState(),
            filteredCount = 1,
            onClearSearch = { searchBarActive = false })
    }
}
