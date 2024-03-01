package org.yrovas.linklater

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*
import org.yrovas.linklater.data.LocalBookmark

class SaveActivityState : ViewModel() {
    private var bookmarkAPI: BookmarkAPI = EmptyBookmarkAPI()

    private val _bookmarkToSave: MutableStateFlow<LocalBookmark> =
        MutableStateFlow(LocalBookmark(""))
    var bookmarkToSave = _bookmarkToSave.asStateFlow()
    fun updateBookmark(
        url: String? = null,
        title: String? = null,
        description: String? = null,
        notes: String? = null,
        is_archived: Boolean? = null,
        unread: Boolean? = null,
        shared: Boolean? = null,
        tag: List<String> = emptyList(),
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
    }

    private val _tagNames: MutableStateFlow<String> = MutableStateFlow("")
    var tagNames = _tagNames.asStateFlow()

    fun updateTagNames(tagNames: String) {
        _tagNames.update { tagNames }
    }

    private val _tags: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    var tags = _tags.asStateFlow()

    private val _selectedTags: MutableStateFlow<Set<String>> =
        MutableStateFlow(emptySet())
    var selectedTags = _selectedTags.asStateFlow()

    fun toggleSelectTag(tag: String) {
        _selectedTags.update {
            if (it.contains(tag)) it - tag
            else it + tag
        }
    }

    suspend fun submitBookmark(): Boolean {
        val tags =
            (bookmarkToSave.value.tags + selectedTags.value + tagNames.value.split(
                " "
            ).filter { it.isNotBlank() }).distinct()
        val bookmark =
            bookmarkToSave.value.withUpdates(tags = tags.ifEmpty { null })
        Log.d("DEBUG/save", "submitBookmark: $bookmark")
        return bookmarkAPI.saveBookmark(bookmark)
    }

    private val _submitResult: MutableStateFlow<Pair<String, Boolean>> = MutableStateFlow("" to true)
    var submitResult = _submitResult.asStateFlow()
    fun setSubmitResult(result: Pair<String, Boolean>) = _submitResult.update { result }
    fun setTags(tagList: List<String>) = _tags.update { tagList }

    suspend fun setup(context: Context) {
        var url = ""
        var token = ""
        context.dataStore.data.first { preferences ->
            url = preferences[Prefs.LINKDING_URL].orEmpty()
            token = preferences[Prefs.LINKDING_TOKEN].orEmpty()
            true
        }
        bookmarkAPI = LinkDingAPI(url, token)
        setTags(bookmarkAPI.getCachedTags(context))
    }

    fun validateBookmark(): Boolean {
        return checkURL(bookmarkToSave.value.url)
    }
}
