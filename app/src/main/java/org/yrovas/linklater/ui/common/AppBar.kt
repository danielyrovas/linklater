package org.yrovas.linklater.ui.common

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.yrovas.linklater.ThemePreview
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding

@Composable
fun AppBar(
    page: String,
    back: (() -> Unit)? = null, // { nav.popBackStack() },
    content: @Composable () -> Unit = {},
) {
    AppBar(left = {
        if (back != null) {
            IconButton(onClick = { back() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, tint = colorScheme.primary
                )
            }
        } else {
            Spacer(modifier = Modifier.width(padding.standard))
        }
        Text(
            text = page, style = typography.titleLarge, color = colorScheme.primary
        )
    }, right = {
        content()
    })
}

@Composable
fun AppBar(
    left: @Composable () -> Unit,
    right: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(colorScheme.background),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            left()
        }
        Row(
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End
        ) {
            right()
        }
    }
}

@ThemePreview
@Composable
fun HomeAppBarPreview() {
    AppTheme {
        Surface {
            AppBar(page = "Home")
        }
    }
}

@ThemePreview
@Composable
fun AppBarPreview() {
    AppTheme {
        Surface {
            AppBar(page = "Settings", back = {})
        }
    }
}

//@ThemePreview
//@Composable
//fun LargeHomeBarPreview() {
//    AppTheme {
//        Surface {
//            LargeHomeBar()
//        }
//    }
//}
