package org.yrovas.linklater

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
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
import org.yrovas.linklater.data.local.PrefDataStore
import org.yrovas.linklater.data.local.PrefStore
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

    private val store: DataStore<Preferences>
        get() = context.dataStore

    @Provides
    fun providePrefStore(): PrefDataStore = PrefStore(store)

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
}
