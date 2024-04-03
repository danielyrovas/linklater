package org.yrovas.linklater.ui.common

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.net.toUri
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.yrovas.linklater.data.Bookmark
import org.yrovas.linklater.openUri
import org.yrovas.linklater.timeAgo
import org.yrovas.linklater.ui.theme.padding
import java.net.URI
@Composable
fun Frame(
    appBar: @Composable () -> Unit,
    fab: (@Composable () -> Unit)? = null,
    snackState: SnackbarHostState,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                appBar()
                content()
            }
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                fab?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) { fab() }
                }
                SnackbarHost(hostState = snackState)
            }
        }
    }
}


@Composable
fun BookmarkRow(
    bookmark: Bookmark,
    context: Context = LocalContext.current,
) {
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
        Spacer(modifier = androidx.compose.ui.Modifier.height(padding.tiny))
        Row {
            Text(text = if (!bookmark.title.isNullOrBlank()) {
                bookmark.title
            } else if (!bookmark.website_title.isNullOrBlank()) {
                bookmark.website_title
            } else {
                bookmark.url.substringAfter("://")
            },
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    context.openUri(bookmark.url.toUri())
                })
        }
        if (!bookmark.description.isNullOrBlank() || !bookmark.website_description.isNullOrBlank()) {
            Spacer(modifier = androidx.compose.ui.Modifier.height(padding.half))
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
            Spacer(modifier = androidx.compose.ui.Modifier.height(padding.half))
            LazyRow {
                items(bookmark.tags, key = { it }) {
                    Text(
                        text = "#$it", color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = androidx.compose.ui.Modifier.width(padding.standard))
                }
            }
        }
    }
}
