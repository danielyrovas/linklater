package org.yrovas.linklater.ui.common

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.yrovas.linklater.readClipboard
import org.yrovas.linklater.ui.screens.ThemePreview
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding

@Composable
fun TextPreference(
    icon: ImageVector,
    name: String,
    placeholder: String = "",
    infoPreview: String = "",
    infoTitle: String = "",
    info: (@Composable () -> Unit)? = null,
    state: State<String>,
    onSave: (String) -> Unit,
    onCheck: (String) -> Boolean,
) {
    TextPreference(
        icon = { Icon(icon) },
        name = name,
        placeholder = placeholder,
        infoPreview = infoPreview,
        infoTitle = infoTitle,
        info = info,
        state = state,
        onSave = onSave,
        onCheck = onCheck,
    )
}

@Composable
private fun TextPreference(
    icon: @Composable () -> Unit = {},
    name: String,
    infoPreview: String,
    infoTitle: String,
    info: (@Composable () -> Unit)?,
    placeholder: String,
    state: State<String>,
    onSave: (String) -> Unit,
    onCheck: (String) -> Boolean,
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        Dialog(properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ), onDismissRequest = { showDialog = false }) {
            TextEditDialog(
                name,
                placeholder,
                infoPreview,
                infoTitle,
                info,
                state,
                onSave,
                onCheck
            ) {
                showDialog = false
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding.standard),
        onClick = { showDialog = true },
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                icon()
                Spacer(modifier = Modifier.width(padding.standard))
                Column(modifier = Modifier.padding(padding.half)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                    )
                    Spacer(modifier = Modifier.height(padding.half))
                    Text(
                        text = state.value,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Start,
                    )
                }
            }
            HorizontalDivider()
        }
    }
}



@Composable
private fun TextEditDialog(
    name: String,
    placeholder: String,
    infoPreview: String,
    infoTitle: String,
    info: (@Composable () -> Unit)?,
    storedValue: State<String>,
    onSave: (String) -> Unit,
    onCheck: (String) -> Boolean,
    onDismiss: () -> Unit,
) {
    val context: Context = LocalContext.current
    var currentInput by remember { mutableStateOf(TextFieldValue(storedValue.value)) }
    var isValid by remember { mutableStateOf(onCheck(storedValue.value)) }
    var showInfo by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .padding(
                    vertical = padding.standard, horizontal = padding.standard
                )
                .fillMaxWidth()
        ) {
            Text(
                name,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(padding.standard))
            OutlinedTextField(currentInput,
                placeholder = { Text(placeholder) },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .fillMaxWidth(),
                // leadingIcon = { Icon(Icons.Default.Build) },
                onValueChange = {
                    isValid = onCheck(it.text)
                    currentInput = it
                })
            Spacer(modifier = Modifier.height(padding.standard))

            if (info != null) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                            .clickable { showInfo = !showInfo }
                            .padding(padding.standard)
                    ) {
                        Crossfade(
                            label = "Info Title", targetState = showInfo
                        ) {
                            if (it) Text(text = infoTitle, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            else Text(text = infoPreview, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = if (showInfo) Icons.Default.Info else Icons.Outlined.Info
                        )
                    }
                    AnimatedVisibility(
                        modifier = Modifier.padding(
                            start = padding.standard,
                            end = padding.standard,
                            bottom = padding.standard,
                        ), visible = showInfo
                    ) {
                        info()
                    }
                }
                Spacer(modifier = Modifier.height(padding.standard))
            }

            Row {
                Spacer(modifier = Modifier.weight(1f))

                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onClick = {
                        currentInput = TextFieldValue(readClipboard(context))
                    },
                ) {
                    Icon(imageVector = Icons.Default.ContentPasteGo)
                }
                Spacer(modifier = Modifier.width(padding.standard))
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ), onClick = {
                        onSave(currentInput.text)
                        onDismiss()
                    }, enabled = isValid
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@ThemePreview
@Composable
private fun TextPreferenceURLPreview() {
    val state = remember { mutableStateOf("") }
    AppTheme {
        Surface {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding.standard)
            ) {
                TextPreference(icon = Icons.Default.Build,
                    name = "LinkDing API URL",
                    state = state,
                    onSave = {},
                    onCheck = { true })
            }
        }
    }
}

@ThemePreview
@Composable
private fun TextPreferencePreview() {
    val state = remember { mutableStateOf("APITokenstringVeryLongAndRandom") }
    AppTheme {
        Surface {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding.standard)
            ) {
                TextPreference(icon = Icons.Default.Build,
                    name = "LinkDing API Token",
                    state = state,
                    onSave = {},
                    onCheck = { true })
            }
        }
    }
}

@ThemePreview
@Composable
private fun TextEditDialogPreview() {
    val state = remember { mutableStateOf("") }
    AppTheme {
        Surface {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding.double)
            ) {
                TextEditDialog(name = "LinkDing API URL",
                    placeholder = "Enter your LinkDing instance URL...",
                    infoPreview = "include /api",
                    infoTitle = "Enter the LinkDing API URL",
                    // the protocol (https://), and the path (`/api`). e.g. `https://demo.linkding.link/api`",
                    info = {
                        Column {
                            Text("Include the protocol (https://).", color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("Include the /api path.", color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("Include the port if necessary.", color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("For example", color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .padding(padding.half)
                            ) {
                                Text("https://demo.linkding.link/api", color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text("or", color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .padding(padding.half)
                            ) {
                                Text("http://192.168.0.47:8000/api", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    },
                    storedValue = state,
                    onSave = {},
                    onCheck = { true }) {}
            }
        }
    }
}
