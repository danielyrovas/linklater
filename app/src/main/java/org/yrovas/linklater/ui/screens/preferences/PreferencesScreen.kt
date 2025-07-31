package org.yrovas.linklater.ui.screens.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.BuildConfig
import org.yrovas.linklater.checkBookmarkAPIToken
import org.yrovas.linklater.checkURL
import org.yrovas.linklater.getAppVersion
import org.yrovas.linklater.openUri
import org.yrovas.linklater.ui.common.Frame
import org.yrovas.linklater.ui.common.Icon
import org.yrovas.linklater.ui.common.TextPreference
import org.yrovas.linklater.ui.screens.Destination
import org.yrovas.linklater.ui.screens.LocalBackStack
import org.yrovas.linklater.ui.screens.preferences.PreferencesModel.Event
import org.yrovas.linklater.ui.theme.padding

typealias PreferencesScreen = @Composable () -> Unit

@Inject
@Composable
fun PreferencesScreen(
    preferencesModel: () -> PreferencesModel,
) {
    val context = LocalContext.current
    val backStack = LocalBackStack.current
    val state = viewModel { preferencesModel() }

    val defaultBookmark by state.defaultBookmark.collectAsState()

    Frame(
        title = "Preferences",
        back = { backStack.removeLastOrNull() },
        actions = {
            if (BuildConfig.DEBUG) {
                clickableItem(
                    onClick = {
                        backStack.add(Destination.Logs)
                    },
                    icon = {
                        Icon(imageVector = Icons.Filled.BugReport)
                    },
                    label = "Settings",
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding.md)
                .verticalScroll(rememberScrollState())
        ) {
            StyledTitle(title = "LinkDing Account")
            TextPreference(
                name = "LinkDing API Endpoint",
                placeholder = "URL/IP incl. port and https://",
                icon = Icons.Default.Bookmark,
                infoPreview = "Include /api",
                infoTitle = "Enter the LinkDing API URL",
                info = {
                    Column {
                        Text(
                            "Include the protocol (https://).",
                            color = colorScheme.onSecondaryContainer
                        )
                        Text(
                            "Include the /api path.", color = colorScheme.onSecondaryContainer
                        )
                        Text(
                            "Include the port if necessary.",
                            color = colorScheme.onSecondaryContainer
                        )
                        Text(
                            "For example", color = colorScheme.onSecondaryContainer
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(colorScheme.surfaceContainer)
                                .padding(padding.sm)
                        ) {
                            Text(
                                "https://demo.linkding.link/api", color = colorScheme.onSurface
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
                                .padding(padding.sm)
                        ) {
                            Text(
                                "http://192.168.0.47:8000/api", color = colorScheme.onSurface
                            )
                        }
                    }
                },
                state = state.bookmarkEndpoint.collectAsState(),
                onSave = {
                    state.sendEvent(Event.SaveEndpoint(url = it))
                },
                onCheck = { checkURL(it) },
            )
            TextPreference(
                name = "LinkDing API Token",
                placeholder = "Enter your REST API Token",
                icon = Icons.Default.Key,
                infoPreview = "Settings > Integrations",
                infoTitle = "Go to your Instance Settings",
                info = {
                    Column {
                        Text(
                            "Select Integrations", color = colorScheme.onSecondaryContainer
                        )
                        Text(
                            "Copy the token under REST API.",
                            color = colorScheme.onSecondaryContainer
                        )
                    }
                },
                state = state.bookmarkAPIToken.collectAsState(),
                onSave = { state.sendEvent(Event.SaveToken(it)) },
                onCheck = { checkBookmarkAPIToken(it) },
            )
            Spacer(modifier = Modifier.height(padding.lg))
            StyledTitle(title = "Bookmark Defaults")
            TextPreference(
                icon = Icons.Default.Tag,
                name = "Tags",
                state = state.tag_names.collectAsState(),
                onSave = {
                    state.sendEvent(Event.SaveDefaults(tag_names = it))
                },
            )
            StyledCheckPreference(
                name = "Unread",
                icon = Icons.Default.Visibility,
                checked = defaultBookmark.unread,
                onCheckedChange = {
                    state.sendEvent(Event.SaveDefaults(unread = it))
                })
            StyledCheckPreference(
                name = "Shared",
                icon = Icons.Default.Public,
                checked = defaultBookmark.shared,
                onCheckedChange = {
                    state.sendEvent(Event.SaveDefaults(shared = it))
                })

            Spacer(modifier = Modifier.weight(1F))
            Spacer(modifier = Modifier.height(padding.lg))
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        context.openUri("https://github.com/danielyrovas/linklater/releases".toUri())
                    },
                text = "Application Version: ${context.getAppVersion()}",
                style = typography.bodySmall,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun StyledTitle(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = padding.lg, horizontal = padding.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = typography.titleMedium, color = colorScheme.primary)
        Spacer(modifier = Modifier.height(padding.sm))
        HorizontalDivider(color = colorScheme.primary)
    }
}

@Composable
private fun StyledCheckPreference(
    name: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Spacer(modifier = Modifier.width(padding.md))
        Icon(imageVector = icon)
        Spacer(modifier = Modifier.width(padding.md))
        Spacer(modifier = Modifier.width(padding.sm))
        Text(
            text = name,
            style = typography.bodyMedium,
        )
        Spacer(modifier = Modifier.weight(1f))
        Checkbox(checked = checked, onCheckedChange = { onCheckedChange(it) })
        Spacer(modifier = Modifier.width(padding.sm))
    }
}
