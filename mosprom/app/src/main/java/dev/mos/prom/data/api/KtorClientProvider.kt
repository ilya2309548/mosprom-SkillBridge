package dev.mos.prom.data.api

import dev.mos.prom.data.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class KtorClientProvider(
    private val baseHost: String,
    private val basePort: Int,
    private val tokenStorage: TokenStorage,
) {
    val client: HttpClient by lazy {
        HttpClient(Android) {
            expectSuccess = true
            install(Logging) {
                level = LogLevel.ALL
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        // Print to console/Logcat
                        println("Ktor: $message")
                    }
                }
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = false
                        encodeDefaults = true
                    }
                )
            }
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTP
                    host = baseHost
                    port = basePort
                }
                val token = kotlinx.coroutines.runBlocking { tokenStorage.getAccessToken() }
                if (!token.isNullOrBlank()) {
                    headers.append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        }
    }
}
