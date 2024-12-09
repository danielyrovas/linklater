package org.yrovas.linklater.ui.screens.home

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.filter
import org.yrovas.linklater.show
import org.yrovas.linklater.ui.common.Frame
import org.yrovas.linklater.ui.common.Icon
import org.yrovas.linklater.ui.screens.home.HomeState.Effect
import org.yrovas.linklater.ui.screens.home.HomeState.Event
import org.yrovas.linklater.ui.screens.home.components.BookmarkRow
import org.yrovas.linklater.ui.screens.home.components.HomeAppBar
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkDestination
import org.yrovas.linklater.ui.theme.padding

@Composable
fun HomeScreen(
    nav: NavController,
    snackState: SnackbarHostState,
    state: () -> HomeState,
) {
    @Suppress("NAME_SHADOWING") val state = viewModel { state() }
//    val LocalState = staticCompositionLocalOf { viewModel { state() } }
//    val state = LocalState.current
    val scope = rememberCoroutineScope()
    val bookmarks by state.displayedBookmarks.collectAsState()
    val bookmarkCount by state.bookmarkCount.collectAsState()
    val filteredCount by state.filteredBookmarkCount.collectAsState()
    val listState = rememberLazyListState()
    val searchBarActive by state.searchBarActive.collectAsState()
    var expandAppBar by remember { mutableStateOf(listState.firstVisibleItemIndex == 0) }

    state.subscribeEffects(scope) { effect ->
        when (effect) {
            is Effect.RefreshError -> snackState.show(effect.error)
            Effect.RefreshOk -> listState.animateScrollToItem(0)
            Effect.FilterChanged -> listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.filter { it in 0..1 } // only care about change from being at the `top`
            .collect {
                Log.d(TAG, "HomeScreen: First item visible $it")
                expandAppBar = it == 0
            }
    }

    Frame(appBar = { HomeAppBar(
        nav = nav,
        bookmarkCount = bookmarkCount,
        filteredCount = filteredCount,
        isExpanded = expandAppBar,
        searchBarActive = searchBarActive,
        isRefreshing = state.isRefreshing,
        onRefreshClick = { state.sendEvent(Event.RefreshBookmarks) },
        searchState = state.bookmarkQueryState,
        onSearchChange = { state.sendEvent(Event.UpdateQuery) },
        onClearSearch = { state.sendEvent(Event.SearchBarClose) },
    )}, fab = {
        FloatingActionButton(modifier = Modifier.padding(padding.standard),
            onClick = { nav.navigate(SaveBookmarkDestination) },
            content = { Icon(imageVector = Icons.Default.AddLink) })
    }, snackState = snackState) {
        LazyColumn(
            state = listState, modifier = Modifier.padding(horizontal = padding.standard)
        ) {
            if (bookmarks.isEmpty() && !searchBarActive) {
                item {
                    Text(text = "No bookmarks... try entering your credentials into the settings.")
                }
            }
            items(bookmarks, key = { it.id }) { BookmarkRow(it) }
        }
    }
}
