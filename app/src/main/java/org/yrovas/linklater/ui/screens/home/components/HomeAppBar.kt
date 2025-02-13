package org.yrovas.linklater.ui.screens.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.yrovas.linklater.ThemePreview
import org.yrovas.linklater.ui.common.AnimateExpandBox
import org.yrovas.linklater.ui.common.Icon
import org.yrovas.linklater.ui.common.RefreshIcon
import org.yrovas.linklater.ui.common.StyledTextField
import org.yrovas.linklater.ui.screens.Destination
import org.yrovas.linklater.ui.screens.NavModel
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding

@Composable
fun HomeAppBar(
    bookmarkCount: Int,
    filteredCount: Int,
    isExpanded: Boolean,
    searchBarActive: Boolean,
    isRefreshing: StateFlow<Boolean>,
    onRefreshClick: () -> Unit,
    searchState: TextFieldState,
    onClearSearch: () -> Unit,
) {
    AnimateExpandBox(isExpanded = isExpanded) {
        if (!(searchBarActive && !isExpanded)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(padding.standard),
                contentAlignment = if (isExpanded) Alignment.BottomStart else Alignment.CenterStart
            ) {
                AppHeader(isExpanded, searchBarActive, bookmarkCount, filteredCount)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(vertical = padding.standard),
//            contentAlignment = if (isExpanded) Alignment.TopStart else Alignment.TopStart
        ) {
            if (searchBarActive) {
                SearchRow(searchState = searchState, onClearSearch = onClearSearch)
            } else {
                DefaultActions(isExpanded, onRefreshClick, isRefreshing)
            }
        }
    }
}

@Composable
fun AppHeader(
    isExpanded: Boolean, searchBarActive: Boolean, bookmarkCount: Int, filteredCount: Int
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Bookmarks ",
            style = typography.titleLarge,
            color = colorScheme.primary,
        )
        Spacer(modifier = Modifier.weight(1f))
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

@Composable
fun DefaultActions(
    isExpanded: Boolean, onRefreshClick: () -> Unit, isRefreshing: StateFlow<Boolean>
) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.Search, tint = colorScheme.primary
            )
        }
        if (isExpanded) {
            Spacer(Modifier.weight(1f))
//            val x by animateFloatAsState(targetValue = if (isExpanded) 1f else 0f)
//            Spacer(modifier = Modifier.weight(x))
        }
        Row {
            IconButton(onClick = onRefreshClick) {
                RefreshIcon(isRefreshing = isRefreshing)
            }
            IconButton(onClick = { NavModel.navigate(Destination.Preferences) }) {
                Icon(
                    imageVector = Icons.Default.Settings, tint = colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SearchRow(
    searchState: TextFieldState, onClearSearch: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(padding.standard))
            StyledTextField(
                modifier = Modifier
                    .height(40.dp)
                    .weight(1f), state = searchState
            )
            IconButton(onClick = onClearSearch) {
                Icon(
                    imageVector = Icons.Default.Close, tint = colorScheme.primary
                )
            }
        }
    }
}

@ThemePreview
@Composable
private fun HomeAppBarExpandedSearchPreview() {
    var isExpanded by remember { mutableStateOf(true) }
    var searchBarActive by remember { mutableStateOf(true) }

    AppTheme {
        HomeAppBar(isExpanded = isExpanded,
            searchBarActive = searchBarActive,
            isRefreshing = MutableStateFlow(false),
            bookmarkCount = 403,
            onRefreshClick = { isExpanded = !isExpanded },
            searchState = rememberTextFieldState(),
            filteredCount = 1,
            onClearSearch = { searchBarActive = false })
    }
}

@ThemePreview
@Composable
private fun HomeAppBarCollapsedSearchPreview() {
    var isExpanded by remember { mutableStateOf(false) }
    var searchBarActive by remember { mutableStateOf(true) }

    AppTheme {
        HomeAppBar(isExpanded = isExpanded,
            searchBarActive = searchBarActive,
            isRefreshing = MutableStateFlow(false),
            bookmarkCount = 403,
            onRefreshClick = { isExpanded = !isExpanded },
            searchState = rememberTextFieldState(),
            filteredCount = 1,
            onClearSearch = { searchBarActive = false })
    }
}

@ThemePreview
@Composable
private fun HomeAppBarExpandedPreview() {
    var isExpanded by remember { mutableStateOf(true) }
    var searchBarActive by remember { mutableStateOf(false) }

    AppTheme {
        HomeAppBar(isExpanded = isExpanded,
            searchBarActive = searchBarActive,
            isRefreshing = MutableStateFlow(false),
            bookmarkCount = 403,
            onRefreshClick = { isExpanded = !isExpanded },
            searchState = rememberTextFieldState(),
            filteredCount = 1,
            onClearSearch = { searchBarActive = false })
    }
}

@ThemePreview
@Composable
private fun HomeAppBarCollapsedPreview() {
    var isExpanded by remember { mutableStateOf(false) }
    var searchBarActive by remember { mutableStateOf(false) }

    AppTheme {
        HomeAppBar(isExpanded = isExpanded,
            searchBarActive = searchBarActive,
            isRefreshing = MutableStateFlow(false),
            bookmarkCount = 403,
            onRefreshClick = { isExpanded = !isExpanded },
            searchState = rememberTextFieldState(),
            filteredCount = 1,
            onClearSearch = { searchBarActive = false })
    }
}
