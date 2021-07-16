package handy

import handy.base.HandyCommand
import handy.data.HandyConfig
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder

object HandyDiscord {
    val token = HandyConfig.get().getToken()
    var api: DiscordApi? = null

    fun create() {
        api = DiscordApiBuilder().setToken(token).setAllIntents().login().join()
        HandySubscriptions.findAndSubscribe()
        HandyCommand.registerComponentSubscriber()
    }
}