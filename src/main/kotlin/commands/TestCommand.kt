package commands

import base.HandyCommand
import base.Subscribe
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.interaction.SlashCommand
import org.javacord.api.interaction.SlashCommandBuilder
import org.javacord.api.interaction.SlashCommandInteraction

@Subscribe
class TestCommand : HandyCommand() {
    override fun register() = simpleCommand("test", "Test the bot")!!.setDefaultPermission(true)!!

    override fun onCalled(ctx: SlashCommandInteraction) {
        ctx.createImmediateResponder()
            .setFlags(MessageFlag.EPHEMERAL)
            .setContent("I'm here!")
            .respond()
    }
}