package org.yrovas.linklater.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun Frame(
    page: String,
    back: (() -> Unit),
    fab: (@Composable () -> Unit)? = null,
    snackState: SnackbarHostState,
    globalContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Frame(
        appBar = { AppBar(page, back) },
        fab = fab,
        snackState = snackState,
        globalContent = globalContent,
    ) {
        content()
    }
}

@Composable
fun Frame(
    appBar: @Composable () -> Unit,
    fab: (@Composable () -> Unit)? = null,
    snackState: SnackbarHostState,
    globalContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.background,
        contentColor = colorScheme.onBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // we set WindowCompat.setDecorFitsSystemWindows(window, false)
                // so we now must include the window padding values
                .padding(WindowInsets.systemBars.asPaddingValues())
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                globalContent()
                Column {
                    appBar()
                    content()
                }
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    fab?.let {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) { fab() }
                    }
                    SnackbarHost(hostState = snackState)
                }
            }
        }
    }
}
