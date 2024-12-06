package org.yrovas.linklater.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.yrovas.linklater.ThemePreview
import org.yrovas.linklater.ui.theme.AppTheme

@Composable
fun TagChip(
    modifier: Modifier = Modifier,
    tag: String,
    selected: Boolean = false,
    onClick: (() -> Unit)) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = colorScheme.tertiary,
            selectedLabelColor = colorScheme.onTertiary,
            selectedContainerColor = colorScheme.tertiary
        ),
        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = true),
        label = { Text(text = "#$tag") })
}

@ThemePreview
@Composable
fun TagChipPreview() {
    var selected by remember { mutableStateOf(true) }
    AppTheme {
        Surface(Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TagChip(tag = "tag", selected = selected) {
                    selected = !selected
                }

                TagChip(tag = "tag", selected = !selected) {
                    selected = !selected
                }
            }
        }
    }
}
