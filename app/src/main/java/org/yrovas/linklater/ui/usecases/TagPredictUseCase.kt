package org.yrovas.linklater.ui.usecases

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.data.local.TagDataSource
import org.yrovas.linklater.data.models.Bookmark

@Inject
class TagPredictUseCase(
    private val tagSource: TagDataSource,
) {

    private val _tags: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    val tags = _tags.asStateFlow()

    fun observeLocalTags(scope: CoroutineScope) = scope.launch(Dispatchers.IO) {
        tagSource.getTags().collect {
            _tags.emit(it)
        }
    }

    fun predictedTags(queryFlow: Flow<String>, scope: CoroutineScope): StateFlow<List<String>> =
        combine(
            tags,
            queryFlow,
        ) { tags, query ->
            if (query.isBlank()) {
                tags
            } else {
                val partialTag = query.trim().split("\\s+".toRegex()).last()
                tags.filter {
                    it.startsWith(
                        if (partialTag.startsWith("#")) partialTag.drop(1) else partialTag,
                        ignoreCase = true
                    )
                }
            }
        }.stateIn(
            scope = scope, started = SharingStarted.WhileSubscribed(), initialValue = tags.value
        )

    fun recentTags(
        bookmarkFlow: StateFlow<List<Bookmark>>,
        scope: CoroutineScope): StateFlow<List<String>> =
        combine(
            bookmarkFlow,
            tags,
        ) { bookmarkFlow, tags ->
            bookmarkFlow.flatMap { it.tags }.distinct().take(10)
        }.stateIn(
            scope = scope, started = SharingStarted.WhileSubscribed(), initialValue = emptyList()
        )
}
