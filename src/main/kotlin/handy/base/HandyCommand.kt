package handy.base

import handy.HandyDiscord.api
import handy.data.HandyConfig
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.entity.server.Server
import org.javacord.api.event.interaction.MessageComponentCreateEvent
import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.interaction.*
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder
import java.util.function.BiConsumer
import java.util.function.Consumer

abstract class HandyCommand(val id: String) : Initable {
    companion object {
        private var lastComponentId = 0
        private val components = hashMapOf<String, BiConsumer<MessageComponentCreateEvent, MessageComponentInteraction>>()

        fun registerComponentSubscriber() {
            api!!.addMessageComponentCreateListener {
                val interaction = it.messageComponentInteraction
                if(components.containsKey(interaction.customId)) {
                    components[interaction.customId]!!.accept(it, interaction)
                }
            }
        }
    }

    var command: SlashCommand? = null

    override fun init() {
        val b = register()
        command = ((if (HandyConfig.get().isDev) b.createForServer(api!!.getServerById(HandyConfig.get().mainServer).get()) else b.createGlobal(api))).join()
        api!!.addSlashCommandCreateListener { event: SlashCommandCreateEvent ->
            val slashCommandInteraction = event.slashCommandInteraction
            if (slashCommandInteraction.commandName == command!!.name) {
                onCalled(slashCommandInteraction)
            }
        }
        afterCommandRegistered()
    }

    open fun afterCommandRegistered() {}

    fun setCommandPermissions(server: Server, vararg p: SlashCommandPermissions, o: (SlashCommandPermissionsUpdater) -> SlashCommandPermissionsUpdater = { i:SlashCommandPermissionsUpdater -> i}) {
        o(SlashCommandPermissionsUpdater(server).setPermissions(p.toMutableList())).update(command!!.id)
    }

    fun simpleCommand(name: String = id, description: String): SlashCommandBuilder? {
        return SlashCommand.with(name, description)
    }

    fun updateCommand(transform: Consumer<SlashCommandUpdater>): SlashCommand {
        val u = SlashCommandUpdater(command!!.id)
        transform.accept(u)
        command = (if(HandyConfig.get().isDev) u.updateForServer(getServer()) else u.updateGlobal(api!!)).join()
        return command!!
    }

    fun getServer() = api!!.getServerById(HandyConfig.get().mainServer).get()

    fun addComponentSubscriber(name: String = "", onPress: BiConsumer<MessageComponentCreateEvent, MessageComponentInteraction>): String {
        val id = name + lastComponentId++
        components[id] = onPress
        return id
    }

    fun simpleResponse(ctx: SlashCommandInteraction, content: String, transform: Consumer<InteractionImmediateResponseBuilder>? = null) {
        val b = ctx.createImmediateResponder().setContent(content)
        transform?.accept(b)
        b.respond()
    }

    fun simpleUserOnlyResponse(ctx: SlashCommandInteraction, content: String, transform: Consumer<InteractionImmediateResponseBuilder>? = null) {
        simpleResponse(ctx, content) { b ->
            b.setFlags(MessageFlag.EPHEMERAL)
            transform?.accept(b)
        }
    }

    fun getNextComponentId() = lastComponentId++

    fun builder(name: String = id, description: String) = SlashCommandBuilder().setName(name).setDescription(description)

    abstract fun register(): SlashCommandBuilder
    abstract fun onCalled(ctx: SlashCommandInteraction)
}