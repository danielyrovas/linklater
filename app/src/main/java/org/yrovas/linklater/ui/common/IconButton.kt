package org.yrovas.linklater.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun IconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconModifier: Modifier = Modifier,
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    androidx.compose.material3.IconButton(
        onClick = onClick, modifier = modifier
    ) {
        Icon(
            modifier = iconModifier,
            imageVector = icon,
            contentDescription = contentDescription,
        )
    }
}

@Composable
fun IconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tint: Color,
    iconModifier: Modifier = Modifier,
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    androidx.compose.material3.IconButton(
        onClick = onClick, modifier = modifier
    ) {
        Icon(
            modifier = iconModifier,
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}
