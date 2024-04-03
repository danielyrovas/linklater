package org.yrovas.linklater.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        Spacer(modifier = Modifier.width(padding.standard))
        back?.let {
            IconButton(onClick = { back() }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = colorScheme.primary
                )
            }
        }
        Text(
            text = page,
            style = typography.titleLarge,
            color = colorScheme.primary
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            right()
        }
    }
}

@Composable
fun LargeHomeBar() {
}

@ThemePreview
@Composable
fun AppBarPreview () {
    AppTheme {
        Surface {
            AppBar(page = "Settings", back = {})
        }
    }
}

@ThemePreview
@Composable
fun LargeHomeBarPreview() {
    AppTheme {
        Surface {
            LargeHomeBar()
        }
    }
}
