package org.yrovas.linklater.domain

import kotlinx.coroutines.flow.Flow
import org.yrovas.linklater.data.Bookmark

interface TagDataSource {
    suspend fun getTag(id: Long): String?
    fun getTags(): Flow<List<String>>
//    suspend fun insertTag(name: String)
//    suspend fun insertTags(names: List<String>)
//    suspend fun deleteTag(id: Long)
}
