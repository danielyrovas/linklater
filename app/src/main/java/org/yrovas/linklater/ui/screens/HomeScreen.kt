package org.yrovas.linklater.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.PreferencesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SaveBookmarkScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.yrovas.linklater.ThemePreview
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.data.remote.EmptyBookmarkAPI
import org.yrovas.linklater.domain.APIError
import org.yrovas.linklater.domain.Err
import org.yrovas.linklater.domain.Ok
import org.yrovas.linklater.ui.common.AppBar
import org.yrovas.linklater.ui.common.BookmarkRow
import org.yrovas.linklater.ui.common.Frame
import org.yrovas.linklater.ui.common.Icon
import org.yrovas.linklater.ui.state.HomeScreenState
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding

@Destination<RootGraph>(start = true)
@Composable
fun HomeScreen(
    nav: DestinationsNavigator,
    snackState: SnackbarHostState,
    setup_complete: StateFlow<Boolean>,
    state: () -> HomeScreenState,
) {
    val state = viewModel { state() }
    val setup_complete by setup_complete.collectAsState()
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

    LaunchedEffect(setup_complete) {
        if (state.displayedBookmarks.value.isEmpty()) {
            Log.d("DEBUG", "HomeScreen: REFRESHING")
            refresh()
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
        if (!setup_complete) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(horizontal = padding.standard)
            ) {
                items(bookmarks, key = { it.id }) { BookmarkRow(it) }
            }
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
            EmptyDestinationsNavigator,
            SnackbarHostState(),
            MutableStateFlow(true),
            { state }
        )
    }
}
