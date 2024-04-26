package org.yrovas.linklater.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun Icon(
    painter: Painter,
    tint: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    androidx.compose.material3.Icon(
        modifier = modifier,
        painter = painter,
        tint = tint, contentDescription = contentDescription
    )
}

@Composable
fun Icon(
    painter: Painter,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    androidx.compose.material3.Icon(
        modifier = modifier,
        painter = painter,
        contentDescription = contentDescription
    )
}

@Composable
fun Icon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    androidx.compose.material3.Icon(
        modifier = modifier,
        imageVector = imageVector,
        contentDescription = contentDescription
    )
}

@Composable
fun Icon(
    imageVector: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    androidx.compose.material3.Icon(
        modifier = modifier,
        imageVector = imageVector,
        tint = tint, contentDescription = contentDescription
    )
}
