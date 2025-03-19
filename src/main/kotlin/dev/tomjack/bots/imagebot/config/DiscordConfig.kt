package dev.tomjack.bots.imagebot.config

import dev.tomjack.bots.imagebot.commands.CommandRegistrar
import dev.tomjack.bots.imagebot.eventhandlers.EventHandler
import dev.tomjack.bots.imagebot.extension.required
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.Event
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import discord4j.rest.RestClient
import kotlinx.coroutines.reactor.mono
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import reactor.kotlin.core.publisher.toFlux

@Configuration
class DiscordConfig {
    @Bean
    @Profile("!test")
    fun gatewayDiscordClient(
        env: Environment,
        eventHandlers: List<EventHandler<*>>,
    ): GatewayDiscordClient =
        DiscordClientBuilder
            .create(env.required(DISCORD_TOKEN_PROPERTY))
            .build()
            .gateway()
            .setEnabledIntents(IntentSet.of(Intent.GUILD_MESSAGES))
            // .setInitialPresence() TODO -> Would be interesting to look into this
            .withEventDispatcher { dispatcher ->
                @Suppress("UNCHECKED_CAST")
                (eventHandlers as List<EventHandler<Event>>)
                    .toFlux()
                    .flatMap {
                        dispatcher.on(it.eventType()) { event -> mono { it.handle(event) } }
                    }
            }.login()
            .block()!! // TODO investigate this

    @Bean
    @Profile("!test")
    fun restDiscordClient(gatewayDiscordClient: GatewayDiscordClient): RestClient = gatewayDiscordClient.rest()

    @Bean
    @Profile("local && !test")
    fun commandRegistrarGuild(
        env: Environment,
        restClient: RestClient,
    ): CommandRegistrar =
        CommandRegistrar(
            restClient = restClient,
            guildId = env.required(DISCORD_GUILD_ID_KEY),
        )

    @Bean
    @Profile("!local && !test")
    fun commandRegistrarGlobal(restClient: RestClient): CommandRegistrar =
        CommandRegistrar(
            restClient = restClient,
            guildId = null,
        )

    companion object {
        private const val DISCORD_GUILD_ID_KEY = "dev.tomjack.bots.imagebot.discord.guildId"
        private const val DISCORD_TOKEN_PROPERTY = "dev.tomjack.bots.imagebot.discord.token"
    }
}
