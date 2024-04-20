package org.yrovas.linklater.ui.state

import androidx.lifecycle.ViewModel
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
import org.yrovas.linklater.domain.Err
import org.yrovas.linklater.domain.Ok
import org.yrovas.linklater.domain.Res
import org.yrovas.linklater.domain.TagDataSource
import org.yrovas.linklater.intoTags

@Inject
class SaveBookmarkScreenState(
    private val bookmarkAPI: BookmarkAPI,
    private val prefStore: PrefDataStore,
    private val tagSource: TagDataSource,
    private val bookmarkSource: BookmarkDataSource,
) :
    ViewModel() {
    private val _bookmarkToSave = MutableStateFlow(LocalBookmark(""))
    var bookmarkToSave = _bookmarkToSave.asStateFlow()

    private val _tagNames = MutableStateFlow("")
    var tagNames = _tagNames.asStateFlow()

    private val _tags: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    var tags = _tags.asStateFlow()

    private val _selectedTags = MutableStateFlow(emptyList<String>())
    var selectedTags = _selectedTags.asStateFlow()

    private val _showPaste = MutableStateFlow(bookmarkToSave.value.url.isBlank())
    var showPaste = _showPaste.asStateFlow()

    private val _submitResult: MutableStateFlow<Res<String, APIError>?> =
        MutableStateFlow(null)
    var submitResult = _submitResult.asStateFlow()

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

    fun updateTagNames(tagNames: String) {
        _tagNames.update { tagNames }
    }

    fun toggleSelectTag(tag: String) {
        _selectedTags.update {
            (if (it.contains(tag)) it - tag
            else it + tag).sorted().distinct()
        }
    }

    private fun setSubmitResult(result: Res<String, APIError>) =
        _submitResult.update { result }

    fun submitBookmark() {
        val tags =
            (bookmarkToSave.value.tags + selectedTags.value + tagNames.value.split(
                " "
            ).filter { it.isNotBlank() }).distinct()
        val bookmark =
            bookmarkToSave.value.withUpdates(tags = tags.ifEmpty { null })
        viewModelScope.launch(Dispatchers.IO) {
            setSubmitResult(
                when (val res = bookmarkAPI.saveBookmark(bookmark)) {
                    is Res.Err -> Err(res.error)
                    is Res.Ok -> {
                        bookmarkSource.insertBookmark(res.data)
                        Ok("Saved Bookmark to LinkDing")
                    }
                }
            )
        }
    }

    fun setTags(tagList: List<String>) = _tags.update { tagList.sorted() }

    fun validateBookmark(): Boolean {
        return checkURL(bookmarkToSave.value.url)
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            updateBookmark(
                is_archived = prefStore.getPref(
                    Prefs.BOOKMARK_DEFAULT_ARCHIVED, false
                ),
                unread = prefStore.getPref(Prefs.BOOKMARK_DEFAULT_UNREAD, false),
                shared = prefStore.getPref(Prefs.BOOKMARK_DEFAULT_SHARED, false)
            )

            viewModelScope.launch(Dispatchers.IO) {
                tagSource.getTags().collect {
                    setTags((tags.value + it).distinct())
                }
            }

            val tagList =
                prefStore.getPref(Prefs.BOOKMARK_DEFAULT_TAG_NAMES, "").intoTags()
            setTags(tags.value + tagList)
            tagList.forEach {
                toggleSelectTag(it)
            }
        }
    }
}
