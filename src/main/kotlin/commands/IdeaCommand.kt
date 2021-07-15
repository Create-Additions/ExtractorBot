package commands

import base.HandyCommand
import base.Subscribe
import data.HandyConfig
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.interaction.*

@Subscribe
class IdeaCommand : HandyCommand() {
    override fun register(): SlashCommandBuilder =
        builder("idea", "Suggest an idea for an addon or modpack")
//            .addOption(SlashCommandOption.create(SlashCommandOptionType.STRING, "content", "What's your idea?"))
            .addOption(SlashCommandOptionBuilder().setType(SlashCommandOptionType.STRING).setName("content").setDescription("What's your idea?").setRequired(true).build())
    override fun onCalled(ctx: SlashCommandInteraction) {
        if(ctx.channel.get().id.toString() == HandyConfig.get().suggestionChannel) {
            ctx.createImmediateResponder().setContent("${ctx.user.mentionTag} suggested:").appendNewLine().append(ctx.firstOption.get().stringValue.get()).respond()
        } else {
            ctx.createImmediateResponder().setContent("You can only use this command in <#${HandyConfig.get().suggestionChannel}>!").setFlags(MessageFlag.EPHEMERAL).respond()
        }
    }
}