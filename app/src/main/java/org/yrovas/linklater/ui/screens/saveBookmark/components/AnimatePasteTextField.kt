package org.yrovas.linklater.ui.screens.saveBookmark.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPasteGo
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.yrovas.linklater.ui.common.Icon
import org.yrovas.linklater.ui.theme.padding

@Composable
fun AnimatePasteTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    label: String,
    leadingIcon: ImageVector,
    placeholder: String,
    showPaste: Boolean,
    onPaste: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.animateContentSize(
                    animationSpec = tween(durationMillis = 50)
                ),
            ) {
                Text(text = label, style = typography.titleMedium)
                Spacer(modifier = Modifier.height(padding.sm))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = tween(durationMillis = 150))
                ) {
                    OutlinedTextField(
                        state = state,
                        placeholder = { Text(text = placeholder) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(leadingIcon) },
                    )
                }
                Spacer(modifier = Modifier.height(padding.lg))
            }
        }
        AnimatedVisibility(
            visible = showPaste,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(100))
        ) {
            IconButton(
                modifier = Modifier.padding(start = padding.md), onClick = onPaste
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentPasteGo, tint = colorScheme.onBackground
                )
            }
        }
    }
}
