package org.yrovas.linklater.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.yrovas.linklater.Preview
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding

@Composable
fun TagPredictRow(predictedTags: StateFlow<List<String>>, onClick: (String) -> Unit) {
    val tagPredictions by predictedTags.collectAsState()
    Column {
        HorizontalDivider(color = colorScheme.outline)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .zIndex(4f)
                .background(colorScheme.background),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            itemsIndexed(tagPredictions) { index, tag ->
                TagChip(
                    modifier = Modifier.animateItem(), tag = tag
                ) { onClick(tag) }
                if (index < tagPredictions.lastIndex) {
                    VerticalDivider(
                        modifier = Modifier
                            .height(18.dp)
                            .padding(horizontal = padding.md),
                        color = colorScheme.outline
                    )
                }
            }
        }
        HorizontalDivider(color = colorScheme.outline)
    }
}

@Preview
@Composable
fun TagPredictRowPreview() {
    val predictedTags: StateFlow<List<String>> = MutableStateFlow(listOf("tag1", "tag2", "tag3"))
    AppTheme {
        Surface {
            TagPredictRow(predictedTags = predictedTags, onClick = {})
        }
    }
}
