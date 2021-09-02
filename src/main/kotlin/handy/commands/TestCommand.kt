package handy.commands

import handy.base.HandyCommand
import handy.base.SubscribeInitable
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.interaction.SlashCommandInteraction

@SubscribeInitable
class TestCommand : HandyCommand("test") {
    override fun register() = command(description = "Test the bot") {
        permissions {
            default = true
        }
    }

    override fun onCalled(ctx: SlashCommandInteraction) {
        ctx.createImmediateResponder()
            .setFlags(MessageFlag.EPHEMERAL)
            .setContent("I'm here!")
            .respond()
    }
}