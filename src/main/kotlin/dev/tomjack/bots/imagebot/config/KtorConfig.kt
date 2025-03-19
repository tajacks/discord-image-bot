package dev.tomjack.bots.imagebot.config

import dev.tomjack.bots.imagebot.extension.required
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class KtorConfig {
    @Bean
    fun httpClient(environment: Environment): HttpClient =
        HttpClient(Java) {
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(
                            accessToken = environment.required(FIREWORKS_KEY_PROPERTY),
                            refreshToken = null,
                        )
                    }
                }
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            install(ContentNegotiation) {
                json()
            }
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3)
                exponentialDelay()
            }
        }

    companion object {
        private const val FIREWORKS_KEY_PROPERTY = "dev.tomjack.bots.imagebot.fireworks.token"
    }
}
