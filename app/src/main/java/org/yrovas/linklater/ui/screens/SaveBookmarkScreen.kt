package org.yrovas.linklater.ui.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentPasteGo
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import kotlinx.coroutines.delay
import org.yrovas.linklater.*
import org.yrovas.linklater.destinations.SubmitBookmarkScreenDestination
import org.yrovas.linklater.ui.common.AppBar
import org.yrovas.linklater.ui.common.Icon
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding
import kotlin.math.abs
import kotlin.math.max

@NavGraph
annotation class SaveBookmarkNavGraph(
    val start: Boolean = false,
)

@SaveBookmarkNavGraph(start = true)
@Destination
@Composable
fun SaveBookmarkScreen(
    nav: DestinationsNavigator,
    saveActivityState: SaveActivityState,
    context: Context = LocalContext.current,
) {
    Surface {
        Column {
            AppBar(
                page = "Add Bookmark",
                nav = nav,
                back = { pressBack(context) }) {
                IconButton(onClick = {
                    nav.navigate(SubmitBookmarkScreenDestination)
                }) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        tint = colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(padding.half))
            }
            SaveBookmarkFields(saveActivityState = saveActivityState, submit = {
                nav.navigate(SubmitBookmarkScreenDestination)
            })
        }
    }
}

@Destination
@SaveBookmarkNavGraph
@Composable
fun SubmitBookmarkScreen(
    nav: DestinationsNavigator,
    saveActivityState: SaveActivityState,
    context: Context = LocalContext.current,
) {
    var isSubmitting by remember { mutableStateOf(false) }
    val submitResult by saveActivityState.submitResult.collectAsState()

    LaunchedEffect(Unit) {
        isSubmitting = true
//        delay(2000)
        if (saveActivityState.validateBookmark()) {
            if (saveActivityState.submitBookmark()) {
                saveActivityState.setSubmitResult("Saved Bookmark" to true)
                isSubmitting = false
                delay(500)
//                nav.clearBackStack(SaveBookmarkScreenDestination)
//                nav.clearBackStack(SubmitBookmarkScreenDestination)
//                pressBack(context)
                (context as Activity).finish()
            }
        } else {
            saveActivityState.setSubmitResult("Invalid URL" to false)
        }
        isSubmitting = false
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppBar(page = "Saving...", nav = nav, back = { pressBack(context) })
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator()
                } else {
                    if (submitResult.second) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(colorScheme.tertiary)
                                .clickable { pressBack(context) },
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.tertiaryContainer)
                                    .align(Alignment.Center),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    tint = colorScheme.tertiary,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(colorScheme.error)
                                .clickable { pressBack(context) },
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.errorContainer)
                                    .align(Alignment.Center),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    tint = colorScheme.error,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(padding.double))
                    Text(submitResult.first, style = typography.headlineSmall)
                    Spacer(modifier = Modifier.height(padding.large))
                }
            }
        }
    }
}

@ThemePreview
@Composable
fun SubmitScreenPreview() {
    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val s = SaveActivityState()
            s.setSubmitResult("Invalid URL" to false)
            SubmitBookmarkScreen(EmptyDestinationsNavigator, s)
        }
    }
}

@Composable
fun StyledTextField(
    name: String,
    value: String,
    icon: ImageVector,
    placeholder: String? = null,
    onChange: (String) -> Unit,
) {
    Column {
        Text(text = name, style = typography.titleMedium)
        Spacer(modifier = Modifier.height(padding.half))
        OutlinedTextField(value,
            placeholder = { if (placeholder.isNullOrBlank()) else Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(icon) },
            onValueChange = { onChange(it) })
        Spacer(modifier = Modifier.height(padding.double))
    }
}

@Composable
fun SaveBookmarkFields(
    saveActivityState: SaveActivityState,
    submit: () -> Unit,
    context: Context = LocalContext.current,
) {
    val bookmark by saveActivityState.bookmarkToSave.collectAsState()
    val tagNames by saveActivityState.tagNames.collectAsState()
    val tags by saveActivityState.tags.collectAsState()
    val selectedTags by saveActivityState.selectedTags.collectAsState()
    var collapseTags by remember { mutableStateOf(true) }

    var clip by remember { mutableStateOf("") }
//    var
    LaunchedEffect(key1 = true) {
        clip = readClipboard(context)
    }

    Column(
        modifier = Modifier
            .padding(
                top = padding.standard,
                start = padding.standard,
                end = padding.standard,
                bottom = padding.half
            )
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                StyledTextField(name = "URL",
                    placeholder = "Enter a URL to save",
                    value = bookmark.url,
                    icon = Icons.Default.Link,
                    onChange = { saveActivityState.updateBookmark(url = it) })
            }

            // Additional element in the same row
            Spacer(modifier = Modifier.width(8.dp)) // Add space between the elements
            IconButton(
//                modifier = Modifier.width(24.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = colorScheme.primaryContainer,
//                    contentColor = colorScheme.onPrimaryContainer
//                ),
                onClick = {
                    saveActivityState.updateBookmark(url = clip)
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentPasteGo,
                    tint = colorScheme.onBackground
                )
            }
        }

        StyledTextField(name = "Tags",
            placeholder = "Enter tags...",
            value = tagNames,
            icon = Icons.Default.Tag,
            onChange = { saveActivityState.updateTagNames(tagNames = it) })

        var rows by remember { mutableIntStateOf(max(abs(tags.size / 4), 1)) }
        if (collapseTags && rows > 3) rows = 3
        if (!collapseTags) rows = max(abs(tags.size / 4), 1)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height((rows * 40).dp)
        ) {
            LazyHorizontalStaggeredGrid(
                rows = StaggeredGridCells.Adaptive(40.dp)
            ) {
                items(selectedTags.toList().sorted()) {
                    TextButton(onClick = { saveActivityState.toggleSelectTag(it) }) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(colorScheme.tertiary)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                text = "#$it", color = colorScheme.onTertiary
                            )
                        }
                    }
                }
                items((tags - selectedTags).sorted()) {
                    TextButton(
                        onClick = { saveActivityState.toggleSelectTag(it) },
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 2.dp),
                            text = "#$it",
                            color = colorScheme.tertiary,

                            )
                    }
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { collapseTags = !collapseTags }) {
                Icon(imageVector = if (collapseTags) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropUp)
            }
        }
//        Spacer(modifier = Modifier.height(padding.double))

        StyledTextField(name = "Title",
            value = bookmark.title ?: "",
            icon = Icons.Default.Title,
            placeholder = "Leave blank to use website title",
            onChange = { saveActivityState.updateBookmark(title = it.ifBlank { null }) })
        StyledTextField(name = "Description",
            value = bookmark.description ?: "",
            icon = Icons.AutoMirrored.Filled.ShortText,
            placeholder = "Leave blank to use website description",
            onChange = { saveActivityState.updateBookmark(description = it.ifBlank { null }) })

        StyledCheckBox("Share", bookmark.shared, onCheckedChange = {
            saveActivityState.updateBookmark(shared = it)
        })
        StyledCheckBox("Mark as unread", bookmark.unread, onCheckedChange = {
            saveActivityState.updateBookmark(unread = it)
        })

        Spacer(modifier = Modifier.height(padding.standard))

        StyledTextField(name = "Notes",
            value = bookmark.notes ?: "",
            icon = Icons.AutoMirrored.Filled.Notes,
            placeholder = "Enter some notes...",
            onChange = { saveActivityState.updateBookmark(notes = it.ifBlank { null }) })

        Row(
            horizontalArrangement = Arrangement.End, modifier = Modifier
                .padding(
                    end = padding.half, bottom = padding.standard
                )
                .fillMaxWidth()
        ) {
            Button(onClick = { submit() }) {
                Icon(imageVector = Icons.Default.Bookmark)
                Spacer(modifier = Modifier.width(padding.half))
                Text(
                    modifier = Modifier.padding(vertical = padding.half),
                    text = "Save Bookmark"
                )
            }
        }
    }
}

@Composable
fun StyledCheckBox(
    name: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = name, style = typography.titleMedium
        )
        Checkbox(checked = checked, onCheckedChange = { onCheckedChange(it) })
    }
}

@ThemePreview
@Composable
fun SaveBookmarkScreenPreview() {
    AppTheme {
        Surface {
            val saveActivityState = SaveActivityState()
            saveActivityState.updateBookmark("https://alpinelinux.org")
            saveActivityState.setTags(
                listOf(
                    "cool",
                    "selfhost",
                    "tag",
                    "name",
                    "\$hit",
                    "is",
                    "cool",
                    "jetpack-compose",
                    "android",
                    "development",
                    "selfhost",
                    "server",
                    "gaming",
                    "amazon",
                    "prime",
                    "garbage",
                    "man",
                    "why-though",
                    "chadland",
                    "chetland",
                )
            )
            saveActivityState.toggleSelectTag("cool")
            saveActivityState.toggleSelectTag("selfhost")
            SaveBookmarkScreen(
                nav = EmptyDestinationsNavigator,
                saveActivityState = saveActivityState
            )
        }
    }
}
