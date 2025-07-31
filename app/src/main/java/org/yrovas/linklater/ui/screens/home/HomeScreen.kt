package org.yrovas.linklater.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.ui.common.FloatingTopSearchBar
import org.yrovas.linklater.show
import org.yrovas.linklater.ui.common.Frame
import org.yrovas.linklater.ui.common.Icon
import org.yrovas.linklater.ui.common.KeyboardRow
import org.yrovas.linklater.ui.common.TagPredictRow
import org.yrovas.linklater.ui.screens.Destination
import org.yrovas.linklater.ui.screens.LocalBackStack
import org.yrovas.linklater.ui.screens.LocalSnackState
import org.yrovas.linklater.ui.screens.home.HomeModel.Effect
import org.yrovas.linklater.ui.screens.home.HomeModel.Event
import org.yrovas.linklater.ui.screens.home.components.BookmarkRow
import org.yrovas.linklater.ui.theme.padding

typealias HomeScreen = @Composable () -> Unit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Inject
@Composable
fun HomeScreen(homeModel: () -> HomeModel) {
    val state = viewModel { homeModel() }
    val scope = rememberCoroutineScope()
    val snackState = LocalSnackState.current
    val listState = rememberLazyListState()
    val expandedFab by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    val bookmarks by state.filteredBookmarks.collectAsState()
    val bookmarkCount by state.bookmarkCount.collectAsState()
    val filteredCount by state.filteredBookmarkCount.collectAsState()
    val searchBarState = rememberSearchBarState()
    val backStack = LocalBackStack.current
    val queryState = state.bookmarkQueryState
    val scrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
    val refreshState = rememberPullToRefreshState()
    val isRefreshing by state.isRefreshing.collectAsState()
    val resultText = if (bookmarks.isEmpty() && queryState.text.isNotBlank()) {
        "No bookmarks... try entering your credentials into the settings."
    } else if (queryState.text.isNotBlank()) {
        "Showing $filteredCount of $bookmarkCount bookmarks"
    } else {
        "Showing $bookmarkCount bookmarks"
    }
    val inputField = @Composable {
        SearchBarDefaults.InputField(
            modifier = Modifier,
            searchBarState = searchBarState,
            textFieldState = queryState,
            onSearch = {
                scope.launch { searchBarState.animateToCollapsed() }
            },
            placeholder = { Text("Bookmarks") },
            leadingIcon = {
                if (searchBarState.isExpanded) {
                    IconButton(
                        onClick = { scope.launch { searchBarState.animateToCollapsed() } }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                } else {
                    Icon(Icons.Default.Search)
                }
            },
            trailingIcon = {
                if (searchBarState.isExpanded) {
                    IconButton(
                        onClick = {
                            queryState.clearText()
                            scope.launch { searchBarState.animateToCollapsed() }
                        }) {
                        Icon(Icons.Default.Close)
                    }
                } else {
                    IconButton(
                        onClick = {
                            backStack.add(Destination.Preferences)
                        }) {
                        Icon(Icons.Default.MoreVert)
                    }
                }
            },
        )
    }

    state.subscribeEffects(scope) { effect ->
        when (effect) {
            is Effect.RefreshError -> snackState.show(effect.error)
        }
    }


    Frame(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FloatingTopSearchBar(
                scrollBehavior = scrollBehavior,
                state = searchBarState,
                inputField = inputField,
            )
        },
        fab = {
            ExtendedFloatingActionButton(
                onClick = { backStack.add(Destination.SaveBookmark) },
                expanded = expandedFab,
                icon = { Icon(Icons.Filled.AddLink) },
                text = { Text("Add bookmark") },
            )
        },
        globalContent = {
            if (searchBarState.isExpanded) KeyboardRow {
                TagPredictRow(predictedTags = state.predictedTags) {
                    state.sendEvent(Event.SelectTagPrediction(it))
                }
            }
        },
    ) {
        PullToRefreshBox(
            state = refreshState,
            isRefreshing = isRefreshing,
            onRefresh = { state.sendEvent(Event.RefreshBookmarks) },
            indicator = {
                PullToRefreshDefaults.IndicatorBox(
                    state = refreshState,
                    isRefreshing = isRefreshing,
                    containerColor = colorScheme.surface,
                    modifier = Modifier.align(Alignment.TopCenter),
                ) {
                    val mod = Modifier.padding(padding.xs)
                    if (isRefreshing) {
                        CircularWavyProgressIndicator(modifier = mod)
                    } else {
                        CircularWavyProgressIndicator(
                            modifier = mod,
                            progress = { refreshState.distanceFraction },
                            trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                        )
                    }
                }
            }) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(padding.xs),
                verticalArrangement = Arrangement.spacedBy(padding.sm),
            ) {
                item {
                    Text(
                        modifier = Modifier
                            .padding(top = padding.md)
                            .padding(horizontal = padding.md),
                        text = resultText,
                        color = colorScheme.primary,
                        style = typography.bodySmall
                    )
                }
                items(bookmarks, key = { it.id }) { bookmark ->
                    BookmarkRow(bookmark) { tag ->
                        state.sendEvent(Event.SearchForTag(tag))
                        scope.launch {
                            listState.animateScrollToItem(0)
                            scrollBehavior.scrollOffset = 0f
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private val SearchBarState.isExpanded
    get() = this.currentValue == SearchBarValue.Expanded
