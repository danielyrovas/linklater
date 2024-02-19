package org.yrovas.linklater.ui.screens

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
                    infoPreview = "include /api",
                    infoTitle = "Enter the LinkDing API URL",
                    info = {
                        Column {
                            Text(
                                "Include the protocol (https://).",
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "Include the /api path.",
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "Include the port if necessary.",
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "For example",
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .padding(padding.half)
                            ) {
                                Text(
                                    "https://demo.linkding.link/api",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                "or",
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .padding(padding.half)
                            ) {
                                Text(
                                    "http://192.168.0.47:8000/api",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
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
                    placeholder = "Enter your REST API Token",
                    icon = Icons.Default.Build,
                    infoPreview = "Settings > Integrations",
                    infoTitle = "Go to your Instance Settings",
                    info = {
                        Column {
                            Text(
                                "Select Integrations",
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "Copy the token under REST API.",
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    },
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

@ThemePreview
@Composable
fun PreferencesScreenPreview() {
    AppTheme {
        PreferencesScreen(EmptyDestinationsNavigator, MainActivityState())
    }
}
