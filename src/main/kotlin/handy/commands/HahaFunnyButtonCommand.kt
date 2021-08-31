package handy.commands

import handy.base.HandyCommand
import handy.base.SubscribeInitable
import handy.data.HandyConfig
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.entity.message.component.ActionRow
import org.javacord.api.entity.message.component.Button
import org.javacord.api.entity.permission.PermissionState
import org.javacord.api.entity.permission.PermissionType
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User
import org.javacord.api.interaction.*

@SubscribeInitable
class HahaFunnyButtonCommand : HandyCommand("haha") {
    fun getText(user: String, times: String, isOff: Boolean): String {
        var s = "Clicked $times times, last clicked by $user"
        if(isOff) {
            s += "_"
            s = "_$s"
        }
        return s
    }
    fun getCount(content: String) = content.split(" ")[1].replace(",", "")
    fun getUser(msg: Message) = msg.mentionedUsers[0]

    override fun register(): SlashCommandBuilder {
        return builder(description = "fun").addOption(
            SlashCommandOption.createWithOptions(
                SlashCommandOptionType.SUB_COMMAND_GROUP, "funny", "fun",
                listOf(SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "button", "yes"))
            )
        ).setDefaultPermission(false)
    }

    override fun afterCommandRegistered() {
        val s = getServer()
        setCommandPermissions(s,
            *s.roles.map {
                return@map if(canControlButton(listOf(it)))
                    SlashCommandPermissions.create(it.id, SlashCommandPermissionType.ROLE, true)
                else null
            }.filterNotNull().toTypedArray())
    }

    val button: String = addComponentSubscriber { event, interaction ->
        val isOff = isOff(interaction.message.get().content)
        if(HandyConfig.get().funnyButton && !isOff) {
            interaction.createOriginalMessageUpdater().setContent(getText(interaction.user.mentionTag, (getCount(interaction.message.get().content).toInt() + 1).toString(), isOff))
                .addComponents(ActionRow.of(
                    Button.primary(interaction.customId, "haha funny button"),
                    Button.secondary(toggle, "toggle funny button")
                )).update()
        } else {
            interaction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("The funny button is currently disabled :(").respond()
        }
    }

    fun isOff(m: String) = m.startsWith("_") && m.endsWith("_")

    fun canControlButton(roles: List<Role>) =
        roles.any {it.permissions.getState(PermissionType.MANAGE_CHANNELS).equals(PermissionState.ALLOWED)}

    fun canControlButton(user: User, server: Server) =
        canControlButton(user.getRoles(server))

    val toggle: String = addComponentSubscriber { event, interaction ->
        val str: String
        val isOff = isOff(interaction.message.get().content)
        if(canControlButton(interaction.user, interaction.server.get())) {
            str = "funny button ${if(isOff) "enabled" else "disabled"}"
            interaction.createOriginalMessageUpdater().addComponents(ActionRow.of(
                Button.primary(button, "haha funny button"),
                Button.secondary(interaction.customId, "toggle funny button")
            ))
                .setContent(getText(getUser(interaction.message.get()).mentionTag, getCount(interaction.message.get().content), !isOff)).update()
        } else {
            str = "you dont have enough perms to turn off the button and ruin the fun"
        }
        interaction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent(str).respond()
    }

    override fun onCalled(ctx: SlashCommandInteraction) {
        if(canControlButton(ctx.user, ctx.server.get())) {
            simpleResponse(ctx, getText("nobody", "0", false)) {
                it.addComponents(ActionRow.of(
                    Button.primary(button, "haha funny button"),
                    Button.secondary(toggle, "toggle funny button")
                ))
            }
        } else {
            simpleUserOnlyResponse(ctx, "Only people with permissions to manage channels can send the funny button")
        }
    }
}