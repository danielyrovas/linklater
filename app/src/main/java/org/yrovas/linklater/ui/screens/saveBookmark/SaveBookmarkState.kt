package org.yrovas.linklater.ui.screens.saveBookmark

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkState.Effect
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkState.Event
import org.yrovas.linklater.ui.screens.ScreenEffect
import org.yrovas.linklater.ui.screens.ScreenEvent
import org.yrovas.linklater.ui.screens.ScreenState

@Inject
class SaveBookmarkState(
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

    private val _previewTitle: MutableStateFlow<String?> = MutableStateFlow(null)
    val previewTitle = _previewTitle.asStateFlow()

    private val _previewDescription: MutableStateFlow<String?> =
        MutableStateFlow(null)
    val previewDescription = _previewDescription.asStateFlow()

    private val _bookmarkExists = MutableStateFlow(false)
    val bookmarkExists = _bookmarkExists.asStateFlow()

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
        _showPaste.update { bookmarkToSave.value.url.isBlank() }
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
                    tags.value.filter { it.startsWith(partialTag, ignoreCase = true) }.take(5)
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
        var tagNameString =
            tagNames.value.trim().split("\\s+".toRegex()).dropLast(1)
                .joinToString(" ")
        if (tagNameString.isNotBlank()) {
            tagNameString += " "
        }
        _tagNames.update { tagNameString }
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

    private lateinit var defaultBookmark: LocalBookmark
    private fun getDefaultBookmark() {
        viewModelScope.launch(Dispatchers.IO) {
            defaultBookmark = LocalBookmark(
                url = "",
                is_archived = prefStore.getPref(
                    Prefs.BOOKMARK_DEFAULT_ARCHIVED, false
                ),
                unread = prefStore.getPref(Prefs.BOOKMARK_DEFAULT_UNREAD, false),
                shared = prefStore.getPref(Prefs.BOOKMARK_DEFAULT_SHARED, false)
            )
            setDefaultBookmark()
        }
    }

    private fun setDefaultBookmark() {
        updateBookmark(
            is_archived = defaultBookmark.is_archived,
            unread = defaultBookmark.unread,
            shared = defaultBookmark.shared
        )
    }

    private lateinit var defaultTags: List<String>
    private fun getDefaultTags() {
        viewModelScope.launch(Dispatchers.IO) {
            defaultTags =
                prefStore.getPref(Prefs.BOOKMARK_DEFAULT_TAG_NAMES, "").intoTags()
            setTagList((tags.value + defaultTags).distinct())
            _selectedTags.update { defaultTags }
        }
    }

    private fun getTagList() {
        viewModelScope.launch(Dispatchers.IO) {
            tagSource.getTags().collect {
                setTagList((tags.value + it).distinct())
            }
        }
    }

    private fun checkBookmarkExists(url: String): Job {
        return viewModelScope.launch {
            val (b, m) = bookmarkAPI.checkExists(url)
            m?.title?.let { title ->
                _previewTitle.update { title }
            }
            m?.description?.let { description ->
                _previewDescription.update { description }
            }

            if (b != null) {
                _bookmarkExists.update { true }
                updateBookmark(
                    url = b.url,
                    title = b.title,
                    description = b.description,
                    notes = b.notes,
                    is_archived = b.is_archived,
                    unread = b.unread,
                    shared = b.shared,
                )

                setTagList((tags.value + b.tags).distinct()) // guard against tags not being present in database
//                _tagNames.update { "" }
                _selectedTags.update { emptyList() }
                _selectedTags.update { b.tags }
            } else {
                _bookmarkExists.update { false }
            }
        }
    }

    init {
        getTagList()
        getDefaultBookmark()
        getDefaultTags()
    }

    private fun clearPreview() {
        _bookmarkExists.update { false }
        _previewTitle.update { null }
        _previewDescription.update { null }
        _selectedTags.update { defaultTags }
        setDefaultBookmark()
    }

    private var checkExistsJob: Job? = null
    override fun handleEvent(event: Event) {
        when (event) {
            Event.SubmitBookmark -> submitBookmark()
            is Event.ToggleSelectTag -> toggleSelectTag(event.tagName)
            is Event.UpdateBookmark -> {
                viewModelScope.launch {
                    checkExistsJob?.cancel()
                    clearPreview()
                    if (!event.url.isNullOrBlank() && checkURL(event.url)) {
                        checkExistsJob = checkBookmarkExists(event.url)
                    }
                }
                updateBookmark(
                url = event.url,
                title = event.title,
                description = event.description,
                notes = event.notes,
                is_archived = event.is_archived,
                unread = event.unread,
                shared = event.shared,
                )
            }

            is Event.UpdateTagNames -> updateTagNames(event.tagNames)
            is Event.SelectTagPrediction -> selectTagPrediction(event.tagName)
        }
    }
}
