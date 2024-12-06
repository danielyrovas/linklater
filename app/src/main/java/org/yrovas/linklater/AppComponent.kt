package org.yrovas.linklater

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import co.touchlab.kermit.Severity
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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope
import org.yrovas.linklater.data.local.BookmarkDataSource
import org.yrovas.linklater.data.local.BookmarkDataSourceImpl
import org.yrovas.linklater.data.local.PrefStore
import org.yrovas.linklater.data.local.PrefStoreImpl
import org.yrovas.linklater.data.local.TagDataSource
import org.yrovas.linklater.data.local.TagDataSourceImpl
import org.yrovas.linklater.data.models.PREF_STORE_NAME
import org.yrovas.linklater.data.models.Prefs
import org.yrovas.linklater.data.remote.BookmarkAPI
import org.yrovas.linklater.data.remote.LinkDingAPI
import org.yrovas.linklater.ui.screens.NavigationHost
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FILE
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.LOCAL_VARIABLE
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER
import kotlin.annotation.AnnotationTarget.TYPE
import kotlin.annotation.AnnotationTarget.TYPEALIAS
import kotlin.annotation.AnnotationTarget.TYPE_PARAMETER
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

@Scope
@Target(
    CLASS,
    ANNOTATION_CLASS,
    TYPE_PARAMETER,
    PROPERTY,
    FIELD,
    LOCAL_VARIABLE,
    VALUE_PARAMETER,
    CONSTRUCTOR,
    FUNCTION,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    TYPE,
    FILE,
    TYPEALIAS
)
annotation class AppScope


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(PREF_STORE_NAME)

@AppScope
@Component
abstract class AppComponent(
    @get:Provides val context: Context,
) {
    protected val PrefStoreImpl.bind: PrefStore
        @Provides get() = this

    protected val BookmarkDataSourceImpl.bind: BookmarkDataSource
        @Provides get() = this

    protected val TagDataSourceImpl.bind: TagDataSource
        @Provides get() = this
    abstract val navigationHost: NavigationHost
    protected val LinkDingAPI.bind: BookmarkAPI
        @Provides get() = this

    val store: DataStore<Preferences>
        @Provides get() = context.dataStore

    @Provides
    @AppScope
    fun provideHttpClient(prefStore: PrefStore): HttpClient = HttpClient(Android) {
        val logSeverity = runBlocking {
            prefStore.getPref(Prefs.NET_LOG_SEVERITY, Severity.Verbose.ordinal).asSeverity()
        }

        Log.v { "Creating Ktor HTTP Client with logging severity: $logSeverity" }
        install(Logging) {
            logger = when (logSeverity) {
                Severity.Verbose -> object : Logger {
                    override fun log(message: String) {
                        NetLog.v { message }
                    }
                }

                Severity.Debug -> object : Logger {
                    override fun log(message: String) {
                        NetLog.d { message }
                    }
                }

                else -> object : Logger {
                    override fun log(message: String) {}
                }
            }
            level = when (logSeverity) {
                Severity.Verbose -> LogLevel.ALL
                Severity.Debug -> LogLevel.INFO
                else -> LogLevel.NONE
            }
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

    @AppScope
    @Provides
    fun provideSQLDriver(context: Context): SqlDriver {
        Log.v { "Creating SQL Driver" }
        return AndroidSqliteDriver(schema = Database.Schema,
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
        Log.v { "Creating SQL Database" }
        return Database(driver)
    }
}
