package org.yrovas.linklater.data.local

import kotlinx.coroutines.flow.Flow
import org.yrovas.linklater.AppScope

interface TagDataSource {
    suspend fun getTag(id: Long): String?
    fun getTags(): Flow<List<String>>
    fun getRecentTags(): Flow<List<String>>
//    suspend fun insertTag(name: String)
//    suspend fun insertTags(names: List<String>)
//    suspend fun deleteTag(id: Long)
}
