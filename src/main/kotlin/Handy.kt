import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.reflections.Reflections

object Handy {
        @JvmStatic
        fun main(args: Array<String>) {
            HandyDiscord.create()
    }
}
