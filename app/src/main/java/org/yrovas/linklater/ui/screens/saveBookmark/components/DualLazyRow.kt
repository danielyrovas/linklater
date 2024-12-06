package org.yrovas.linklater.ui.screens.saveBookmark.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.yrovas.linklater.ThemePreview
import org.yrovas.linklater.ui.common.Frame
import org.yrovas.linklater.ui.common.TagChip
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding

@Composable
fun <T> DualLazyRow(
    modifier: Modifier = Modifier,
    items: List<T>,
    rowModifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal =
        if (!reverseLayout) Arrangement.Start else Arrangement.End,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    key: ((T) -> Any)? = null,
    breakPointMin: Int = 4,
    breakPointMax: Int = 5,
    content: @Composable LazyItemScope.(T) -> Unit
) {
    val items1 by remember(items, breakPointMax, breakPointMin) {
        derivedStateOf {
            if (items.size + 1 >= breakPointMax * 2) {
                items.take(breakPointMax)
            } else {
                items.take(breakPointMin)
            }
        }
    }
    val items2 by remember(items, breakPointMax, breakPointMin) {
        derivedStateOf {
            if (items.size + 1 >= breakPointMax * 2) {
                items.drop(breakPointMax)
            } else {
                items.drop(breakPointMin)
            }
        }
    }

    Column(modifier = modifier) {
        LazyRow(
            modifier = rowModifier,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment
        ) {
            if (key != null) {
                items(items1, key = key) {
                    content(it)
                }
            } else {
                items(items1) {
                    content(it)
                }
            }
        }
        AnimatedVisibility(visible = items2.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(durationMillis = 150)),
            exit = fadeOut(animationSpec = tween(durationMillis = 150)),
        ) {
            LazyRow(
                modifier = rowModifier,
                contentPadding = contentPadding,
                reverseLayout = reverseLayout,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment
            ) {
                if (key != null) {
                    items(items2, key = key) {
                        content(it)
                    }
                } else {
                    items(items2) {
                        content(it)
                    }
                }
            }
        }
    }
}

@ThemePreview
@Composable
fun PreviewDualLazyRow() {
    val items = remember { mutableStateListOf("tags", "400", "43093", "and", "nine") }
    AppTheme {
        Frame(title = "Preview") {
            Column {
                DualLazyRow(
                    modifier = Modifier.padding(padding.md),
                    items = items
                ) { tag ->
                    TagChip(
                        modifier = Modifier.animateItem(),
                        tag = tag,
                        selected = true,
                        onClick = {
                            items.remove(tag)
                        })
                }
                Button(onClick = {
                    items.add("new tag")
                }) {
                    Text("Add tag")
                }
            }
        }
    }
}
