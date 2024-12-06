package org.yrovas.linklater.ui.screens.saveBookmark.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import org.yrovas.linklater.ui.common.Icon
import org.yrovas.linklater.ui.theme.padding

@Composable
fun TitledTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    label: String,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    onFocusChanged: (Boolean) -> Unit = {}
) {
    Column(modifier = modifier) {
        Text(text = label, style = typography.titleMedium)
        OutlinedTextField(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFocusChanged(it.isFocused) }
                .padding(top = padding.sm),
            leadingIcon = if (leadingIcon != null) { -> Icon(leadingIcon) } else null,
            placeholder = if (placeholder != null) { -> Text(placeholder) } else null,
        )
    }
}
