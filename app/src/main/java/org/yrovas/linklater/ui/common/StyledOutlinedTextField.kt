package org.yrovas.linklater.ui.common

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.yrovas.linklater.ThemePreview
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding

@Composable
fun StyledOutlinedTextField(
    name: String,
    value: String,
    icon: ImageVector? = null,
    placeholder: String? = null,
    onFocusChanged: ((Boolean) -> Unit)? = null,
    onChange: (String) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    if (onFocusChanged != null) {
        LaunchedEffect(isFocused) {
            onFocusChanged(isFocused)
        }
    }


    Column(
        modifier = Modifier.animateContentSize(
            animationSpec = tween(durationMillis = 50)
        )
    ) {
        Text(text = name, style = typography.titleMedium)
        Spacer(modifier = Modifier.height(padding.half))
        OutlinedTextField(value,
            placeholder = { if (!placeholder.isNullOrBlank()) Text(placeholder) },
            leadingIcon = { if (icon != null) Icon(icon) },
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { onChange(it) },
            interactionSource = interactionSource
        )
        Spacer(modifier = Modifier.height(padding.double))
    }
}

@Composable
fun StyledOutlinedTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    icon: ImageVector? = null,
    placeholder: String? = null,
    label: String? = null,
    onFocusChanged: ((Boolean) -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    if (onFocusChanged != null) {
        LaunchedEffect(isFocused) {
            onFocusChanged(isFocused)
        }
    }

    OutlinedTextField(
        modifier = modifier,
        state = state,
        leadingIcon = if (icon != null) { -> Icon(icon) } else null,
        placeholder = if (!placeholder.isNullOrBlank()) { -> Text(placeholder) } else null,
        label = if (!label.isNullOrBlank()) { -> Text(label) } else null,
        interactionSource = interactionSource,
    )
}

@ThemePreview
@Composable
private fun StyledOutlinedTextFieldPreview() {
    AppTheme {
        Surface {
            Box(
                modifier = Modifier
                    .height(75.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                StyledOutlinedTextField(
                    state = rememberTextFieldState(),
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(.9f)
                    , label = "Tags",
                    icon = Icons.Default.Tag,
                    placeholder = "Enter tags..."
                )
            }
        }
    }
}

