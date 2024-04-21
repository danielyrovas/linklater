package org.yrovas.linklater.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.PreferencesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SaveBookmarkScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import org.yrovas.linklater.domain.APIError
import org.yrovas.linklater.show
import org.yrovas.linklater.ui.common.AppBar
import org.yrovas.linklater.ui.common.BookmarkRow
import org.yrovas.linklater.ui.common.Frame
import org.yrovas.linklater.ui.common.Icon
import org.yrovas.linklater.ui.common.RefreshIcon
import org.yrovas.linklater.ui.state.HomeScreenState
import org.yrovas.linklater.ui.state.HomeScreenState.Effect
import org.yrovas.linklater.ui.state.HomeScreenState.Event
import org.yrovas.linklater.ui.theme.padding

@Destination<RootGraph>(start = true)
@Composable
fun HomeScreen(
    nav: DestinationsNavigator,
    snackState: SnackbarHostState,
    state: () -> HomeScreenState,
) {
    @Suppress("NAME_SHADOWING") val state = viewModel { state() }
    val scope = rememberCoroutineScope()
    val bookmarks by state.displayedBookmarks.collectAsState()
    val bookmarkCount by state.bookmarkCount.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(true) {
        scope.launch {
            state.effect.collect { effect ->
                when (effect) {
                    is Effect.RefreshError -> {
                        snackState.show(effect.error)
                    }

                    Effect.RefreshOk -> {
                        listState.animateScrollToItem(0)
                    }
                }
            }
        }
    }

    Frame(appBar = {
        AppBar(page = "Bookmarks", back = null) {
            IconButton(onClick = { state.sendEvent(Event.RefreshBookmarks) }) {
                RefreshIcon(refreshing = state.isRefreshing)
            }
            IconButton(onClick = { nav.navigate(PreferencesScreenDestination) }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    tint = colorScheme.primary
                )
            }

        }
    }, fab = {
        FloatingActionButton(modifier = Modifier.padding(padding.standard),
            onClick = { nav.navigate(SaveBookmarkScreenDestination) },
            content = { Icon(imageVector = Icons.Default.AddLink) })
    }, snackState = snackState) {
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(horizontal = padding.standard)
        ) {
            if (bookmarks.isEmpty()) {
                item {
                    Text(text = "No bookmarks... try entering your credentials into the settings")
                }
            } else {
                item {
                    Text(
                        text = "Showing $bookmarkCount bookmarks...",
                        style = typography.labelMedium,
                        color = colorScheme.secondary
                    )
                }
            }
            items(bookmarks, key = { it.id }) { BookmarkRow(it) }
        }
    }
}

//@ThemePreview
//@Composable
//fun HomeScreenPreview() {
//    val state = HomeScreenState(EmptyBookmarkAPI(), EmptyBookmarkSource())
//    state.setDisplayedBookmarks(
//        listOf(
//            Bookmark(
//                1,
//                "https://garb.com",
//                website_title = "Pls github | stop making the titles way to large. This is running over 3 lines!!! ludicrous",
//                website_description = "its a github, also a lame hub, but quite a bit too large? should be concatonated. This runs over way too many lines and should be reduced to fit the correct area",
//                date_modified = "2024-02-06T00:05:35.570735Z",
//                tags = listOf("span", "the", "flames")
//            ), Bookmark(
//                2,
//                "https://www.garb.com",
//                website_description = "its a github",
//                date_modified = "2022-02-06T00:05:35.570735Z",
//                tags = listOf("art", "science", "bullshit")
//            ), Bookmark(
//                3,
//                "https://garb.com",
//                website_title = "Pls github | stop making the titles way to large. This is running over 3 lines!!! ludicrous",
//                website_description = "its a github, also a lame hub, but quite a bit too large? should be concatonated. This runs over way too many lines and should be reduced to fit the correct area",
//                date_modified = "2024-02-06T00:05:35.570735Z",
//                tags = listOf("span", "the", "flames")
//            ), Bookmark(
//                4,
//                "https://www.garb.com",
//                website_description = "its a github",
//                date_modified = "2022-02-06T00:05:35.570735Z",
//                tags = listOf("art", "science", "bullshit")
//            ), Bookmark(
//                5,
//                "https://www.garb.com",
//                website_description = "its a github",
//                date_modified = "2022-02-06T00:05:35.570735Z",
//                tags = listOf("art", "science", "bullshit")
//            )
//        )
//    )
//    AppTheme {
//        HomeScreen(
//            EmptyDestinationsNavigator,
//            SnackbarHostState(),
//        ) { state }
//    }
//}
