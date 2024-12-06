package org.yrovas.linklater.ui.screens.saveBookmark.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.flow.StateFlow
import org.yrovas.linklater.ui.common.Icon
import org.yrovas.linklater.ui.common.TagChip
import kotlin.math.max
import kotlin.math.round

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelectRow(
    tags: StateFlow<List<String>>,
    selectedTags: StateFlow<List<String>>,
    onTagSelect: (String) -> Unit,
) {
    var collapseTags by remember { mutableStateOf(true) }
    val tags by tags.collectAsState()
    val selectedTags by selectedTags.collectAsState()
    val unselectedTags by remember(tags, selectedTags) {
        mutableStateOf(tags - selectedTags.toSet())
    }
    val rotation: Float by animateFloatAsState(
        targetValue = if (collapseTags) 0f else 180f,
        label = "Trailing Icon Rotation",
    )

    val rowSize by remember(unselectedTags) {
        mutableIntStateOf(
            max(round(unselectedTags.size / 3f).toInt() + 1, 8)
        )
    }
    if (unselectedTags.isNotEmpty()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Add tags...", style = typography.titleMedium)
            if (unselectedTags.size > 8) {
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
            if (collapseTags && unselectedTags.size >= 7) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .horizontalScroll(rememberScrollState())
                ) {
                    FlowRow(
                        maxItemsInEachRow = rowSize
                    ) {
                        unselectedTags.forEach {
                            TagChip(tag = it, onClick = { onTagSelect(it) })
                        }
                    }
                }
            } else {
                FlowRow {
                    unselectedTags.forEach {
                        TagChip(tag = it, onClick = { onTagSelect(it) })
                    }
                }
            }
        }
    }

}

