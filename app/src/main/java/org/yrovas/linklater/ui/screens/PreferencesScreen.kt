package org.yrovas.linklater.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import kotlinx.coroutines.launch
import org.yrovas.linklater.MainActivityState
import org.yrovas.linklater.checkURL
import org.yrovas.linklater.ui.common.AppBar
import org.yrovas.linklater.ui.common.TextPreference
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding

@Destination
@Composable
fun PreferencesScreen(
    nav: DestinationsNavigator,
    mainActivityState: MainActivityState,
    context: Context = LocalContext.current,
) {
    val scope = rememberCoroutineScope()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            AppBar(page = "Preferences", nav = nav)
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(padding.standard)
            ) {
                TextPreference(
                    name = "LinkDing URL",
                    placeholder = "URL/IP incl. port and https://",
                    icon = Icons.Default.Create,
                    state = mainActivityState.bookmarkURL.collectAsState(),
                    onSave = {
                        scope.launch {
                            mainActivityState.saveBookmarkURL(it, context)
                        }
                    },
                    onCheck = { checkURL(it) },
                )
                TextPreference(
                    name = "LinkDing API Token",
                    placeholder = "Enter your LinkDing API Token",
                    icon = Icons.Default.Build,
                    state = mainActivityState.bookmarkAPIToken.collectAsState(),
                    onSave = {
                        scope.launch {
                            mainActivityState.saveBookmarkAPIToken(it, context)
                        }
                    },
                    onCheck = { mainActivityState.checkBookmarkAPIToken(it) },
                )
            }
        }
    }
}

@Preview
@Composable
fun PreferencesScreenPreview() {
    AppTheme(darkTheme = true) {
        PreferencesScreen(EmptyDestinationsNavigator, MainActivityState())
    }
}

@Preview
@Composable
fun PreferencesLightScreenPreview() {
    AppTheme(darkTheme = false) {
        PreferencesScreen(EmptyDestinationsNavigator, MainActivityState())
    }
}
