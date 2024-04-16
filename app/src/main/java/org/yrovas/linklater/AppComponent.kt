package org.yrovas.linklater

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope
import org.yrovas.linklater.data.local.BookmarkDataSource
import org.yrovas.linklater.data.local.BookmarkDataSourceImpl
import org.yrovas.linklater.data.local.PrefDataStore
import org.yrovas.linklater.data.local.PrefStore
import org.yrovas.linklater.data.local.TagDataSource
import org.yrovas.linklater.data.local.TagDataSourceImpl
import org.yrovas.linklater.data.remote.BookmarkAPI
import org.yrovas.linklater.data.remote.LinkDingAPI
import org.yrovas.linklater.ui.activity.DestinationHost

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

@Scope
annotation class AppScope

@Component
@AppScope
abstract class AppComponent(
    @get:Provides val context: Context,
) {
    abstract val destinationHost: DestinationHost
    abstract val prefStore: PrefDataStore

    val store: DataStore<Preferences>
        @Provides get() = context.dataStore

    @Provides
    fun providePrefStore(store: DataStore<Preferences>): PrefDataStore =
        PrefStore(store)

    @Provides
    fun provideHttpClient(): HttpClient = HttpClient(Android) {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.i("Ktor =>", message)
                }
            }
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }

    abstract val linkDingAPI: LinkDingAPI
    val bookmarkAPI: BookmarkAPI
        @Provides get() = linkDingAPI

    abstract val bookmarkDataSource: BookmarkDataSource

    @Provides
    fun provideBookmarkDataSource(db: Database): BookmarkDataSource =
        BookmarkDataSourceImpl(db)

    @Provides
    fun provideTagDataSource(db: Database): TagDataSource = TagDataSourceImpl(db)

    @Provides
    fun provideSQLDriver(context: Context): SqlDriver =
        AndroidSqliteDriver(schema = Database.Schema,
            context = context,
            name = "linklater.db",
            callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.setForeignKeyConstraintsEnabled(true)
                }
            })

    @Provides
    fun provideDB(driver: SqlDriver): Database = Database(driver)
}
