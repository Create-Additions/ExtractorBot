package handy.commands

import handy.base.HandyCommand
import handy.base.Subscribe
import org.javacord.api.entity.message.component.ActionRow
import org.javacord.api.entity.message.component.Button
import org.javacord.api.entity.permission.PermissionState
import org.javacord.api.entity.permission.PermissionType
import org.javacord.api.interaction.SlashCommandBuilder
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.interaction.SlashCommandOption
import org.javacord.api.interaction.SlashCommandOptionType

@Subscribe
class HahaFunnyButtonCommand : HandyCommand() {
    fun getText(user: String, times: String) = "Clicked $times times, last clicked by $user"
    fun getCount(content: String) = content.split(" ")[1].replace(",", "")

    override fun register(): SlashCommandBuilder {
        return builder("haha", "fun").addOption(
           SlashCommandOption.createWithOptions(
               SlashCommandOptionType.SUB_COMMAND_GROUP, "funny", "fun",
               listOf(SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "button", "yes"))
           )
        )
    }

    val button = addComponentSubscriber { event, interaction ->
        interaction.createOriginalMessageUpdater().setContent(getText(interaction.user.mentionTag, (getCount(interaction.message.get().content).toInt() + 1).toString()))
            .addComponents(ActionRow.of(
                Button.primary(interaction.customId, "haha funny button")
            )).update()
    }

    override fun onCalled(ctx: SlashCommandInteraction) {
        if(ctx.user.getRoles(ctx.server.get()).any {it.permissions.getState(PermissionType.MANAGE_CHANNELS).equals(PermissionState.ALLOWED)}) {
            simpleResponse(ctx, getText("nobody", "0")) {
                it.addComponents(ActionRow.of(
                    Button.primary(button, "haha funny button")
                ))
            }
        } else {
            simpleUserOnlyResponse(ctx, "Only people with permissions to manage channels can send the funny button")
        }
    }
}