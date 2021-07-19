package handy.commands

import handy.base.HandyCommand
import handy.base.Subscribe
import handy.data.HandyConfig
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.entity.message.component.ActionRow
import org.javacord.api.entity.message.component.Button
import org.javacord.api.event.interaction.MessageComponentCreateEvent
import org.javacord.api.interaction.*

@Subscribe
class ApplyCommand : HandyCommand("apply") {
    enum class ApplicationType(val description: String) {
        MOD("Apply with your mod"),
        MODPACK("Apply with your modpack");

        val id: Int = lastTypeId++
    }

    companion object {
        var lastTypeId = 0
    }

    override fun register(): SlashCommandBuilder {
        val b = builder(description = "Apply for a developer role")
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

    fun acceptComponent(roleId: String, event: MessageComponentCreateEvent, interaction: MessageComponentInteraction) {
        val forUser = interaction.message.get().mentionedUsers[0]
        val d = HandyConfig.get().devRole
        forUser.addRole(event.api.getRoleById(d).get()).join()
        forUser.addRole(event.api.getRoleById(roleId).get()).join()
        interaction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL)
            .setContent("Given ${forUser.mentionTag} roles <@&${d}> and <@&${roleId}>")
            .respond()
    }

    val acceptCoderComponent = addComponentSubscriber { e, i ->
        acceptComponent(HandyConfig.get().coderRole, e, i)
    }
    val acceptArtistComponent = addComponentSubscriber { e, i ->
        acceptComponent(HandyConfig.get().artRole, e, i)
    }
    val acceptPackerComponent = addComponentSubscriber { e, i ->
        acceptComponent(HandyConfig.get().packRole, e, i)
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
        fun site(v2: String, n: String) {
            val v = o.getOptionByName(v2)
            if(v.isPresent) {
                links[n] = v.get().stringValue.get()
            }
        }
        fun site(n: String) = site(n.capitalize(), n)
        if(discord.isPresent) {
            val v = discord.get().stringValue.get()
            b.appendNewLine().append("Discord: $v")
        }
        site("website")
        site("github")
        site("CurseForge", "curseforge")
        if(links.isNotEmpty()) {
            b.addComponents(ActionRow.of(
                *links.map { Button.link(it.value, it.key) }.toTypedArray()
            ))
        }
        b.addComponents(ActionRow.of(
            Button.secondary(acceptCoderComponent, "Add coder role"),
            Button.secondary(acceptArtistComponent, "Add artist role"),
            Button.secondary(acceptPackerComponent, "Add modpack role")
        ))
        simpleUserOnlyResponse(ctx, "Sending application")
        b.send(appChannel).handle { message: Message?, throwable: Throwable? ->
                    ctx.createFollowupMessageBuilder().setFlags(MessageFlag.EPHEMERAL)
                        .setContent(if(throwable != null) "Unable to send application (is one of the links invalid?)" else "Application sent").send()
        }.get().get()
    }
}

