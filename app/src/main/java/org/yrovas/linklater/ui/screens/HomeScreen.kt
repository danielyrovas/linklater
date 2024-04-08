package org.yrovas.linklater.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.PreferencesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SaveBookmarkScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.*
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.domain.*
import org.yrovas.linklater.ui.common.*
import org.yrovas.linklater.ui.state.HomeScreenState
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding

@Destination<RootGraph>(start = true)
@Inject
@Composable
fun HomeScreen(
    nav: DestinationsNavigator,
    snackState: SnackbarHostState,
    appViewModel: AppViewModel,
    state: HomeScreenState = viewModel { HomeScreenState(appViewModel.bookmarkAPI) },
) {
    val scope = rememberCoroutineScope()
    val bookmarks by state.displayedBookmarks.collectAsState()
    val listState = rememberLazyListState()
    val refresh = {
        state.refreshBookmarks { result ->
            scope.launch {
                when (result) {
                    is Err -> snackState.show(result.error)
                    is Ok -> {
                        delay(1); listState.animateScrollToItem(0)
                    }
                }
            }
        }
    }
    Frame(appBar = {
        AppBar(page = "Bookmarks", back = null) {
            IconButton(onClick = { refresh() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    tint = colorScheme.primary
                )
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
            items(bookmarks, key = { it.id }) { BookmarkRow(it) }
        }
    }
}

private suspend fun SnackbarHostState.show(error: APIError) {
    when (error) {
        APIError.CONNECTION -> showSnackbar("Could not connect to LinkDing ")
        APIError.AUTH -> showSnackbar("Invalid Credentials")
    }
}

@ThemePreview
@Composable
fun HomeScreenPreview() {
    val state = HomeScreenState(EmptyBookmarkAPI())
    state.setDisplayedBookmarks(
        listOf(
            Bookmark(
                1,
                "https://garb.com",
                website_title = "Pls github | stop making the titles way to large. This is running over 3 lines!!! ludicrous",
                website_description = "its a github, also a lame hub, but quite a bit too large? should be concatonated. This runs over way too many lines and should be reduced to fit the correct area",
                date_modified = "2024-02-06T00:05:35.570735Z",
                tags = listOf("span", "the", "flames")
            ), Bookmark(
                2,
                "https://www.garb.com",
                website_description = "its a github",
                date_modified = "2022-02-06T00:05:35.570735Z",
                tags = listOf("art", "science", "bullshit")
            ), Bookmark(
                3,
                "https://garb.com",
                website_title = "Pls github | stop making the titles way to large. This is running over 3 lines!!! ludicrous",
                website_description = "its a github, also a lame hub, but quite a bit too large? should be concatonated. This runs over way too many lines and should be reduced to fit the correct area",
                date_modified = "2024-02-06T00:05:35.570735Z",
                tags = listOf("span", "the", "flames")
            ), Bookmark(
                4,
                "https://www.garb.com",
                website_description = "its a github",
                date_modified = "2022-02-06T00:05:35.570735Z",
                tags = listOf("art", "science", "bullshit")
            ), Bookmark(
                5,
                "https://www.garb.com",
                website_description = "its a github",
                date_modified = "2022-02-06T00:05:35.570735Z",
                tags = listOf("art", "science", "bullshit")
            )
        )
    )
    AppTheme {
        HomeScreen(
            EmptyDestinationsNavigator, SnackbarHostState(), PreviewAppViewModel(), state
        )
    }
}
