package org.yrovas.linklater.ui.screens.saveBookmark

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.mapBoth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
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
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.Log
import org.yrovas.linklater.checkURL
import org.yrovas.linklater.data.local.BookmarkDataSource
import org.yrovas.linklater.data.local.PrefStore
import org.yrovas.linklater.data.local.TagDataSource
import org.yrovas.linklater.data.models.APIError
import org.yrovas.linklater.data.models.LocalBookmark
import org.yrovas.linklater.data.models.Prefs
import org.yrovas.linklater.data.remote.BookmarkAPI
import org.yrovas.linklater.intoTags
import org.yrovas.linklater.ui.screens.ScreenEffect
import org.yrovas.linklater.ui.screens.ScreenEvent
import org.yrovas.linklater.ui.screens.ScreenModel
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkModel.Effect
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkModel.Event
import org.yrovas.linklater.ui.usecases.TagPredictUseCase
import kotlin.uuid.ExperimentalUuidApi

@Serializable
sealed class BookmarkParam {
    @Serializable
    data class Save(val url: String) : BookmarkParam()

    @Serializable
    data class Edit(val id: Long) : BookmarkParam()

    @Serializable
    data object New : BookmarkParam()
}

@OptIn(ExperimentalUuidApi::class)
@Inject
class SaveBookmarkModel(
    @Assisted private val bookmarkParam: BookmarkParam,
    private val bookmarkAPI: BookmarkAPI,
    private val tagPredictUseCase: TagPredictUseCase,
    private val tagSource: TagDataSource,
    private val prefStore: PrefStore,
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

    override fun handleEvent(event: Event) {
        when (event) {
            is Event.SubmitBookmark -> submitBookmark()
            is Event.ToggleShared -> _bookmarkShared.update { event.shared }
            is Event.ToggleUnread -> _bookmarkUnread.update { event.unread }
            is Event.SelectTagPrediction -> selectTagPrediction(event.tag)
            is Event.PasteURL -> {
                bookmarkURL.edit {
                    replace(0, bookmarkURL.text.length, event.url)
                }
                launchCheckExistsJob(event.url)
            }

            is Event.ToggleSelectTag -> setSelectedTags(
                if (selectedTags.value.contains(event.tag)) selectedTags.value - event.tag
                else selectedTags.value + event.tag
            )
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
    private val _bookmarkArchived = MutableStateFlow(false)
    private val bookmarkArchived = _bookmarkArchived.asStateFlow()

    private fun selectTagPrediction(tag: String) {
        setSelectedTags(selectedTags.value + tag)
        bookmarkTagNames.edit {
            val match = "#?\\w+$".toRegex().find(this.originalText)
            if (match != null) {
                delete(match.range.first, match.range.last + 1)
            }
        }
    }

    private val _selectedTags = MutableStateFlow(emptyList<String>())
    val selectedTags = _selectedTags.asStateFlow()
    private fun setSelectedTags(tags: List<String>) {
        _selectedTags.update { tags.sortedBy { tag -> tag.lowercase() }.distinct() }
    }

    val moreTags: StateFlow<List<String>> =
        combine(tagSource.getRecentTags(30), selectedTags) { tags, selected ->
            tags - selected
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    val recentTags: StateFlow<List<String>> =
        combine(tagSource.getRecentTags(10), selectedTags) { recent, selected ->
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

    @OptIn(FlowPreview::class)
    private val bookmarkURLFlow = snapshotFlow { bookmarkURL.text.toString() }.debounce(500)

    private val _previewTitle: MutableStateFlow<String?> = MutableStateFlow(null)
    val previewTitle = _previewTitle.asStateFlow()

    private val _previewDescription: MutableStateFlow<String?> = MutableStateFlow(null)
    val previewDescription = _previewDescription.asStateFlow()

    private fun submitBookmark() {
        if (!checkURL(bookmarkURL.text.toString())) {
            sendEffect(Effect.InvalidBookmark(message = "Invalid URL"))
            return
        }
        _isSubmitting.update { true }
        checkExistsJob?.second?.cancel()
        val bookmark = LocalBookmark(
            url = bookmarkURL.text.toString(),
            tags = (selectedTags.value + bookmarkTagNames.text.toString().intoTags()).distinct(),
            title = bookmarkTitle.text.toString(),
            description = bookmarkDescription.text.toString(),
            unread = bookmarkUnread.value,
            shared = bookmarkShared.value,
            is_archived = bookmarkArchived.value,
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

    init {
        when (bookmarkParam) {
            BookmarkParam.New -> {
                Log.d { "New bookmark" }
            }

            is BookmarkParam.Edit -> viewModelScope.launch {
                Log.d { "Loading bookmark by id: ${bookmarkParam.id}" }
                bookmarkSource.getBookmark(bookmarkParam.id)?.url?.let {
                    sendEvent(Event.PasteURL(it))
                }
            }

            is BookmarkParam.Save -> {
                Log.d { "Loading bookmark by url: ${bookmarkParam.url}" }
                sendEvent(Event.PasteURL(bookmarkParam.url))
            }
        }
        tagPredictUseCase.observeLocalTags(viewModelScope)
        setBookmarkDefaults()
        viewModelScope.launch {
            bookmarkURLFlow.collect {
                launchCheckExistsJob(it)
            }
        }
    }

    private fun launchCheckExistsJob(url: String) {
        if (bookmarkURL.text.isNotBlank() && checkURL(bookmarkURL.text.toString())) {
            if (checkExistsJob?.first != url) {
                checkExistsJob?.second?.let {
                    Log.d { "Cancelling job" }
                    it.cancel()
                }
                checkExistsJob = checkBookmarkExists(bookmarkURL.text.toString())
            } else {
                Log.d { "Most recent job already checked: $url" }
            }
        } else {
            clearPreview()
        }
    }

    private fun clearPreview() {
        Log.d { "Clearing preview" }
        _bookmarkExists.update { false }
        _previewTitle.update { null }
        _previewDescription.update { null }
    }

    fun setBookmarkDefaults() {
        viewModelScope.launch {
            _bookmarkUnread.emit(
                prefStore.getPref(Prefs.BOOKMARK_DEFAULT_UNREAD, false)
            )
            _bookmarkShared.emit(
                prefStore.getPref(Prefs.BOOKMARK_DEFAULT_SHARED, false)
            )
            _selectedTags.emit(
                (_selectedTags.value + prefStore.getPref(Prefs.BOOKMARK_DEFAULT_TAG_NAMES, "")
                    .intoTags()).sortedBy { it.lowercase() }.distinct()
            )
        }
    }

    private var checkExistsJob: Pair<String, Job>? = null
    private fun checkBookmarkExists(url: String): Pair<String, Job> {
        return url to viewModelScope.launch {
            bookmarkAPI.waitForAuth()
            clearPreview()

            val (b, m) = bookmarkAPI.checkExists(url)
            m?.title?.let { title ->
                _previewTitle.update { title }
            }
            m?.description?.let { description ->
                _previewDescription.update { description }
            }

            if (b != null) {
                Log.d { "Found existing bookmark for ${b.url}" }
                _bookmarkExists.update { true }
                bookmarkURL.edit {
                    replace(0, bookmarkURL.text.length, b.url)
                }
                bookmarkTitle.edit {
                    b.title?.let { replace(0, bookmarkTitle.text.length, it) }
                }
                bookmarkDescription.edit {
                    b.description?.let { replace(0, bookmarkDescription.text.length, it) }
                }
                bookmarkNotes.edit {
                    b.notes?.let { replace(0, bookmarkNotes.text.length, it) }
                }
                _bookmarkShared.update { b.shared }
                _bookmarkUnread.update { b.unread }
                _bookmarkArchived.update { b.is_archived }

                // guard against tags not being present in database ??
                setSelectedTags(b.tags)
                bookmarkTagNames.edit {
                    delete(0, bookmarkTagNames.text.length)
                }
            } else {
                _bookmarkExists.update { false }
            }
        }
    }
}
