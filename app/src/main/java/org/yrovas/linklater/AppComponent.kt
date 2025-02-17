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
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Provides
import org.yrovas.linklater.data.local.PrefDataStore
import org.yrovas.linklater.data.local.PrefStore
import org.yrovas.linklater.data.local.Prefs
import org.yrovas.linklater.data.local.asSeverity
import org.yrovas.linklater.data.remote.BookmarkAPI
import org.yrovas.linklater.ui.screens.NavigationHost
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
abstract class AppComponent(
    @get:Provides val context: Context,
) {
    val store: DataStore<Preferences>
        @Provides get() = context.dataStore
    abstract val navigationHost: NavigationHost
    abstract val prefStore: PrefDataStore
    abstract val bookmarkAPI: BookmarkAPI
}

@Inject
@ContributesTo(AppScope::class)
interface AppModule {
    @SingleIn(AppScope::class)
    @Provides
    fun provideHttpClient(severityPreferences: SeverityPreferences): HttpClient = HttpClient(Android) {
        InitLog.d { "Creating Ktor HTTP Client with logging severity: ${severityPreferences.net.name}" }
        install(Logging) {
            logger = when (severityPreferences.net) {
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
                    override fun log(message: String) {
                        NetLog.w { message }
                    }
                }
            }
            level = when (severityPreferences.net) {
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

    @SingleIn(AppScope::class)
    @Provides
    fun provideSQLDriver(context: Context): SqlDriver {
        InitLog.v { "Creating SQL Driver" }
        return AndroidSqliteDriver(schema = Database.Schema,
            context = context,
            name = "linklater.db",
            callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.setForeignKeyConstraintsEnabled(true)
                }
            })
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideDB(driver: SqlDriver): Database {
        InitLog.v { "Creating SQL Database" }
        return Database(driver)
    }
}
