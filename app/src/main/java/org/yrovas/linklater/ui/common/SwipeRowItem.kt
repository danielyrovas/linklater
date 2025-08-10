package org.yrovas.linklater.ui.common

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import org.yrovas.linklater.ThemePreview
import org.yrovas.linklater.data.models.Bookmark
import org.yrovas.linklater.ui.screens.home.components.BookmarkRow
import kotlin.math.roundToInt

@Composable
private fun SwipeRow(
    isRevealed: Boolean,
    modifier: Modifier = Modifier,
    start: @Composable (BoxScope.(Modifier) -> Unit),
    end: @Composable (BoxScope.(Modifier) -> Unit),
    onRevealed: () -> Unit,
    onCollapsed: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    var startActionWidth by remember { mutableFloatStateOf(120f) }
    var endActionWidth by remember { mutableFloatStateOf(120f) }

    val state = remember(startActionWidth, endActionWidth) {
        AnchoredDraggableState(
            initialValue = DragAnchors.Center,
            anchors = DraggableAnchors {
                DragAnchors.Start at -startActionWidth
                DragAnchors.Center at 0f
                DragAnchors.End at endActionWidth
            },
        )
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        start(
            Modifier
                .onSizeChanged { startActionWidth = it.width.toFloat() }
                .align(Alignment.CenterStart))
        end(
            Modifier
                .onSizeChanged { endActionWidth = it.width.toFloat() }
                .align(Alignment.CenterEnd))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart)
                .offset {
                    IntOffset(
                        x = -state.requireOffset().roundToInt(),
                        y = 0,
                    )
                }
                .anchoredDraggable(
                    state = state, orientation = Orientation.Horizontal, reverseDirection = true
                ),
        ) {
            Surface {
                content()
            }
        }
    }

    // TODO: should trigger `onTouched` or similar so other list items can react
    LaunchedEffect(state.settledValue) {
        when (state.settledValue) {
            DragAnchors.Center -> if (isRevealed) onCollapsed()
            DragAnchors.Start, DragAnchors.End -> if (!isRevealed) onRevealed()
        }
    }

    LaunchedEffect(isRevealed) {
        if (!isRevealed) {
            state.animateTo(DragAnchors.Center)
        }
    }
}

@Composable
fun SlideSwipeRowItem(
    isRevealed: Boolean,
    modifier: Modifier = Modifier,
    startActions: List<@Composable (RowScope.() -> Unit)> = emptyList(),
    endActions: List<@Composable (RowScope.() -> Unit)> = emptyList(),
    onRevealed: () -> Unit = {},
    onCollapsed: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    SwipeRow(
        isRevealed = isRevealed, start = { mod ->
            Row(modifier = mod.then(modifier)) {
                startActions.forEach { it() }
            }
        }, end = { mod ->
            Row(modifier = mod.then(modifier)) {
                endActions.forEach { it() }
            }
        }, onRevealed = onRevealed, onCollapsed = onCollapsed, content = content
    )
}

@Composable
fun VerticalSlideSwipeRowItem(
    isRevealed: Boolean,
    modifier: Modifier = Modifier,
    startActions: List<@Composable (ColumnScope.() -> Unit)> = emptyList(),
    endActions: List<@Composable (ColumnScope.() -> Unit)> = emptyList(),
    onRevealed: () -> Unit = {},
    onCollapsed: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    SwipeRow(
        isRevealed = isRevealed, start = { mod ->
            Column(modifier = mod.then(modifier)) {
                startActions.forEach { it() }
            }
        }, end = { mod ->
            Column(modifier = mod.then(modifier)) {
                endActions.forEach { it() }
            }
        }, onRevealed = onRevealed, onCollapsed = onCollapsed, content = content
    )
}

@ThemePreview
@Composable
fun SlideSwipeBoxPreview() {
    Preview {
        SlideSwipeRowItem(
            isRevealed = true, startActions = listOf(
                {
                    IconButton(icon = Icons.Default.Edit, onClick = {})
                },
            ), content = { BookmarkPreview() })
    }
}

@ThemePreview
@Composable
fun VerticalSlideSwipeBoxPreview() {
    Preview {
        VerticalSlideSwipeRowItem(
            isRevealed = true, startActions = listOf(
                {
                    IconButton(icon = Icons.Default.Edit, onClick = {})
                },
                {
                    IconButton(icon = Icons.Default.Bookmark, onClick = {})
                },
                {
                    IconButton(icon = Icons.Default.Archive, onClick = {})
                },
                {
                    IconButton(icon = Icons.Default.Delete, onClick = {})
                },
            ), content = { BookmarkPreview() })
    }
}

@Composable
private fun BookmarkPreview() = BookmarkRow(
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
    ), onTagSelect = {})
