package org.yrovas.linklater.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.yrovas.linklater.SaveActivityState
import org.yrovas.linklater.SaveBookmarkActivity
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
    a: SaveBookmarkActivity = (LocalContext.current as SaveBookmarkActivity),
) {

    val submit = {
        a.launch { saveActivityState.submitBookmark() }
        a.onBackPressed()
    }

    Surface {
        Column {
            AppBar(page = "Add Bookmark", nav = nav, back = {
                a.onBackPressed()
            }) {
                IconButton(onClick = {
                    if (saveActivityState.validateBookmark()) { submit() }
                }) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(padding.half))
            }
            SaveBookmarkFields(saveActivityState = saveActivityState, submit = submit)
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
    Text(text = name, style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(padding.half))
    OutlinedTextField(value,
        placeholder = { if (placeholder.isNullOrBlank()) else Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(icon) },
        onValueChange = { onChange(it) })
    Spacer(modifier = Modifier.height(padding.double))
}

@Composable
fun SaveBookmarkFields(saveActivityState: SaveActivityState, submit: () -> Unit) {
    val bookmark by saveActivityState.bookmarkToSave.collectAsState()
    val tagNames by saveActivityState.tagNames.collectAsState()
    val tags by saveActivityState.tags.collectAsState()
    val selectedTags by saveActivityState.selectedTags.collectAsState()
    var collapseTags by remember { mutableStateOf(true) }

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
        StyledTextField(name = "URL",
            placeholder = "Enter a URL to save",
            value = bookmark.url,
            icon = Icons.Default.Link,
            onChange = { saveActivityState.updateBookmark(url = it) })
        StyledTextField(name = "Tags",
            placeholder = "Enter tags...",
            value = tagNames,
            icon = Icons.Default.Tag,
            onChange = { saveActivityState.updateTagNames(tagNames = it) })

        var rows by remember { mutableStateOf(max(abs(tags.size / 5), 1)) }
        if (collapseTags && rows > 3) rows = 3
        if (!collapseTags) rows = max(abs(tags.size / 5), 1)
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
                                .background(MaterialTheme.colorScheme.tertiary)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                text = "#$it",
                                color = MaterialTheme.colorScheme.onTertiary
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
                            color = MaterialTheme.colorScheme.tertiary,

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
            Button(onClick = {
                if (saveActivityState.validateBookmark()) {
                    submit()
                }
            }) {
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
            text = name, style = MaterialTheme.typography.titleMedium
        )
        Checkbox(checked = checked, onCheckedChange = { onCheckedChange(it) })
    }
}

@ThemePreview
@Composable
fun SaveBookmarkFieldPreview() {
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
            SaveBookmarkFields(saveActivityState = saveActivityState, submit = {})
        }
    }
}
