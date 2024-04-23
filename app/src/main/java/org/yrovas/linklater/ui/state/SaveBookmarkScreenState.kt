package org.yrovas.linklater.ui.state

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.checkURL
import org.yrovas.linklater.data.LocalBookmark
import org.yrovas.linklater.data.local.PrefDataStore
import org.yrovas.linklater.data.local.Prefs
import org.yrovas.linklater.domain.APIError
import org.yrovas.linklater.domain.BookmarkAPI
import org.yrovas.linklater.domain.BookmarkDataSource
import org.yrovas.linklater.domain.Res
import org.yrovas.linklater.domain.TagDataSource
import org.yrovas.linklater.intoTags
import org.yrovas.linklater.ui.state.SaveBookmarkScreenState.Effect
import org.yrovas.linklater.ui.state.SaveBookmarkScreenState.Event

@Inject
class SaveBookmarkScreenState(
    private val bookmarkAPI: BookmarkAPI,
    private val prefStore: PrefDataStore,
    private val tagSource: TagDataSource,
    private val bookmarkSource: BookmarkDataSource,
) : ScreenState<Event, Effect>() {

    sealed interface Event : ScreenEvent {
        data object SubmitBookmark : Event
        data class UpdateBookmark(
            val url: String? = null,
            val title: String? = null,
            val description: String? = null,
            val notes: String? = null,
            val unread: Boolean? = null,
            val shared: Boolean? = null,
            val is_archived: Boolean? = null,
        ) : Event

        data class UpdateTagNames(val tagNames: String) : Event
        data class ToggleSelectTag(val tagName: String) : Event
        data class SelectTagPrediction(val tagName: String) : Event
    }

    sealed interface Effect : ScreenEffect {
        data object SubmitSuccess : Effect
        data class SubmitError(val error: APIError) : Effect
        data class InvalidBookmark(val message: String) : Effect
    }

    private val _bookmarkToSave = MutableStateFlow(LocalBookmark(""))
    val bookmarkToSave = _bookmarkToSave.asStateFlow()

    private val _tagNames = MutableStateFlow("")
    val tagNames = _tagNames.asStateFlow()

    private val _tags: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    val tags = _tags.asStateFlow()

    private val _selectedTags = MutableStateFlow(emptyList<String>())
    val selectedTags = _selectedTags.asStateFlow()

    private val _predictionTags = MutableStateFlow(emptyList<String>())
    val predictedTags = _predictionTags.asStateFlow()

    private val _showPaste = MutableStateFlow(bookmarkToSave.value.url.isBlank())
    val showPaste = _showPaste.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private fun updateBookmark(
        url: String? = null,
        title: String? = null,
        description: String? = null,
        notes: String? = null,
        is_archived: Boolean? = null,
        unread: Boolean? = null,
        shared: Boolean? = null,
    ) {
        _showPaste.update { url.isNullOrBlank() }
        _bookmarkToSave.update {
            bookmarkToSave.value.withUpdates(
                url = url,
                title = title,
                description = description,
                notes = notes,
                is_archived = is_archived,
                unread = unread,
                shared = shared,
            )
        }
    }

    private fun updateTagNames(tagNameString: String) {
        _tagNames.update { tagNameString }
        viewModelScope.launch(Dispatchers.Main) {
            _predictionTags.update {
                if (tagNameString.isBlank()) {
                    emptyList()
                } else {
                    val partialTag =
                        tagNameString.trim().split("\\s+".toRegex()).last()
                    Log.d(TAG, "updateTagNames: partialTag: $partialTag")
                    tags.value.filter { it.startsWith(partialTag) }.take(5)
                }
            }
        }
    }

    private fun toggleSelectTag(tag: String) {
        _selectedTags.update {
            (if (it.contains(tag)) it - tag
            else it + tag).sorted().distinct()
        }
    }

    private fun selectTagPrediction(tag: String) {
        if (!_selectedTags.value.contains(tag)) {
            toggleSelectTag(tag)
        }
        _predictionTags.update { emptyList() }
        val tagList = tagNames.value.trim().split("\\s+".toRegex())
        _tagNames.update { tagList.dropLast(1).joinToString(" ") }
    }

    private fun submitBookmark() {
        if (!checkURL(bookmarkToSave.value.url)) {
            sendEffect(Effect.InvalidBookmark(message = "Invalid URL"))
            return
        }
        _isSubmitting.update { true }
        val tags =
            (bookmarkToSave.value.tags + selectedTags.value + tagNames.value.intoTags()).distinct()
        val bookmark =
            bookmarkToSave.value.withUpdates(tags = tags.ifEmpty { null })
        viewModelScope.launch(Dispatchers.IO) {
            when (val res = bookmarkAPI.saveBookmark(bookmark)) {
                is Res.Err -> sendEffect(Effect.SubmitError(res.error))
                is Res.Ok -> {
                    bookmarkSource.insertBookmark(res.data)
                    sendEffect(Effect.SubmitSuccess)
                }
            }
            _isSubmitting.update { false }
        }
    }

    private fun setTagList(tagList: List<String>) {
        _tags.update { tagList.sorted() }
    }

    private fun getDefaultBookmark() {
        viewModelScope.launch(Dispatchers.IO) {
            updateBookmark(
                is_archived = prefStore.getPref(
                    Prefs.BOOKMARK_DEFAULT_ARCHIVED, false
                ),
                unread = prefStore.getPref(Prefs.BOOKMARK_DEFAULT_UNREAD, false),
                shared = prefStore.getPref(Prefs.BOOKMARK_DEFAULT_SHARED, false)
            )
        }
    }

    private fun getDefaultTags() {
        viewModelScope.launch(Dispatchers.IO) {
            val selectedTagList =
                prefStore.getPref(Prefs.BOOKMARK_DEFAULT_TAG_NAMES, "").intoTags()
            setTagList((tags.value + selectedTagList).distinct())
            selectedTagList.forEach {
                toggleSelectTag(it)
            }
        }
    }

    private fun getTagList() {
        viewModelScope.launch(Dispatchers.IO) {
            tagSource.getTags().collect {
                setTagList((tags.value + it).distinct())
            }
        }
    }

    init {
        getTagList()
        getDefaultBookmark()
        getDefaultTags()
    }

    override fun handleEvent(event: Event) {
        when (event) {
            Event.SubmitBookmark -> submitBookmark()
            is Event.ToggleSelectTag -> toggleSelectTag(event.tagName)
            is Event.UpdateBookmark -> updateBookmark(
                url = event.url,
                title = event.title,
                description = event.description,
                notes = event.notes,
                is_archived = event.is_archived,
                unread = event.unread,
                shared = event.shared,
            )

            is Event.UpdateTagNames -> updateTagNames(event.tagNames)
            is Event.SelectTagPrediction -> selectTagPrediction(event.tagName)
        }
    }
}
