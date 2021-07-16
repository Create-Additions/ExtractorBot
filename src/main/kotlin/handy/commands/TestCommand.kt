package handy.commands

import handy.base.HandyCommand
import handy.base.Subscribe
import org.javacord.api.entity.message.MessageFlag
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