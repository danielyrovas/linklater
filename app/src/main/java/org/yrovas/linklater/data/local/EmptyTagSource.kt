package org.yrovas.linklater.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class EmptyTagSource() : TagDataSource {
    override suspend fun getTag(id: Long) = "Not-A-Tag"

    override fun getTags(): Flow<List<String>> {
        return listOf(listOf("Not-A-Tag", "Not-A-Two")).asFlow()
    }

    override fun getRecentTags(count: Long): Flow<List<String>> {
        return listOf(listOf("Not-A-Tag")).asFlow()
    }
}
