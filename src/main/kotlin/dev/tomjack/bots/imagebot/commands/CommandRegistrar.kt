package dev.tomjack.bots.imagebot.commands

import discord4j.common.JacksonResources
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.RestClient
import io.klogging.Klogging
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import kotlin.jvm.java

class CommandRegistrar(
    private val restClient: RestClient,
    private val guildId: Long?,
) {
    private val identifier =
        if (guildId != null) {
            "guild"
        } else {
            "global"
        }

    // Registers the commands either globally or within a guild upon initializing
    init {
        runBlocking {
            logger.info { "Registering $identifier commands" }
            try {
                val applicationId: Long = restClient.applicationId.awaitSingle()
                val commands: List<ApplicationCommandRequest> =
                    MATCHER
                        .getResources("commands/*.json")
                        .map { MAPPER.objectMapper.readValue(it.inputStream, ApplicationCommandRequest::class.java) }

                val registerOperation =
                    if (guildId != null) {
                        restClient.applicationService.bulkOverwriteGuildApplicationCommand(
                            applicationId,
                            guildId,
                            commands,
                        )
                    } else {
                        restClient.applicationService.bulkOverwriteGlobalApplicationCommand(applicationId, commands)
                    }
                registerOperation.awaitSingle()
                logger.info { "Successfully registered ${commands.size} $identifier commands" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to register $identifier commands on startup" }
            }
        }
    }

    companion object : Klogging {
        private val MAPPER = JacksonResources.create()
        private val MATCHER = PathMatchingResourcePatternResolver()
    }
}
