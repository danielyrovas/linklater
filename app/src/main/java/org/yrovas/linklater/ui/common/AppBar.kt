package org.yrovas.linklater.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import org.yrovas.linklater.ui.screens.ThemePreview
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding

@Composable
fun AppBar(
    page: String,
    nav: DestinationsNavigator,
    back: (() -> Unit)? = { nav.popBackStack() },
    content: @Composable () -> Unit = {},
) {
    AppBar(left = {
        Spacer(modifier = Modifier.width(padding.standard))
        back?.let {
            IconButton(onClick = { back() }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(text = page, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }) {
        content()
    }
}

@Composable
fun AppBar(left: @Composable () -> Unit, right: @Composable () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.background),
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
}

@ThemePreview
@Composable
fun AppBarPreview () {
    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppBar(page = "Settings", nav = EmptyDestinationsNavigator)
        }
    }
}
