package org.yrovas.linklater.ui.screens.home.components

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.ktor.http.Url
import org.yrovas.linklater.ThemePreview
import org.yrovas.linklater.data.models.Bookmark
import org.yrovas.linklater.data.models.showDescriptionOrElse
import org.yrovas.linklater.data.models.showTitleOrElse
import org.yrovas.linklater.openUri
import org.yrovas.linklater.timeAgo
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Composable
fun BookmarkRow(bookmark: Bookmark, onTagSelect: (tag: String) -> Unit) {
    val context: Context = LocalContext.current
    var selected by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { selected = !selected },
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(padding.xs))
        Column(
            modifier = Modifier
                .padding(horizontal = padding.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = Url(bookmark.url).host,
                    overflow = TextOverflow.Ellipsis,
                    style = typography.labelLarge,
                    color = colorScheme.outline,
                )
                Text(
                    text = if (bookmark.date_modified.isNullOrBlank()) "" else timeAgo(
                        Instant.parse(bookmark.date_modified), Clock.System.now()
                    ), style = typography.labelLarge, color = colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.height(padding.xs))
            Row {
                Text(
                    text = bookmark.showTitleOrElse(bookmark.url.substringAfter("://")),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = if (selected) 5 else 2,
                    style = typography.titleLarge,
                    color = colorScheme.primary,
                    modifier = Modifier
                        .clickable { context.openUri(bookmark.url.toUri()) }
                        .animateContentSize())
            }
            if (!bookmark.description.isNullOrBlank() || !bookmark.website_description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(padding.sm))
                Row {
                    Text(
                        text = bookmark.showDescriptionOrElse(""),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = if (selected) 10 else 2,
                        style = typography.bodyMedium,
                        color = colorScheme.outline,
                        modifier = Modifier.animateContentSize()
                    )
                }
            }
        }
        val tags by remember { mutableStateOf(bookmark.tags.sorted()) } // prevent re-sorting every render
        if (bookmark.tags.isNotEmpty()) {
//            Spacer(modifier = Modifier.height(padding.sm))
            LazyRow(modifier = Modifier.padding(horizontal = padding.sm)) {
                items(tags, key = { it }) {
                    Text(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onTagSelect(it) }
                            .padding(padding.sm)
                        ,
                        text = "#$it", color = colorScheme.tertiary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(padding.xs))
        }
    }
}

@ThemePreview
@Composable
fun PreviewBookmarkRow() {
    AppTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding.md)
            ) {
                BookmarkRow(
                    bookmark = Bookmark(
                        1,
                        url = "https://danielyrovas.com",
                        title = "Section 1.10.32 of 'de Finibus Bonorum et Malorum'",
                        description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?",
                        shared = false,
                        unread = true,
                        is_archived = false,
                        date_added = "2024-04-21T13:38:40.360183Z",
                        date_modified = "2024-04-21T13:38:40.360185Z",
                        tags = listOf(
                            "tag", "myself", "with", "the", "best", "tags"
                        )
                    ), onTagSelect = {}
                )
            }
        }
    }
}
