package org.yrovas.linklater.ui.screens.saveBookmark

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.readClipboard
import org.yrovas.linklater.show
import org.yrovas.linklater.ui.activity.AppActivity
import org.yrovas.linklater.ui.common.Frame
import org.yrovas.linklater.ui.common.Icon
import org.yrovas.linklater.ui.common.KeyboardRow
import org.yrovas.linklater.ui.common.SelectedTagChip
import org.yrovas.linklater.ui.common.TagChip
import org.yrovas.linklater.ui.common.TagPredictRow
import org.yrovas.linklater.ui.screens.LocalBackStack
import org.yrovas.linklater.ui.screens.LocalSnackState
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkModel.Effect
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkModel.Event
import org.yrovas.linklater.ui.screens.saveBookmark.components.AnimatePasteTextField
import org.yrovas.linklater.ui.screens.saveBookmark.components.DualLazyRow
import org.yrovas.linklater.ui.screens.saveBookmark.components.ExpandableTagRow
import org.yrovas.linklater.ui.screens.saveBookmark.components.TitledTextField
import org.yrovas.linklater.ui.theme.padding

typealias SaveBookmarkScreen = @Composable (bookmarkParam: BookmarkParam) -> Unit

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Inject
@Composable
fun SaveBookmarkScreen(
    @Assisted bookmarkParam: BookmarkParam,
    saveBookmarkModel: (bookmarkParam: BookmarkParam) -> SaveBookmarkModel,
    backStack: NavBackStack = LocalBackStack.current,
    back: () -> Unit = { backStack.removeLastOrNull() },
    snackState: SnackbarHostState = LocalSnackState.current,
    exitOnSuccess: Boolean = false,
) {
    val state = viewModel { saveBookmarkModel(bookmarkParam) }
    val isSubmitting by state.isSubmitting.collectAsState()
    val scope = rememberCoroutineScope()
    val bookmarkExists by state.bookmarkExists.collectAsState()
    val previewTitle by state.previewTitle.collectAsState()
    val previewDescription by state.previewDescription.collectAsState()
    val showPaste by remember(state.bookmarkURL.text) {
        derivedStateOf { state.bookmarkURL.text.isBlank() }
    }
    val unread by state.bookmarkUnread.collectAsState()
    val shared by state.bookmarkShared.collectAsState()
    val selectedTags by state.selectedTags.collectAsState()
    val moreTags by state.moreTags.collectAsState()
    var showPredictedTags by remember { mutableStateOf(false) }
    val recentTags by state.recentTags.collectAsState()
    val context: Context = LocalContext.current
    val onSubmitSuccess: suspend () -> Unit = {
        (context as AppActivity).lifecycleScope.launch(Dispatchers.Main) {
            if (exitOnSuccess) {
                Toast.makeText(context, "Saved Bookmark", Toast.LENGTH_SHORT).show()
                context.finish()
            } else {
                backStack.removeLastOrNull()
                snackState.showSnackbar("Saved Bookmark")
            }
        }
    }

    state.subscribeEffects { effect ->
        when (effect) {
            Effect.SubmitSuccess -> scope.launch { onSubmitSuccess() }
            is Effect.SubmitError -> scope.launch {
                snackState.show(effect.error)
            }

            is Effect.InvalidBookmark -> scope.launch {
                snackState.showSnackbar(effect.message)
            }
        }
    }


    if (isSubmitting) SavingBookmark()
    else Frame(
        title = if (bookmarkExists) "Edit Bookmark" else "Add Bookmark",
        back = back,
        globalContent = {
            if (showPredictedTags) KeyboardRow {
                TagPredictRow(predictedTags = state.predictedTags) {
                    state.sendEvent(Event.SelectTagPrediction(it))
                }
            }
        },
        actions = {
            clickableItem(
                onClick = {
                    state.sendEvent(Event.SubmitBookmark)
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Bookmark, tint = colorScheme.primary
                    )
                },
                label = "Save",
            )
        },
        fab = {
            SmallExtendedFloatingActionButton(
                onClick = { state.sendEvent(Event.SubmitBookmark) },
                icon = { Icon(Icons.Default.Bookmark) },
                text = { Text("Save Bookmark") },
            )
        }) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding.md)
                .verticalScroll(rememberScrollState()),
        ) {
            AnimatePasteTextField(
                state = state.bookmarkURL,
                label = "URL",
                placeholder = "Enter URL...",
                leadingIcon = Icons.Default.Link,
                showPaste = showPaste,
                onPaste = {
                    state.sendEvent(Event.PasteURL(context.readClipboard()))
                })
            TitledTextField(
                state = state.bookmarkTagNames,
                onFocusChanged = { showPredictedTags = it },
                modifier = Modifier.padding(bottom = padding.md),
                label = "Tags",
                placeholder = "Enter tags...",
                leadingIcon = Icons.Default.Tag,
            )
            DualLazyRow(
                items = selectedTags,
                horizontalArrangement = Arrangement.spacedBy(padding.sm),
                spaceBetween = padding.sm,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = tween(durationMillis = 180)
                    )
            ) { tag ->
                SelectedTagChip(
                    modifier = Modifier.animateItem(),
                    tag = tag,
                    onClick = { state.sendEvent(Event.ToggleSelectTag(tag)) })
            }
            Spacer(modifier = Modifier.height(padding.md))
            Text(
                modifier = Modifier, text = "Recent Tags", style = typography.titleMedium
            )
            DualLazyRow(items = recentTags) { tag ->
                TagChip(
                    modifier = Modifier.animateItem(),
                    tag = tag,
                    onClick = { state.sendEvent(Event.ToggleSelectTag(tag)) })
            }
            Spacer(modifier = Modifier.height(padding.md))

            TitledTextField(
                state = state.bookmarkTitle,
                modifier = Modifier.padding(bottom = padding.lg),
                label = "Title",
                placeholder = previewTitle ?: "Leave blank to use website title",
                leadingIcon = Icons.Default.Title,
            )
            TitledTextField(
                state = state.bookmarkDescription,
                modifier = Modifier.padding(bottom = padding.lg),
                label = "Description",
                placeholder = previewDescription ?: "Leave blank to use website description",
                leadingIcon = Icons.AutoMirrored.Filled.ShortText,
            )

            Row(
                modifier = Modifier.padding(bottom = padding.lg),
                horizontalArrangement = Arrangement.spacedBy(padding.sm)
            ) {
                ToggleButton(
                    modifier = Modifier.defaultMinSize(180.dp),
                    checked = unread,
                    onCheckedChange = {
                        state.sendEvent(Event.ToggleUnread(it))
                    }) {
                    if (unread) Icon(
                        Icons.Default.WatchLater,
                        modifier = Modifier
                            .padding(end = padding.sm)
                            .size(16.dp),
                    )
                    Text(if (unread) "Marked as unread" else "Not marked as unread")
                }
                ToggleButton(
                    modifier = Modifier.defaultMinSize(120.dp),
                    checked = shared,
                    onCheckedChange = {
                        state.sendEvent(Event.ToggleShared(it))
                    }) {
                    if (shared) Icon(
                        Icons.Default.Public,
                        modifier = Modifier
                            .padding(end = padding.sm)
                            .size(16.dp),
                    )
                    Text(if (shared) "Shared" else "Not Shared")
                }
            }

            TitledTextField(
                state = state.bookmarkNotes,
                modifier = Modifier.padding(bottom = padding.lg),
                label = "Notes",
                placeholder = "Enter notes...",
                leadingIcon = Icons.AutoMirrored.Filled.Notes
            )

            ExpandableTagRow(
                tags = moreTags, title = "More Tags", onTagSelect = { tag ->
                    state.sendEvent(Event.ToggleSelectTag(tag))
                })

            Spacer(modifier = Modifier.height(padding.xl))
            Spacer(modifier = Modifier.height(padding.lg))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SavingBookmark() {
    Frame(
        title = "Saving...",
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularWavyProgressIndicator()
        }
    }
}
