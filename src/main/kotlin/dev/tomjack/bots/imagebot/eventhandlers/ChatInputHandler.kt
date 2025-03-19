package dev.tomjack.bots.imagebot.eventhandlers

import dev.tomjack.bots.imagebot.client.FireworksClient
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.spec.InteractionFollowupCreateSpec
import io.klogging.Klogging
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import kotlin.jvm.optionals.getOrNull

@Component
class ChatInputHandler(
    private val fireworksClient: FireworksClient,
) : EventHandler<ChatInputInteractionEvent> {
    override fun eventType(): Class<ChatInputInteractionEvent> = ChatInputInteractionEvent::class.java

    override suspend fun handle(event: ChatInputInteractionEvent) {
        if (event.interaction.user.isBot) return
        if (event.commandName != DRAW_COMMAND_NAME) return

        val prompt: String? =
            event
                .getOption(DRAW_COMMAND_PROMPT)
                .flatMap { it.value }
                .map { it.asString() }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?.trim()

        if (prompt == null) {
            event.reply(DRAW_COMMAND_BAD_PROMPT).awaitSingle()
            return
        }

        event.deferReply().awaitSingleOrNull()

        logger.info { "Drawing image with prompt $prompt from user ${event.interaction.user.username}" }

        val imageData: ByteArray =
            try {
                fireworksClient.getImage(prompt)
            } catch (e: Exception) {
                logger.error(e) { "Exception when drawing image with prompt $prompt" }
                event.createFollowup(DRAW_COMMAND_EXCEPTION).awaitSingle()
                return
            }

        val followUpSpec =
            InteractionFollowupCreateSpec
                .builder()
                .content("""Here's your image with the prompt "$prompt".""")
                .addFile("drawn_image.jpg", ByteArrayInputStream(imageData))
                .build()

        event.createFollowup(followUpSpec).awaitSingle()
    }

    companion object : Klogging {
        private const val DRAW_COMMAND_NAME = "draw"
        private const val DRAW_COMMAND_PROMPT = "prompt"

        private const val DRAW_COMMAND_BAD_PROMPT =
            "Invalid prompt string. A valid prompt must be provided and not empty."
        private const val DRAW_COMMAND_EXCEPTION =
            "I'm truly sorry, I ran into an issue generating that image. Please try again."
    }
}
