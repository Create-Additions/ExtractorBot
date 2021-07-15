package commands

import HandyDiscord.api
import base.HandyCommand
import base.Subscribe
import data.HandyConfig
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.entity.permission.Role
import org.javacord.api.interaction.*

@Subscribe
class IdeaCommand : HandyCommand() {
    override fun register(): SlashCommandBuilder =
        builder("idea", "Suggest an idea for an addon or modpack")
//            .addOption(SlashCommandOption.create(SlashCommandOptionType.STRING, "content", "What's your idea?"))
            .addOption(SlashCommandOptionBuilder().setType(SlashCommandOptionType.STRING).setName("content").setDescription("What's your idea?").setRequired(true).build())
    override fun onCalled(ctx: SlashCommandInteraction) {
        val c = ctx.api.getChannelById(HandyConfig.get().suggestionChannel)
        MessageBuilder()
            .append("${ctx.user.mentionTag} suggested:")
            .appendNewLine().append("<@&${HandyConfig.get().ideaRole}> ").append(ctx.firstOption.get().stringValue.get())
            .send(c.get().asTextChannel().get())
        simpleUserOnlyResponse(ctx, "Idea suggested")
    }

    override fun subscribe() {
        super.subscribe()
        api!!.addMessageCreateListener {
            val ideaId = HandyConfig.get().ideaRole
            if(it.channel.idAsString == HandyConfig.get().suggestionChannel && it.message.mentionedRoles.any { r -> r.id.toString() == ideaId}) {
                it.message.addReactions("👍", "👎")
            }
        }
    }
}