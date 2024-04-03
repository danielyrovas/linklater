package org.yrovas.linklater.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import org.yrovas.linklater.*
import org.yrovas.linklater.domain.*
import org.yrovas.linklater.ui.common.*
import org.yrovas.linklater.ui.state.SaveBookmarkScreenState
import org.yrovas.linklater.ui.theme.AppTheme
import org.yrovas.linklater.ui.theme.padding
import kotlin.math.abs
import kotlin.math.max

@Destination<RootGraph>
@Composable
fun SaveBookmarkScreen(
    nav: DestinationsNavigator,
    appViewModel: AppViewModel,
    snackState: SnackbarHostState,
    context: Context = LocalContext.current,
    state: SaveBookmarkScreenState = SaveBookmarkScreenState(appViewModel.bookmarkAPI),
    back: () -> Unit = { nav.popBackStack() },
    onSubmitSuccess: suspend () -> Unit = {
        context.launch {
            snackState.showSnackbar("Saved Bookmark")
        }
        back()
    },
) {
    LaunchedEffect(true) { state.setup(context) }
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
            snackState = snackState,
            onSubmitSuccess = onSubmitSuccess
        )
        else SaveBookmarkFields(state = state, submit = submit)
    }
}

@Composable
fun <D, T : Any, E : Error> Res<D, E>?.then(
    ok: @Composable (data: D) -> T,
    err: @Composable (error: E) -> T,
    nil: @Composable () -> T,
): T {
    return when (this) {
        is Res.Err -> err(error)
        is Res.Ok -> ok(data)
        null -> nil()
    }
}

@Composable
fun SaveBookmarkResult(
    state: SaveBookmarkScreenState,
    snackState: SnackbarHostState,
    onSubmitSuccess: suspend () -> Unit,
) {
    val submitResult by state.submitResult.collectAsState()
//    var networking by remember { mutableStateOf(false) }

    LaunchedEffect(submitResult.isNotNull()) {
        if (submitResult.isNotNull() && submitResult!!.isOk) {
            onSubmitSuccess()
        }
    }
//        networking = true
//        submitResult = state.submitBookmark()
//        if (submitResult.isOk) {
//            onSubmitSuccess()
//        }
//        networking = false
//    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        submitResult.then(ok = {
            CheckIcon()
            Spacer(Modifier.height(padding.double))
            Text("Saved Bookmark")
        }, err = {
            CrossIcon()
            Spacer(Modifier.height(padding.double))
            Text(
                when ((submitResult as Res.Err<String, APIError>).error) {
                    APIError.CONNECTION -> "Failed to connect to LinkDing"
                    APIError.AUTH -> "Failed to authenticate"
                }
            )

        }, nil = {
            CircularProgressIndicator()
        }

        )
//        when (submitResult) {
//            is Res.Err -> {
//                CrossIcon()
//                Spacer(Modifier.height(padding.double))
//                    when ((submitResult as Res.Err<String, APIError>).error) {
//                        APIError.CONNECTION -> "Failed to connect to LinkDing"
//                        APIError.AUTH -> "Failed to authenticate"
//                    }
//            }
//            is Res.Ok -> {
//                CheckIcon()
//            Spacer(Modifier.height(padding.double))
//                Text("Saved Bookmark")
//            }
//            null -> CircularProgressIndicator()
//        }
//        if (submitResult.isNull()) {
//            CircularProgressIndicator()
//        } else {
//            if (submitResult.isOk) CheckIcon() else CrossIcon()
//            Spacer(modifier = Modifier.height(padding.double))
//            Text(
//                submitResult.then(ok = { "Saved Bookmark" }, err = {
//                    when (it) {
//                        APIError.CONNECTION -> "Failed to connect to LinkDing"
//                        APIError.AUTH -> "Failed to authenticate"
//                    }
//                }), style = typography.headlineSmall
//            )
//            Spacer(modifier = Modifier.height(padding.large))
//        }
    }
}

@Composable
fun SaveBookmarkFields(
    state: SaveBookmarkScreenState,
    submit: () -> Unit,
    context: Context = LocalContext.current,
) {
    val bookmark by state.bookmarkToSave.collectAsState()
    val tagNames by state.tagNames.collectAsState()
    val tags by state.tags.collectAsState()
    val selectedTags by state.selectedTags.collectAsState()
    var collapseTags by remember { mutableStateOf(true) }
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

        StyledTextField(name = "Tags",
            placeholder = "Enter tags...",
            value = tagNames,
            icon = Icons.Default.Tag,
            onChange = { state.updateTagNames(tagNames = it) })

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
                    TextButton(onClick = {
                        state.toggleSelectTag(it)
                    }) {
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
                        onClick = { state.toggleSelectTag(it) },
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .background(colorScheme.background),
                            text = "#$it",
                            color = colorScheme.tertiary
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
            onChange = { state.updateBookmark(title = it.ifBlank { null }) })
        StyledTextField(name = "Description",
            value = bookmark.description ?: "",
            icon = Icons.AutoMirrored.Filled.ShortText,
            placeholder = "Leave blank to use website description",
            onChange = { state.updateBookmark(description = it.ifBlank { null }) })

        StyledCheckBox("Share", bookmark.shared, onCheckedChange = {
            state.updateBookmark(shared = it)
        })
        StyledCheckBox("Mark as unread", bookmark.unread, onCheckedChange = {
            state.updateBookmark(unread = it)
        })

        Spacer(modifier = Modifier.height(padding.standard))

        StyledTextField(name = "Notes",
            value = bookmark.notes ?: "",
            icon = Icons.AutoMirrored.Filled.Notes,
            placeholder = "Enter some notes...",
            onChange = { state.updateBookmark(notes = it.ifBlank { null }) })

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
fun StyledURLRow(
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

@Composable
fun CheckIcon() {
    StyledBoxIcon(
        fg = colorScheme.tertiary,
        bg = colorScheme.tertiaryContainer,
        iv = Icons.Default.Check
    )
}

@Composable
fun CrossIcon() {
    StyledBoxIcon(
        fg = colorScheme.error,
        bg = colorScheme.errorContainer,
        iv = Icons.Default.Close
    )
}

@Composable
fun StyledBoxIcon(
    fg: Color,
    bg: Color,
    iv: ImageVector,
    context: Context = LocalContext.current,
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(fg)
            .clickable { context.onBackPressed() },
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(bg)
                .align(Alignment.Center),
        ) {
            Icon(
                imageVector = iv,
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
        val state = SaveBookmarkScreenState(EmptyBookmarkAPI())
        state.updateBookmark("https://alpinelinux.org/arbitrary/URL/that-is-far-to-long-andhassomelongerwordsthatareannoying-especially-for-a-text-field.html")
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
        state.toggleSelectTag("cool")
        state.toggleSelectTag("selfhost")
        state.setSubmitResult(Err(APIError.AUTH))
        SaveBookmarkScreen(
            nav = EmptyDestinationsNavigator,
            appViewModel = PreviewAppViewModel(),
            snackState = SnackbarHostState(),
            state = state
        )
    }
}
