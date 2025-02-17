package org.yrovas.linklater.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.yrovas.linklater.ThemePreview
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding

@Composable
fun StyledTextField(
    modifier: Modifier = Modifier,
    boxModifier: Modifier = Modifier,
    state: TextFieldState,
    textStyle: TextStyle = typography.bodyLarge,
    color: Color = colorScheme.primary,
    border: Color = color,
    borderFocus: Color = color,
    background: Color = colorScheme.background,
    cornerRadius: Dp = 5.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    BasicTextField(state = state,
        modifier = modifier,
        interactionSource = interactionSource,
        lineLimits = TextFieldLineLimits.SingleLine,
        textStyle = textStyle.copy(color = color),
        cursorBrush = SolidColor(color),
        decorator = { field ->
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = boxModifier
                    .border(
                        width = if (isFocused) 1.5.dp else 1.dp,
                        color = if (isFocused) borderFocus else border,
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(background)
            ) {
                Spacer(Modifier.width(padding.standard))
                field()
                Spacer(Modifier.width(padding.standard))
            }
        })
}

@ThemePreview
@Composable
private fun StyledTextFieldPreview() {
    val state = rememberTextFieldState("Hello World!")
    AppTheme {
        Surface {
            Box(
                modifier = Modifier
                    .height(75.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                StyledTextField(
                    state = state, modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(.9f)
                )
            }
        }
    }
}

