package org.yrovas.linklater.ui.screens

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.yrovas.linklater.*
import org.yrovas.linklater.ui.common.*
import org.yrovas.linklater.ui.state.PreferencesScreenState
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding

@Destination<RootGraph>
@Composable
fun PreferencesScreen(
    nav: DestinationsNavigator,
    snackState: SnackbarHostState,
    appViewModel: AppViewModel,
    state: PreferencesScreenState = PreferencesScreenState(appViewModel),
    context: Context = LocalContext.current,
) {
    Frame(appBar = {
        AppBar(page = "Preferences", back = { nav.popBackStack() })
    }, snackState = snackState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding.standard)
                .verticalScroll(rememberScrollState())
        ) {
            TextPreference(
                name = "LinkDing URL",
                placeholder = "URL/IP incl. port and https://",
                icon = Icons.Default.Create,
                infoPreview = "include /api",
                infoTitle = "Enter the LinkDing API URL",
                info = {
                    Column() {
                        Text(
                            "Include the protocol (https://).",
                            color = colorScheme.onSecondaryContainer
                        )
                        Text(
                            "Include the /api path.",
                            color = colorScheme.onSecondaryContainer
                        )
                        Text(
                            "Include the port if necessary.",
                            color = colorScheme.onSecondaryContainer
                        )
                        Text(
                            "For example",
                            color = colorScheme.onSecondaryContainer
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(colorScheme.surfaceContainer)
                                .padding(padding.half)
                        ) {
                            Text(
                                "https://demo.linkding.link/api",
                                color = colorScheme.onSurface
                            )
                        }
                        Text(
                            "or", color = colorScheme.onSecondaryContainer
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(colorScheme.surfaceContainer)
                                .padding(padding.half)
                        ) {
                            Text(
                                "http://192.168.0.47:8000/api",
                                color = colorScheme.onSurface
                            )
                        }
                    }
                },
                state = state.bookmarkURL.collectAsState(),
                onSave = { state.saveBookmarkURL(it) },
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
                            color = colorScheme.onSecondaryContainer
                        )
                        Text(
                            "Copy the token under REST API.",
                            color = colorScheme.onSecondaryContainer
                        )
                    }
                },
                state = state.bookmarkAPIToken.collectAsState(),
                onSave = { state.saveBookmarkAPIToken(it) },
                onCheck = { checkBookmarkAPIToken(it) },
            )
            Spacer(modifier = Modifier.weight(1F))
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "Application Version: ${context.getAppVersion()}",
                style = typography.bodySmall,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@ThemePreview
@Composable
fun PreferencesScreenPreview() {
    AppTheme {
//        PreferencesScreen(
//            EmptyDestinationsNavigator, SnackbarHostState(), PreviewAppViewModel()
//        )
    }
}
