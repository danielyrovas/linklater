package org.yrovas.linklater.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.outlined.ContentPasteGo
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import org.yrovas.linklater.ThemePreview
import org.yrovas.linklater.data.local.EmptyBookmarkSource
import org.yrovas.linklater.data.local.EmptyPrefStore
import org.yrovas.linklater.data.local.EmptyTagSource
import org.yrovas.linklater.data.remote.EmptyBookmarkAPI
import org.yrovas.linklater.domain.APIError
import org.yrovas.linklater.domain.Res
import org.yrovas.linklater.domain.apply
import org.yrovas.linklater.domain.isNotNull
import org.yrovas.linklater.domain.isOk
import org.yrovas.linklater.launch
import org.yrovas.linklater.onBackPressed
import org.yrovas.linklater.readClipboard
import org.yrovas.linklater.ui.common.AppBar
import org.yrovas.linklater.ui.common.Frame
import org.yrovas.linklater.ui.common.Icon
import org.yrovas.linklater.ui.state.SaveBookmarkScreenState
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round

@Destination<RootGraph>
@Composable
fun SaveBookmarkScreen(
    nav: DestinationsNavigator,
    snackState: SnackbarHostState,
    context: Context = LocalContext.current,
    state: () -> SaveBookmarkScreenState,
    back: () -> Unit = { nav.popBackStack() },
    onSubmitSuccess: suspend () -> Unit = {
        context.launch {
            snackState.showSnackbar("Saved Bookmark")
        }
        back()
    },
) {
    @Suppress("NAME_SHADOWING") val state = viewModel { state() }

    var isSubmitting by remember { mutableStateOf(false) }
    val submit = {
        if (state.validateBookmark()) {
            state.submitBookmark()
            isSubmitting = true
        }
    }

    Frame(appBar = {
        AppBar(page = "Add Bookmark", back = back) {
            IconButton(onClick = submit) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    tint = colorScheme.primary
                )
            }
        }
    }, snackState = snackState) {
        if (isSubmitting) SaveBookmarkResult(
            state = state,
            onSubmitSuccess = onSubmitSuccess
        )
        else SaveBookmarkFields(state = state, submit = submit)
    }
}

@Composable
fun SaveBookmarkResult(
    state: SaveBookmarkScreenState,
    onSubmitSuccess: suspend () -> Unit,
    context: Context = LocalContext.current,
) {
    val submitResult by state.submitResult.collectAsState()
    LaunchedEffect(submitResult.isNotNull()) {
        if (submitResult.isNotNull() && submitResult!!.isOk) {
            onSubmitSuccess()
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        submitResult.apply(ok = {
            CheckIcon {}
            Spacer(Modifier.height(padding.double))
            Text("Saved Bookmark")
        }, err = {
            CrossIcon { context.onBackPressed() }
            Spacer(Modifier.height(padding.double))
            Text(
                when ((submitResult as Res.Err<String, APIError>).error) {
                    APIError.NO_CONNECTION -> "Failed to connect to LinkDing"
                    APIError.INCORRECT_AUTH -> "Failed to authenticate"
                    APIError.NO_AUTH_PROVIDED -> "Failed to authenticate"
                    APIError.INCORRECT_ENDPOINT -> "Failed to connect"
                }
            )

        }, nil = {
            CircularProgressIndicator()
        })
    }
}

@Composable
private fun SaveBookmarkFields(
    state: SaveBookmarkScreenState,
    submit: () -> Unit,
    context: Context = LocalContext.current,
) {
    val bookmark by state.bookmarkToSave.collectAsState()
    val showPaste by state.showPaste.collectAsState()

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
        StyledURLRow(value = bookmark.url,
            showPaste = showPaste,
            onPaste = { state.updateBookmark(url = context.readClipboard()) },
            onChange = { state.updateBookmark(url = it) })

        StyledTagRow(state)

        Spacer(modifier = Modifier.height(padding.standard))

        StyledCheckBox("Share", bookmark.shared, onCheckedChange = {
            state.updateBookmark(shared = it)
        })
        StyledCheckBox("Mark as unread", bookmark.unread, onCheckedChange = {
            state.updateBookmark(unread = it)
        })

        Spacer(modifier = Modifier.height(padding.standard))

        StyledTextField(name = "Title",
            value = bookmark.title ?: "",
            icon = Icons.Default.Title,
            placeholder = "Leave blank to use website title",
            onChange = { state.updateBookmark(title = it.ifBlank { null }) })
        StyledTextField(name = "Description",
            value = bookmark.description ?: "",
            icon = Icons.AutoMirrored.Filled.ShortText,
            placeholder = "Leave blank to use website description",
            onChange = { state.updateBookmark(description = it.ifBlank { null }) })

        StyledTextField(name = "Notes",
            value = bookmark.notes ?: "",
            icon = Icons.AutoMirrored.Filled.Notes,
            placeholder = "Enter some notes...",
            onChange = { state.updateBookmark(notes = it.ifBlank { null }) })
        SubmitButton { submit() }
    }
}

@Composable
fun SubmitButton(onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .padding(end = padding.half, bottom = padding.standard)
            .fillMaxWidth()
    ) {
        Button(onClick = onClick) {
            Icon(imageVector = Icons.Default.Bookmark)
            Spacer(modifier = Modifier.width(padding.half))
            Text(
                modifier = Modifier.padding(vertical = padding.half),
                text = "Save Bookmark"
            )
        }
    }
}

@Composable
fun Tag(tag: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .background(colorScheme.background),
            text = "#$tag",
            color = colorScheme.tertiary
        )
    }
}

@Composable
fun SelectedTag(tag: String, onClick: (() -> Unit)) {
    TextButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(colorScheme.tertiary)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 10.dp),
                text = "#$tag",
                color = colorScheme.onTertiary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StyledTagRow(
    state: SaveBookmarkScreenState,
) {
    var collapseTags by remember { mutableStateOf(true) }
    val tagNames by state.tagNames.collectAsState()
    val tags by state.tags.collectAsState()
    val selectedTags by state.selectedTags.collectAsState()
    val unselectedTags = remember(tags, selectedTags) {
        mutableStateOf(tags - selectedTags.toSet())
    }

    StyledTextField(name = "Tags",
        placeholder = "Enter tags...",
        value = tagNames,
        icon = Icons.Default.Tag,
        onChange = { state.updateTagNames(tagNames = it) })

    if (selectedTags.isNotEmpty()) {
        LazyRow {
            items(selectedTags) {
                SelectedTag(it) {
                    state.toggleSelectTag(it)
                }
            }
        }
    }
    val rowSize by remember(unselectedTags.value) {
        mutableIntStateOf(
            max(round(unselectedTags.value.size / 3f).toInt() + 1, 8)
        )
    }
    if (unselectedTags.value.isNotEmpty()) {
        Text(text = "Add tags...", style = typography.titleMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            if (collapseTags && unselectedTags.value.size >= 7) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .horizontalScroll(rememberScrollState())
                ) {
                    FlowRow(
                        maxItemsInEachRow = rowSize
                    ) {
                        unselectedTags.value.forEach {
                            Tag(it) {
                                state.toggleSelectTag(it)
                            }
                        }
                    }
                }
            } else {
                FlowRow() {
                    unselectedTags.value.forEach {
                        Tag(it) {
                            state.toggleSelectTag(it)
                        }
                    }
                }
            }
        }
        if (unselectedTags.value.size > 8) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { collapseTags = !collapseTags }) {
                    Icon(imageVector = if (collapseTags) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropUp)
                }
            }
        }
    }
//    TagPredictRow(state) // attached to top of keyboard similar to Brave/Chrome
}

@Composable
private fun TagPredictRow(state: SaveBookmarkScreenState) {
    val tagNames by state.tagNames.collectAsState()

    // TODO:
    // if textfield is focussed, show a bar above the keyboard
    // that displays autofill options for known tags
    // uses the current word as tag suggestion prompt
}

@Composable
private fun TagRow(
    state: SaveBookmarkScreenState,
) {
    val tagNames by state.tagNames.collectAsState()
    val tags by state.tags.collectAsState()
    val selectedTags by state.selectedTags.collectAsState()
    var collapseTags by remember { mutableStateOf(true) }

    var rows by remember { mutableIntStateOf(max(abs(tags.size / 4), 1)) }
    if (collapseTags && rows > 3) rows = 3
    if (!collapseTags) rows = max(abs(tags.size / 4), 1)

    StyledTextField(name = "Tags",
        placeholder = "Enter tags...",
        value = tagNames,
        icon = Icons.Default.Tag,
        onChange = { state.updateTagNames(tagNames = it) })

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height((rows * 40).dp)
    ) {
        LazyHorizontalStaggeredGrid(
            rows = StaggeredGridCells.Adaptive(40.dp)
        ) {
            items(selectedTags.toList().sorted()) {
                SelectedTag(it) {
                    state.toggleSelectTag(it)
                }
            }
            items((tags - selectedTags.toSet())) {
                Tag(it) {
                    state.toggleSelectTag(it)
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
}

@Composable
private fun StyledURLRow(
    value: String,
    showPaste: Boolean,
    onPaste: () -> Unit,
    onChange: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.animateContentSize(
                    animationSpec = tween(durationMillis = 50)
                )
            ) {
                Text(text = "URL", style = typography.titleMedium)
                Spacer(modifier = Modifier.height(padding.half))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = tween(durationMillis = 150))
                ) {
                    OutlinedTextField(value,
                        placeholder = { Text("Enter a URL to save") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Link) },
                        onValueChange = { onChange(it) })
                }
                Spacer(modifier = Modifier.height(padding.double))
            }
        }
        AnimatedVisibility(
            visible = showPaste,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(100))
        ) {
            IconButton(
                modifier = Modifier.padding(start = padding.standard),
                onClick = onPaste
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentPasteGo,
                    tint = colorScheme.onBackground
                )
            }
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
    Column(
        modifier = Modifier.animateContentSize(
            animationSpec = tween(durationMillis = 50)
        )
    ) {
        Text(text = name, style = typography.titleMedium)
        Spacer(modifier = Modifier.height(padding.half))
        OutlinedTextField(value,
            placeholder = { if (!placeholder.isNullOrBlank()) Text(placeholder) },
            leadingIcon = { Icon(icon) },
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { onChange(it) })
        Spacer(modifier = Modifier.height(padding.double))
    }
}

@Composable
private fun StyledCheckBox(
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

@Composable
private fun CheckIcon(onClick: (() -> Unit)?) {
    StyledBoxIcon(
        fg = colorScheme.tertiary,
        bg = colorScheme.tertiaryContainer,
        icon = Icons.Default.Check,
        onClick = onClick,
    )
}

@Composable
private fun CrossIcon(onClick: (() -> Unit)?) {
    StyledBoxIcon(
        fg = colorScheme.error,
        bg = colorScheme.errorContainer,
        icon = Icons.Default.Close,
        onClick = onClick,
    )
}

@Composable
private fun StyledBoxIcon(
    fg: Color,
    bg: Color,
    icon: ImageVector,
    innerSize: Dp = 48.dp,
    outerSize: Dp = 52.dp,
    clip: Shape = CircleShape,
    onClick: (() -> Unit)? = null,
) {
    val m = Modifier
        .size(outerSize)
        .clip(CircleShape)
        .background(fg)
    if (onClick != null) {
        m.clickable { onClick() }
    }
    Box(
        modifier = m,
    ) {
        Box(
            modifier = Modifier
                .size(innerSize)
                .clip(clip)
                .background(bg)
                .align(Alignment.Center),
        ) {
            Icon(
                imageVector = icon,
                tint = fg,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


@ThemePreview
@Composable
fun SaveBookmarkScreenPreview() {
    AppTheme {
        val state = SaveBookmarkScreenState(
            EmptyBookmarkAPI(),
            EmptyPrefStore(),
            EmptyTagSource(),
            EmptyBookmarkSource()
        )
        state.setTags(
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
        state.updateBookmark("https://alpinelinux.org/arbitrary/URL/that-is-far-to-long-andhassomelongerwordsthatareannoying-especially-for-a-text-field.html")
        state.toggleSelectTag("cool")
        state.toggleSelectTag("selfhost")
//        state.setSubmitResult(Err(APIError.AUTH))
        SaveBookmarkScreen(
            nav = EmptyDestinationsNavigator,
            snackState = SnackbarHostState(),
            state = { state }
        )
    }
}
