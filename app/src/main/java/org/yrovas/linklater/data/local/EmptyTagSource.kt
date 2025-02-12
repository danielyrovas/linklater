package org.yrovas.linklater.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.yrovas.linklater.domain.TagDataSource

class EmptyTagSource() : TagDataSource {
    override suspend fun getTag(id: Long) = "Not-A-Tag"

    override fun getTags(): Flow<List<String>> {
        return listOf(listOf("Not-A-Tag", "Not-A-Two")).asFlow()
    }
}
