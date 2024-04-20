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
import org.yrovas.linklater.data.local.BookmarkDataSourceImpl
import org.yrovas.linklater.data.local.PrefDataStore
import org.yrovas.linklater.data.local.PrefStore
import org.yrovas.linklater.data.local.TagDataSourceImpl
import org.yrovas.linklater.data.remote.LinkDingAPI
import org.yrovas.linklater.domain.BookmarkAPI
import org.yrovas.linklater.domain.BookmarkDataSource
import org.yrovas.linklater.domain.TagDataSource
import org.yrovas.linklater.ui.common.DestinationHost

const val TAG = "DEBUG/create"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

@Scope
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER
)
annotation class AppScope

@Component
@AppScope
abstract class AppComponent(
    @get:Provides val context: Context,
) {
    abstract val destinationHost: DestinationHost
    abstract val prefStore: PrefDataStore

    val store: DataStore<Preferences>
        @AppScope @Provides get() = context.dataStore

    @AppScope
    @Provides
    fun providePrefStore(store: DataStore<Preferences>): PrefDataStore {
        Log.d(TAG, "providePrefStore: CREATE")
        return PrefStore(store)
    }

    @AppScope
    @Provides
    fun provideHttpClient(): HttpClient = HttpClient(Android) {
        Log.d(TAG, "provideHttpClient: CREATE")
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
        @AppScope
        @Provides get() = linkDingAPI

    abstract val bookmarkDataSource: BookmarkDataSource

    @AppScope
    @Provides
    fun provideBookmarkDataSource(db: Database): BookmarkDataSource =
        BookmarkDataSourceImpl(db)

    @AppScope
    @Provides
    fun provideTagDataSource(db: Database): TagDataSource = TagDataSourceImpl(db)

    @AppScope
    @Provides
    fun provideSQLDriver(context: Context): SqlDriver {
        Log.d(TAG, "provideSQLDriver: CREATE")
        return AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = "linklater.db",
            callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.setForeignKeyConstraintsEnabled(true)
                }
            })
    }

    @AppScope
    @Provides
    fun provideDB(driver: SqlDriver): Database {
        Log.d(TAG, "provideDB: CREATE")
        return Database(driver)
    }
}
