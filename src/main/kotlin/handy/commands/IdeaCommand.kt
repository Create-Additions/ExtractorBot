package handy.commands

import handy.base.HandyCommand
import handy.HandyDiscord.api
import handy.base.SubscribeInitable
import handy.data.HandyConfig
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.interaction.*

@SubscribeInitable
class IdeaCommand : HandyCommand("suggestion") {
    override fun register(): SlashCommandBuilder =
        builder(description = "Suggest an idea for one of the mods")
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

    override fun init() {
        super.init()
        api!!.addMessageCreateListener {
            val ideaId = HandyConfig.get().ideaRole
            if(it.channel.idAsString == HandyConfig.get().suggestionChannel && it.message.mentionedRoles.any { r -> r.id.toString() == ideaId}) {
                it.message.addReactions("👍", "👎")
            }
        }
    }
}