package org.yrovas.linklater.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import org.yrovas.linklater.ui.screens.ThemePreview
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding

@Composable
fun TextPreference(
    icon: ImageVector,
    name: String,
    placeholder: String = "",
    secure: Boolean = true,
    state: State<String>,
    onSave: (String) -> Unit,
    onCheck: (String) -> Boolean,
) {
    TextPreference(
        icon = { Icon(icon) },
        name = name,
        placeholder = placeholder,
        secure = secure,
        state = state,
        onSave = onSave,
        onCheck = onCheck,
    )
}

@Composable
private fun TextPreference(
    icon: @Composable () -> Unit = {},
    name: String,
    placeholder: String,
    secure: Boolean,
    state: State<String>,
    onSave: (String) -> Unit,
    onCheck: (String) -> Boolean,
) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        Dialog(properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            securePolicy = if (secure) SecureFlagPolicy.SecureOn else SecureFlagPolicy.SecureOff,
        ), onDismissRequest = { showDialog = false }) {
            TextEditDialog(name, placeholder, state, onSave, onCheck) {
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
                horizontalArrangement = Arrangement.Start
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
    storedValue: State<String>,
    onSave: (String) -> Unit,
    onCheck: (String) -> Boolean,
    onDismiss: () -> Unit,
) {
    var currentInput by remember { mutableStateOf(TextFieldValue(storedValue.value)) }
    var isValid by remember { mutableStateOf(onCheck(storedValue.value)) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .padding(vertical = padding.standard, horizontal = padding.standard)
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
            Row {
                Spacer(modifier = Modifier.weight(1f))
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
private fun TextPreferencePreview() {
    AppTheme {
        Surface {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding.standard)
            ) {
                TextPreference(icon = Icons.Default.Build,
                    name = "LinkDing API Token",
                    state = derivedStateOf { "APITokenstringVeryLongAndRandom" },
                    onSave = {},
                    onCheck = { true })
            }
        }
    }
}

@ThemePreview
@Composable
private fun TextEditDialogPreview() {
    AppTheme {
        Surface {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding.double)
            ) {
                TextEditDialog(name = "LinkDing API Token",
                    placeholder = "Enter your API token...",
                    storedValue = derivedStateOf { "" },
                    onSave = {},
                    onCheck = { true }) {}
            }
        }
    }
}
