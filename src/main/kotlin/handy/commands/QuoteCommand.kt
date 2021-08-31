package handy.commands

import handy.HandyDiscord.api
import handy.base.HandyCommand
import handy.base.LazyValue
import handy.base.RawCommand
import handy.base.Subscribe
import handy.data.HandyQuotes
import org.javacord.api.entity.channel.Channel
import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.interaction.*

@Subscribe
class QuoteCommand : RawCommand("quote") {
    init {
        instance = this
        SlashCommand().subscribe()
    }

    companion object {
        var instance: QuoteCommand? = null

        class SlashCommand : HandyCommand("quote") {
            override fun register(): SlashCommandBuilder =
                builder(description = "Quote a message" +
                        ", note: using ${instance!!.prefix + instance!!.id} also allows replying to the " +
                        "message you want to quote")
                    .addOption(SlashCommandOptionBuilder().setType(SlashCommandOptionType.STRING).setName("message")
                        .setDescription("Link to the message, " +
                            "if not provided the last message sent in the channel is quoted").setRequired(false).build())
//            override fun register(): SlashCommandBuilder =
//                builder(description = "Quote a message, " + f +
//                        ", note: using ${instance!!.prefix + instance!!.id} also allows replying to the " +
//                        "message you want to quote, so it's better to use that")
//                    .addOption(SlashCommandOptionBuilder().setType(SlashCommandOptionType.STRING).setName("message").setDescription(f.capitalize()).setRequired(false).build())

            override fun onCalled(ctx: SlashCommandInteraction) {
                val result = instance!!.commonRun(
                    ctx.getOptionByName("message").orElseGet { null }.stringValue.orElseGet { null },
                    null,
                    ctx.channel.get().asServerTextChannel().orElseGet { null },
                    ctx.user)
                if(result.second) simpleResponse(ctx, result.first)
                else simpleUserOnlyResponse(ctx, result.first)
            }
        }
    }
//    override fun register(): SlashCommandBuilder =
//        simpleCommand(description =  "Quote a message")!!
//            .addOption(SlashCommandOption.create(SlashCommandOptionType.STRING, "message",
//                "A link to a message or its ID, if not provided or invalid the last message is used", false))


    //    override fun onCalled(ctx: SlashCommandInteraction) {
//        val messageId = ctx.getOptionByName("message")
//        val message = if(messageId.isPresent) {
//            val id = messageId.get().stringValue.get();
//            if("discord.com/channels/" in id) {
//                api!!.getMessageByLink(id).orElse(null)?.get()
//            } else {
//                api!!.getMessageById(id, ctx.channel.get()).get()
//            }
//        } else null
//        if(message == null) {
//
//        }
//    }
    override fun checkForPermissions(event: MessageCreateEvent) = isOnServer(event)

    private fun errPair(message: String): Pair<String, Boolean> {
        return Pair(message, false)
    }

    fun commonRun(providedMessage: String?, referencedMessage: Message?, channel: ServerTextChannel?, user: User): Pair<String, Boolean> {
        var m: Message? = null
        if(providedMessage != null) {
            if("discord.com/channels/" in providedMessage) {
                m = api!!.getMessageByLink(providedMessage).orElse(null)?.get()
            }
        }
        if(m == null && referencedMessage != null) {
            m = referencedMessage
        }
        if(channel != null && providedMessage == null) {
            m = channel.getMessages(2).get().first()
        }
        if(m == null || m.channel?.canSee(user) == false) {
            return errPair("Could not find message! " +
                    "Try using this command while replying to another message or " +
                    "pass a message's link as an argument")
        }
        return if(HandyQuotes.INSTANCE.get().quotes.none { it.messageId == m.idAsString && it.channelId == m.channel.idAsString }) {
            HandyQuotes.INSTANCE.get().addAndSave(HandyQuotes.Quote(m.idAsString, m.channel.idAsString))
            Pair("Quoted ${m.author.discriminatedName} (${m.author.id}) \n" +
                    m.link, true)
        } else errPair("Quote ${m.channel.idAsString}/${m.idAsString} already exists")
    }

    override fun run(event: MessageCreateEvent) {
//        var args = event.messageContent.split(" ")
//        var providedMessage = args.minus(args.elementAt(0)).joinToString(" ")
        event.message.reply(commonRun(
            event.messageContent.split(" ").getOrNull(1),
            event.message.referencedMessage.orElseGet { null },
            event.channel.asServerTextChannel().orElseGet { null },
            event.messageAuthor.asUser().get()).first)
    }
}