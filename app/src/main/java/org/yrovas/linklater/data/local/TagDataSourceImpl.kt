package org.yrovas.linklater.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.yrovas.linklater.Database
import org.yrovas.linklater.domain.TagDataSource
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class TagDataSourceImpl(db: Database) : TagDataSource {
    private val q = db.bookmarkTagsQueries

    override suspend fun getTag(id: Long): String? {
        return withContext(Dispatchers.IO) {
            q.getTagByID(id).executeAsOneOrNull()?.name
        }

    }

    override fun getTags(): Flow<List<String>> {
        return q.getTagNames().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.mapNotNull { it.name }
        }
    }
}

