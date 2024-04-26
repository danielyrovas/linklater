package org.yrovas.linklater.ui.common

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentPasteGo
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.yrovas.linklater.ThemePreview
import org.yrovas.linklater.readClipboard
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
    onCheck: (String) -> Boolean = { true },
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding.standard)
            .clip(RoundedCornerShape(8.dp))
            .clickable { showDialog = true }
        ,
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
                        style = typography.bodyMedium,
                        textAlign = TextAlign.Start,
                    )
                    Spacer(modifier = Modifier.height(padding.half))
                    Text(
                        text = state.value,
                        style = typography.bodySmall,
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
        color = colorScheme.surfaceContainer,
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
                color = colorScheme.onSurface,
                style = typography.titleLarge
            )
            Spacer(modifier = Modifier.height(padding.standard))
            OutlinedTextField(currentInput,
                placeholder = { Text(placeholder) },
                modifier = Modifier
                    .background(colorScheme.surfaceVariant)
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
                        .background(colorScheme.secondaryContainer)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showInfo = !showInfo }
                            .padding(padding.standard)
                    ) {
                        Crossfade(
                            label = "Info Title", targetState = showInfo
                        ) {
                            if (it) Text(text = infoTitle, color = colorScheme.onSecondaryContainer)
                            else Text(text = infoPreview, color = colorScheme.onSecondaryContainer)
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
                        containerColor = colorScheme.surfaceVariant,
                        contentColor = colorScheme.onSurfaceVariant
                    ),
                    onClick = {
                        currentInput = TextFieldValue(context.readClipboard())
                    },
                ) {
                    Icon(imageVector = Icons.Default.ContentPasteGo)
                }
                Spacer(modifier = Modifier.width(padding.standard))
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer
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
                    .background(colorScheme.background)
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
                    .background(colorScheme.background)
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
                    .background(colorScheme.background)
                    .padding(padding.double)
            ) {
                TextEditDialog(name = "LinkDing API URL",
                    placeholder = "Enter your LinkDing instance URL...",
                    infoPreview = "include /api",
                    infoTitle = "Enter the LinkDing API URL",
                    info = {
                        Column {
                            Text("Include the protocol (https://).", color = colorScheme.onSecondaryContainer)
                            Text("Include the /api path.", color = colorScheme.onSecondaryContainer)
                            Text("Include the port if necessary.", color = colorScheme.onSecondaryContainer)
                            Text("For example", color = colorScheme.onSecondaryContainer)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colorScheme.surfaceContainer)
                                    .padding(padding.half)
                            ) {
                                Text("https://demo.linkding.link/api", color = colorScheme.onSurface)
                            }
                            Text("or", color = colorScheme.onSecondaryContainer)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colorScheme.surfaceContainer)
                                    .padding(padding.half)
                            ) {
                                Text("http://192.168.0.47:8000/api", color = colorScheme.onSurface)
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
