package commands

import base.HandyCommand
import base.Subscribe
import data.HandyConfig
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.entity.message.component.ActionRow
import org.javacord.api.entity.message.component.Button
import org.javacord.api.exception.BadRequestException
import org.javacord.api.interaction.*

@Subscribe
class ApplyCommand : HandyCommand() {
    enum class ApplicationType(val description: String) {
        MOD("Apply with your mod"),
        MODPACK("Apply with your modpack");

        val id: Int = lastTypeId++
    }

    companion object {
        var lastTypeId = 0
    }

    override fun register(): SlashCommandBuilder {
        val b = builder("apply", "Apply for a developer role")
        ApplicationType.values().map {
            b.addOption(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, it.name, it.description, listOf(
                SlashCommandOption.create(SlashCommandOptionType.STRING, "application", "Describe what you're applying for", true),
                SlashCommandOption.create(SlashCommandOptionType.STRING, "discord", "A discord invite to the mod or pack, not required", false),
                SlashCommandOption.create(SlashCommandOptionType.STRING, "website", "The mod's or modpack's website, not required", false),
                SlashCommandOption.create(SlashCommandOptionType.STRING, "github", "The mod's or modpack's Github page, not required", false),
                SlashCommandOption.create(SlashCommandOptionType.STRING, "curseforge", "The mod's or modpack's CurseForge page, not required", false)
            )))
        }
//        b.addOption(SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "type", "The application type", true, ApplicationType.values().map {
//                return@map SlashCommandOptionChoice.create(it.name, it.id)
//            }))
        return b
    }

    override fun onCalled(ctx: SlashCommandInteraction) {
        val appChannel = ctx.api.getChannelById(HandyConfig.get().applyChannel).get().asServerTextChannel().get()
        val b = MessageBuilder().append("${ctx.user.mentionTag}'s application:")
        val o = ctx.firstOption.get()
        val content = o.firstOptionStringValue.get()
        val discord = o.getOptionByName("discord")
        val website = o.getOptionByName("website")
        val github = o.getOptionByName("github")
        val curseforge = o.getOptionByName("curseforge")
        val links = hashMapOf<String, String>()
        b.appendNewLine().append(content)
        if(discord.isPresent) {
            val v = discord.get().stringValue.get()
            b.appendNewLine().append("Discord: $v")
        }
        if(website.isPresent) {
            val v = website.get().stringValue.get()
//            b.appendNewLine().append("Website: $v")
            links["Website"] = v
        }
        if(github.isPresent) {
            val v = github.get().stringValue.get()
//            b.appendNewLine().append("Github: $v")
            links["Github"] = v
        }
        if(curseforge.isPresent) {
            val v = curseforge.get().stringValue.get()
//            b.appendNewLine().append("Curseforge: $v")
            links["Curseforge"] = v
        }
        if(links.isNotEmpty()) {
            b.addComponents(ActionRow.of(
                *links.map { Button.link(it.value, it.key) }.toTypedArray()
            ))
        }
        simpleUserOnlyResponse(ctx, "Application sent")
        b.send(appChannel).handle { message: Message?, throwable: Throwable ->
            ctx.createFollowupMessageBuilder().setFlags(MessageFlag.EPHEMERAL).setContent("Unable to send application (is one of the links invalid?)").send()
        }
    }
}

