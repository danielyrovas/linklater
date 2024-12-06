package org.yrovas.linklater.ui.screens.home

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.data.models.APIError
import org.yrovas.linklater.data.remote.BookmarkAPI
import org.yrovas.linklater.ui.screens.ScreenEffect
import org.yrovas.linklater.ui.screens.ScreenEvent
import org.yrovas.linklater.ui.screens.ScreenModel
import org.yrovas.linklater.ui.usecases.BookmarkQueryUseCase
import org.yrovas.linklater.ui.usecases.BookmarkSyncUseCase
import org.yrovas.linklater.ui.usecases.TagPredictUseCase

const val FIRST_POSSIBLE_DATE = "0000-01-01T00:00:00Z"

@Inject
class HomeModel(
    private val api: BookmarkAPI,
    private val tagPredictUseCase: TagPredictUseCase,
    private val bookmarkSyncUseCase: BookmarkSyncUseCase,
    private val bookmarkQueryUseCase: BookmarkQueryUseCase,
) : ScreenModel<HomeModel.Event, HomeModel.Effect>() {

    sealed interface Event : ScreenEvent {
        data object RefreshBookmarks : Event
        data class SelectTagPrediction(val tag: String) : Event
        data class SearchForTag(val tag: String) : Event
    }

    sealed interface Effect : ScreenEffect {
        data class RefreshError(val error: APIError) : Effect
    }

    init {
        bookmarkSyncUseCase.observeLocalBookmarks(viewModelScope)
        tagPredictUseCase.observeLocalTags(viewModelScope)
        refreshRemoteBookmarks()
    }

    private fun refreshRemoteBookmarks() = bookmarkSyncUseCase.fetchRemoteBookmarks(
        scope = viewModelScope,
        onRefreshStart = { _isRefreshing.update { true } },
        onRefreshComplete = { _isRefreshing.update { false } },
        onError = {
            sendEffect(Effect.RefreshError(it))
        })

    override fun handleEvent(event: Event) {
        when (event) {
            Event.RefreshBookmarks -> refreshRemoteBookmarks()
            is Event.SelectTagPrediction -> selectTagPrediction(event.tag)
            is Event.SearchForTag -> searchForTag(event.tag)
        }
    }

    private fun searchForTag(tag: String) {
        bookmarkQueryState.edit {
            replace(0, this.originalText.length, "#$tag")
        }
    }

    private fun selectTagPrediction(tag: String) {
        if (bookmarkQueryState.text.isBlank()) {
            return bookmarkQueryState.edit {
                append(tag)
            }
        }
        bookmarkQueryState.edit {
            val match = "#?\\w+$".toRegex().find(this.originalText)
            if (match != null) {
                replace(match.range.first, match.range.last + 1, "#$tag")
            }
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val bookmarkCount = bookmarkSyncUseCase.bookmarkCount
    val bookmarkQueryState = TextFieldState()

    @OptIn(FlowPreview::class)
    private val queryFlow = snapshotFlow { bookmarkQueryState.text.toString() }.debounce(20)

    val filteredBookmarks = bookmarkQueryUseCase.filteredBookmarks(
        bookmarkSyncUseCase.allBookmarks, queryFlow, viewModelScope
    )
    val filteredBookmarkCount =
        bookmarkQueryUseCase.filteredBookmarkCount(filteredBookmarks, queryFlow, viewModelScope)

    val predictedTags = tagPredictUseCase.predictedTags(queryFlow, viewModelScope)
}
