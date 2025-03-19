package dev.tomjack.bots.imagebot.eventhandlers

import discord4j.core.event.domain.lifecycle.ReadyEvent
import io.klogging.Klogging
import org.springframework.stereotype.Component

@Component
class ReadyEventHandler : EventHandler<ReadyEvent> {
    override fun eventType(): Class<ReadyEvent> = ReadyEvent::class.java

    override suspend fun handle(event: ReadyEvent) {
        logger.info { """Logged in as "${event.data.user().username()}" with ID ${event.data.user().id()}""" }
    }

    companion object : Klogging
}
