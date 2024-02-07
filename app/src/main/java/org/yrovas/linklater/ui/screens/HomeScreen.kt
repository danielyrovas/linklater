package org.yrovas.linklater.ui.screens

import android.content.Context
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.yrovas.linklater.MainActivityState
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.destinations.PreferencesScreenDestination
import org.yrovas.linklater.destinations.SaveBookmarkActivityDestination
import org.yrovas.linklater.timeAgo
import org.yrovas.linklater.ui.common.AppBar
import org.yrovas.linklater.ui.common.Icon
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding
import java.net.URI

@Preview(name = "Dark Mode", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "Light Mode", showBackground = true, uiMode = UI_MODE_NIGHT_NO)
annotation class ThemePreview

@OptIn(ExperimentalMaterialApi::class) // for rememberPullRefresh
@RootNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(
    nav: DestinationsNavigator, mainActivityState: MainActivityState,
    context: Context = LocalContext.current,
) {
    val bookmarks by mainActivityState.displayedBookmarks.collectAsState()
    val isRefreshing by mainActivityState.isRefreshing.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val pullRefreshState = rememberPullRefreshState(isRefreshing, {
        mainActivityState.refresh(context)
        scope.launch { listState.animateScrollToItem(0) }
    })

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(1F),
                backgroundColor = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pullRefresh(pullRefreshState)
                        .verticalScroll(rememberScrollState())
                        .zIndex(-1F)
                ) {
                    HomeBar("Home", nav)
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .padding(horizontal = padding.standard)
                ) {
                    items(bookmarks, key = { it.id }) { BookmarkRow(it) }
                }
            }
            FloatingActionButton(modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(padding.standard), onClick = {
                nav.navigate(SaveBookmarkActivityDestination)
            }) {
                Icon(imageVector = Icons.Default.Add)
            }
        }
    }
}

@Composable
fun HomeBar(page: String, nav: DestinationsNavigator) {
    AppBar(page = page, nav = nav, back = null) {
        IconButton(onClick = { nav.navigate(PreferencesScreenDestination) }) {
            Icon(
                imageVector = Icons.Default.Settings,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(padding.half))
    }
}

@Composable
fun BookmarkRow(bookmark: Bookmark) {
    Column(
        modifier = Modifier.padding(vertical = padding.standard),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = URI(bookmark.url).host ?: bookmark.url,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = if (bookmark.date_modified.isNullOrBlank()) "" else timeAgo(
                    Instant.parse(bookmark.date_modified), Clock.System.now()
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Spacer(modifier = Modifier.height(padding.tiny))
        Row {
            Text(
                text = if (!bookmark.title.isNullOrBlank()) {
                    bookmark.title
                } else if (!bookmark.website_title.isNullOrBlank()) {
                    bookmark.website_title
                } else {
                    bookmark.url.substringAfter("://")
                },
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (!bookmark.description.isNullOrBlank() || !bookmark.website_description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(padding.half))
            Row {
                Text(
                    text = if (!bookmark.description.isNullOrBlank()) {
                        bookmark.description
                    } else if (!bookmark.website_description.isNullOrBlank()) {
                        bookmark.website_description
                    } else {
                        ""
                    },
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        if (bookmark.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(padding.half))
            LazyRow(
//            modifier = Modifier.padding(
//                horizontal = padding.standard,
//            ),
            ) {
                items(bookmark.tags, key = { it }) {
                    Text(
                        text = "#$it", color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(padding.standard))
                }
            }
        }
    }
}

@ThemePreview
@Composable
fun BookmarkRowsPreview() {
    AppTheme(darkTheme = true) {
        Surface {
            Column(modifier = Modifier.padding(padding.standard)) {
                BookmarkRow(
                    bookmark = Bookmark(
                        1,
                        "https://garb.com",
                        website_title = "Pls github | stop making the titles way to large. This is running over 3 lines!!! ludicrous",
                        website_description = "its a github, also a lame hub, but quite a bit too large? should be concatonated. This runs over way too many lines and should be reduced to fit the correct area",
                        date_modified = "2024-02-06T00:05:35.570735Z",
                        tags = listOf("gas", "the", "jews")
                    )
                )
//                Spacer(modifier = Modifier.height(padding.double))
                BookmarkRow(
                    bookmark = Bookmark(
                        2,
                        "https://www.garb.com",
                        website_description = "its a github",
                        date_modified = "2022-02-06T00:05:35.570735Z",
                        tags = listOf("art", "science", "bullshit")
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    AppTheme(darkTheme = true) {
        HomeScreen(EmptyDestinationsNavigator, MainActivityState())
    }
}

@Preview
@Composable
fun HomeScreenLightPreview() {
    AppTheme(darkTheme = false) {
        HomeScreen(EmptyDestinationsNavigator, MainActivityState())
    }
}
