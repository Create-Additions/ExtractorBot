package handy

import handy.base.HandyCommand
import handy.data.HandyConfig
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder

object HandyDiscord {
    var api: DiscordApi? = null

    fun create() {
        api = DiscordApiBuilder().setToken(HandyConfig.get().discordToken).setAllIntents().login().join()
        HandySubscriptions.findAndSubscribe()
        HandyCommand.registerComponentSubscriber()
    }
}