package org.yrovas.linklater.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.yrovas.linklater.ui.theme.AppTheme

@Composable
fun Preview(
    modifier: Modifier = Modifier,
    title: String = "Preview",
    content: @Composable ColumnScope.() -> Unit
) {
    AppTheme {
        Surface {
            Frame(title = title) {
                Column(modifier = modifier) {
                    content()
                }
            }
        }
    }
}
