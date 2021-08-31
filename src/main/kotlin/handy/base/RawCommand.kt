package handy.base

import handy.HandyDiscord.api
import handy.data.HandyConfig
import org.javacord.api.event.message.MessageCreateEvent

abstract class RawCommand(val id: String, var prefix: String = HandyConfig.get().prefix) : Initable {
    open fun getTriggers() = mutableListOf(prefix + id)

    override fun init() {
        this.getTriggers().forEach { commands[it] = this }
    }

    fun isOnServer(event: MessageCreateEvent) =
        event.server.isPresent

    abstract fun checkForPermissions(event: MessageCreateEvent): Boolean

    open fun start(event: MessageCreateEvent) {
        if(checkForPermissions(event)) {
            run(event)
        }
    }

    abstract fun run(event: MessageCreateEvent)

    @SubscribeInitable
    companion object : Initable {
        val commands = mutableMapOf<String, RawCommand>()

        override fun init() {
            api!!.addMessageCreateListener {
                val content = it.messageContent
                val c = commands.keys.firstOrNull { key -> content.startsWith(key) }
                commands[c]?.start(it)
            }
        }
    }
}