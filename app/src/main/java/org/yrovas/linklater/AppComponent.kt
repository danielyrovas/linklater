package org.yrovas.linklater

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
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
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Provides
import org.yrovas.linklater.data.local.PrefDataStore
import org.yrovas.linklater.data.remote.BookmarkAPI
import org.yrovas.linklater.ui.screens.NavigationHost
import org.yrovas.linklater.ui.screens.home.HomeScreen
import org.yrovas.linklater.ui.screens.preferences.PreferencesScreen
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkActivityScreen
import org.yrovas.linklater.ui.screens.saveBookmark.SaveBookmarkScreen
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

const val TAG = "DEBUG/create"
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
    abstract val homeScreen: HomeScreen
    abstract val preferencesScreen: PreferencesScreen
    abstract val saveBookmarkScreen: SaveBookmarkScreen
    abstract val saveBookmarkActivityScreen: SaveBookmarkActivityScreen

    @SingleIn(AppScope::class)
    @Provides
    fun provideNav(): NavHostController {
        Log.d(TAG, "provideNav: CREATE")
        return NavHostController(context)
    }
}

@Inject
@ContributesTo(AppScope::class)
interface AppModule {
    @SingleIn(AppScope::class)
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

    @SingleIn(AppScope::class)
    @Provides
    fun provideSQLDriver(context: Context): SqlDriver {
        Log.d(TAG, "provideSQLDriver: CREATE")
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
        Log.d(TAG, "provideDB: CREATE")
        return Database(driver)
    }
}
