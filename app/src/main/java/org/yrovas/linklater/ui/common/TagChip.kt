package org.yrovas.linklater.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.yrovas.linklater.Preview
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding

@Composable
fun TagChip(
    modifier: Modifier = Modifier, tag: String, onClick: (() -> Unit)
) {
    Text(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(padding.sm),
        text = "#$tag",
        style = typography.titleMedium,
        color = colorScheme.tertiary,
    )
}

@Composable
fun SelectedTagChip(
    modifier: Modifier = Modifier, tag: String, onClick: (() -> Unit)
) {
    Text(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(colorScheme.tertiary)
            .padding(padding.sm),
        text = "#$tag",
        style = typography.titleMedium,
        color = colorScheme.onTertiary,
    )
}

@Preview
@Composable
fun TagChipPreview() {
    AppTheme {
        Surface(Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TagChip(tag = "tag") {}
                SelectedTagChip(tag = "tag") { }
            }
        }
    }
}
