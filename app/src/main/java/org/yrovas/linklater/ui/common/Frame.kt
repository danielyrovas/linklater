package org.yrovas.linklater.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.AppBarRowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Frame(
    title: String,
    back: (() -> Unit)? = null,
    actions: (AppBarRowScope.() -> Unit) = { },
    fab: (@Composable () -> Unit)? = null,
    globalContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Frame(
        topBar = {
            TopAppBar(title = { Text(title) }, navigationIcon = {
                if (back != null) {
                    IconButton(onClick = back) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack)
                    }
                }
            }, actions = {
                AppBarRow(
                    maxItemCount = 3, overflowIndicator = {
                        IconButton(onClick = { it.show() }) {
                            Icon(imageVector = Icons.Filled.MoreVert)
                        }
                    }, content = actions
                )
            })
        }, fab = fab, globalContent = globalContent, content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Frame(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit,
    fab: (@Composable () -> Unit)? = null,
    globalContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        globalContent()
        Scaffold(
            modifier = modifier,
            floatingActionButton = {
                fab?.let {
                    fab()
                }
            },
            topBar = topBar,
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                content()
            }
        }
    }
}
