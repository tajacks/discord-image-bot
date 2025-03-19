package dev.tomjack.bots.imagebot.listeners

import discord4j.core.event.domain.Event

interface EventHandler<T : Event> {
    fun eventType(): Class<T>

    suspend fun handle(event: T)
}
