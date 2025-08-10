package org.yrovas.linklater.ui.screens.saveBookmark.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import org.yrovas.linklater.Preview
import org.yrovas.linklater.ui.common.Frame
import org.yrovas.linklater.ui.common.Icon
import org.yrovas.linklater.ui.common.TagChip
import org.yrovas.linklater.ui.theme.AppTheme
import kotlin.math.max

@Composable
fun ExpandableTagRow(
    tags: List<String>,
    title: String,
    onTagSelect: (String) -> Unit,
) {
    var collapseTags by remember { mutableStateOf(true) }
    val rows = remember(tags.size, collapseTags) {
        if (collapseTags) 2 else max((tags.size / 4), 1)
    }
    val rotation: Float by animateFloatAsState(
        targetValue = if (collapseTags) 0f else 180f,
        label = "Trailing Icon Rotation",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = typography.titleMedium)
        if (tags.size > 10) {
            IconButton(onClick = { collapseTags = !collapseTags }) {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    modifier = Modifier.graphicsLayer { this.rotationZ = rotation },
                )
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.height(rows * 35.dp)) {
            LazyHorizontalStaggeredGrid(
                modifier = Modifier.fillMaxWidth(),
                rows = StaggeredGridCells.Fixed(rows),
            ) {
                items(tags, key = { it }) { tag ->
                    TagChip(
                        modifier = Modifier.animateItem(
                            fadeInSpec = null, fadeOutSpec = null
                        ), tag = tag, onClick = { onTagSelect(tag) })
                }
            }
        }
    }
}

@Preview
@Composable
fun ExpandableTagRowPreview() {
    val tags = remember {
        mutableStateListOf(
            "Tag1",
            "Tag2",
            "Tag3",
            "Tag4",
            "Tag5",
            "Tag6",
            "Tag7",
            "Tag8",
            "Tag9",
            "Tag10",
            "Tag11",
            "Tag12",
        )
    }
    val tagIndex = remember { mutableIntStateOf(12) }
    AppTheme {
        Frame(title = "Expandable Tags") {
            Column {
                ExpandableTagRow(
                    tags = tags, title = "Tags", onTagSelect = {
                        tags.remove(it)
                    })
                Spacer(modifier = Modifier.height(48.dp))
                Button(onClick = {
                    tagIndex.intValue += 1
                    tags.add("Tag${tagIndex.intValue}")
                }) {
                    Text("Add Tag")
                }
            }
        }
    }
}
