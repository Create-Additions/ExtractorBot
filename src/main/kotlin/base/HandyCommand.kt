package base

import HandyDiscord.api
import data.HandyConfig
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.interaction.SlashCommand
import org.javacord.api.interaction.SlashCommandBuilder
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.interaction.SlashCommandUpdater
import java.util.function.Consumer

abstract class HandyCommand() : Subscribable {
    var command: SlashCommand? = null

    override fun subscribe() {
        val b = register()
        command = ((if (HandyConfig.get().isDev()) b.createForServer(api!!.getServerById(HandyConfig.get().getServer()).get()) else b.createGlobal(api))).join()
        api!!.addSlashCommandCreateListener { event: SlashCommandCreateEvent ->
            val slashCommandInteraction = event.slashCommandInteraction
            if (slashCommandInteraction.commandName == command!!.name) {
                onCalled(slashCommandInteraction)
            }
        }
    }

    fun simpleCommand(name: String, description: String): SlashCommandBuilder? {
        return SlashCommand.with(name, description)
    }

    fun updateCommand(transform: Consumer<SlashCommandUpdater>): SlashCommand {
        val u = SlashCommandUpdater(command!!.id)
        transform.accept(u)
        command = (if(HandyConfig.get().isDev()) u.updateForServer(api!!.getServerById(HandyConfig.get().getServer()).get()) else u.updateGlobal(api!!)).join()
        return command!!
    }

    fun simpleResponse(ctx: SlashCommandInteraction, content: String) {
        ctx.createImmediateResponder().setContent(content).respond()
    }

    fun simpleUserOnlyResponse(ctx: SlashCommandInteraction, content: String) {
        ctx.createImmediateResponder().setContent(content).setFlags(MessageFlag.EPHEMERAL).respond()
    }

    fun builder(name: String, description: String) = SlashCommandBuilder().setName(name).setDescription(description)

    abstract fun register(): SlashCommandBuilder
    abstract fun onCalled(ctx: SlashCommandInteraction)
}