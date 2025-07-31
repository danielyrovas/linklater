package org.yrovas.linklater.ui.screens.saveBookmark

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.mapBoth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.checkURL
import org.yrovas.linklater.data.local.BookmarkDataSource
import org.yrovas.linklater.data.local.PrefStore
import org.yrovas.linklater.data.local.TagDataSource
import org.yrovas.linklater.data.models.APIError
import org.yrovas.linklater.data.models.LocalBookmark
import org.yrovas.linklater.data.remote.BookmarkAPI
import org.yrovas.linklater.intoTags
import org.yrovas.linklater.ui.screens.ScreenEffect
import org.yrovas.linklater.ui.screens.ScreenEvent
import org.yrovas.linklater.ui.screens.ScreenModel
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkModel.Effect
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkModel.Event
import org.yrovas.linklater.ui.usecases.BookmarkSyncUseCase
import org.yrovas.linklater.ui.usecases.TagPredictUseCase
import kotlin.collections.plus
import kotlin.text.lowercase
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Inject
class SaveBookmarkModel(
    private val bookmarkAPI: BookmarkAPI,
    private val tagPredictUseCase: TagPredictUseCase,
    private val tagSource: TagDataSource,
    private val bookmarkSource: BookmarkDataSource,
) : ScreenModel<Event, Effect>() {

    sealed interface Event : ScreenEvent {
        data object SubmitBookmark : Event
        data class SelectTagPrediction(val tag: String) : Event
        data class PasteURL(val url: String) : Event
        data class ToggleUnread(val unread: Boolean) : Event
        data class ToggleSelectTag(val tag: String) : Event
        data class ToggleShared(val shared: Boolean) : Event
    }

    sealed interface Effect : ScreenEffect {
        data object SubmitSuccess : Effect
        data class SubmitError(val error: APIError) : Effect
        data class InvalidBookmark(val message: String) : Effect
    }

    init {
        tagPredictUseCase.observeLocalTags(viewModelScope)
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is Event.SubmitBookmark -> submitBookmark()
            is Event.ToggleShared -> _bookmarkShared.update { event.shared }
            is Event.ToggleUnread -> _bookmarkUnread.update { event.unread }
            is Event.SelectTagPrediction -> selectTagPrediction(event.tag)
            is Event.PasteURL -> bookmarkURL.edit {
                replace(0, bookmarkURL.text.length, event.url)
            }

            is Event.ToggleSelectTag -> {
                _selectedTags.update {
                    if (it.contains(event.tag)) {
                        it - event.tag
                    } else {
                        it + event.tag
                    }.sortedBy { tag -> tag.lowercase() }.distinct()
                }
            }
        }
    }

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _bookmarkExists = MutableStateFlow(false)
    val bookmarkExists = _bookmarkExists.asStateFlow()

    val bookmarkTagNames = TextFieldState()
    val bookmarkURL = TextFieldState()
    val bookmarkTitle = TextFieldState()
    val bookmarkDescription = TextFieldState()
    val bookmarkNotes = TextFieldState()
    private val _bookmarkUnread = MutableStateFlow(false)
    val bookmarkUnread = _bookmarkUnread.asStateFlow()
    private val _bookmarkShared = MutableStateFlow(false)
    val bookmarkShared = _bookmarkShared.asStateFlow()

    private fun selectTagPrediction(tag: String) {
        _selectedTags.update { selectedTags ->
            (selectedTags + tag).sortedBy { it.lowercase() }.distinct()
        }
        bookmarkTagNames.edit {
            val match = "#?\\w+$".toRegex().find(this.originalText)
            if (match != null) {
                delete(match.range.first, match.range.last + 1)
            }
        }
    }

    private val _selectedTags = MutableStateFlow(emptyList<String>())
    val selectedTags = _selectedTags.asStateFlow()

    val unselectedTags: StateFlow<List<String>> =
        combine(tagPredictUseCase.tags, selectedTags) { tags, selected ->
            tags - selected
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = tagPredictUseCase.tags.value
        )

    val recentTags: StateFlow<List<String>> =
        combine(tagSource.getRecentTags(), selectedTags) { recent, selected ->
            recent - selected
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    @OptIn(FlowPreview::class)
    private val bookmarkTagNamesFlow =
        snapshotFlow { bookmarkTagNames.text.toString() }.debounce(80)
    val allPredictedTags =
        tagPredictUseCase.predictedTags(bookmarkTagNamesFlow.map { it }, viewModelScope)
    val predictedTags = combine(allPredictedTags, selectedTags) { predicted, selected ->
        predicted - selected
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = allPredictedTags.value
    )


    private val _previewTitle: MutableStateFlow<String?> = MutableStateFlow(null)
    val previewTitle = _previewTitle.asStateFlow()

    //
    private val _previewDescription: MutableStateFlow<String?> = MutableStateFlow(null)
    val previewDescription = _previewDescription.asStateFlow()

    private fun submitBookmark() {
        if (!checkURL(bookmarkURL.text.toString())) {
            sendEffect(Effect.InvalidBookmark(message = "Invalid URL"))
            return
        }
        _isSubmitting.update { true }
        val bookmark = LocalBookmark(
            url = bookmarkURL.text.toString(),
            tags = (selectedTags.value + bookmarkTagNames.text.toString().intoTags()).distinct(),
            title = bookmarkTitle.text.toString(),
            description = bookmarkDescription.text.toString(),
            unread = bookmarkUnread.value,
            shared = bookmarkShared.value,
            notes = bookmarkNotes.text.toString(),
        )

        viewModelScope.launch(Dispatchers.IO) {
            bookmarkAPI.saveBookmark(bookmark).mapBoth(success = {
                bookmarkSource.insertBookmark(it)
                sendEffect(Effect.SubmitSuccess)
            }, failure = { sendEffect(Effect.SubmitError(it)) })
            _isSubmitting.update { false }
        }
    }

//    private lateinit var defaultBookmark: LocalBookmark
//    private fun getDefaultBookmark() {
//        viewModelScope.launch(Dispatchers.IO) {
//            defaultBookmark = LocalBookmark(
//                url = "",
//                is_archived = prefStore.getPref(
//                    Prefs.BOOKMARK_DEFAULT_ARCHIVED, false
//                ),
//                unread = prefStore.getPref(Prefs.BOOKMARK_DEFAULT_UNREAD, false),
//                shared = prefStore.getPref(Prefs.BOOKMARK_DEFAULT_SHARED, false)
//            )
//            setDefaultBookmark()
//        }
//    }
//
//    private fun setDefaultBookmark() {
//        updateBookmark(
//            is_archived = defaultBookmark.is_archived,
//            unread = defaultBookmark.unread,
//            shared = defaultBookmark.shared
//        )
//    }
//
//    private lateinit var defaultTags: List<String>
//    private fun getDefaultTags() {
//        viewModelScope.launch(Dispatchers.IO) {
//            defaultTags = prefStore.getPref(Prefs.BOOKMARK_DEFAULT_TAG_NAMES, "").intoTags()
//            setTagList((tags.value + defaultTags).distinct())
//            _selectedTags.update { defaultTags }
//        }
//    }
//
//    private fun getTagList() {
//        viewModelScope.launch(Dispatchers.IO) {
//            tagSource.getTags().collect {
//                setTagList((tags.value + it).distinct())
//            }
//        }
//    }
//
//    private fun checkBookmarkExists(url: String): Job {
//        return viewModelScope.launch {
//            val (b, m) = bookmarkAPI.checkExists(url)
//            m?.title?.let { title ->
//                _previewTitle.update { title }
//            }
//            m?.description?.let { description ->
//                _previewDescription.update { description }
//            }
//
//            if (b != null) {
//                _bookmarkExists.update { true }
//                updateBookmark(
//                    url = b.url,
//                    title = b.title,
//                    description = b.description,
//                    notes = b.notes,
//                    is_archived = b.is_archived,
//                    unread = b.unread,
//                    shared = b.shared,
//                )
//
//                setTagList((tags.value + b.tags).distinct()) // guard against tags not being present in database
////                _tagNames.update { "" }
//                _selectedTags.update { emptyList() }
//                _selectedTags.update { b.tags }
//            } else {
//                _bookmarkExists.update { false }
//            }
//        }
//    }
//
//    init {
//        getTagList()
//        getDefaultBookmark()
//        getDefaultTags()
//    }
//
//    private fun clearPreview() {
//        _bookmarkExists.update { false }
//        _previewTitle.update { null }
//        _previewDescription.update { null }
//        _selectedTags.update { defaultTags }
//        setDefaultBookmark()
//    }
//
//    private var checkExistsJob: Job? = null
//    override fun handleEvent(event: Event) {
//        when (event) {
//            Event.SubmitBookmark -> submitBookmark()
//            is Event.ToggleSelectTag -> toggleSelectTag(event.tagName)
//            is Event.UpdateBookmark -> {
//                viewModelScope.launch {
//                    checkExistsJob?.cancel()
//                    clearPreview()
//                    if (!event.url.isNullOrBlank() && checkURL(event.url)) {
//                        checkExistsJob = checkBookmarkExists(event.url)
//                    }
//                }
//                updateBookmark(
//                    url = event.url,
//                    title = event.title,
//                    description = event.description,
//                    notes = event.notes,
//                    is_archived = event.is_archived,
//                    unread = event.unread,
//                    shared = event.shared,
//                )
//            }
//
//            is Event.UpdateTagNames -> updateTagNames(event.tagNames)
//            is Event.SelectTagPrediction -> selectTagPrediction(event.tagName)
//        }
//    }
}
