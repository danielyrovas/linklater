package org.yrovas.linklater

import android.util.Log
import androidx.annotation.Keep
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// private const val TIME_OUT = 60_000

class Ktor {
    companion object {
        val client = HttpClient(Android) {
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
    }
}
//
////            if (BuildConfig.DEBUG) {
//            install(Logging) {
//                level = LogLevel.ALL
//            }
////            }
//
////            if (BuildConfig.DEBUG) {
//            install(ResponseObserver) {
//                onResponse { response ->
//                    Log.i("HTTP status:", "${response.status.value}")
//                }
//            }
////            }
