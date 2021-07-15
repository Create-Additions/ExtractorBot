import data.HandyConfig
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.event.interaction.SlashCommandCreateEvent

object HandyDiscord {
    val token = HandyConfig.get().getToken()
    var api: DiscordApi? = null

    fun create() {
        api = DiscordApiBuilder().setToken(token).setAllIntents().login().join()
        HandySubscriptions.findAndSubscribe()
    }
}