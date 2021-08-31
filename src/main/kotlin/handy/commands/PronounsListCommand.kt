package handy.commands

import handy.base.AdminCommand
import handy.base.SubscribeInitable
import handy.data.HandyPronouns
import org.javacord.api.interaction.*

@SubscribeInitable
class PronounsListCommand : AdminCommand("pronouns_list") {
    override fun createCommand(): SlashCommandBuilder {
        return builder(description = "test")
            .addOption(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "add", "Add pronouns to the list",
                listOf(SlashCommandOption.create(SlashCommandOptionType.ROLE, "role","The pronouns to add", true))))
            .addOption(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "remove", "Remove pronouns from the list",
                listOf(SlashCommandOption.createWithChoices(SlashCommandOptionType.ROLE, "role","The pronouns to remove", true))))
    }

    override fun onCalled(ctx: SlashCommandInteraction) {
        val chosenRole = ctx.firstOption.get().firstOptionRoleValue.get()
        val add = ctx.firstOption.get().name.equals("add")
        val list = HandyPronouns.get().pronouns
        if(add)
            list.add(HandyPronouns.Pronoun(chosenRole.id.toString()))
        else list.remove(list.filter { it.id.toLong() == chosenRole.id }.firstOrNull())
        HandyPronouns.get().save()
        // cry
        // i hate these slash command builders so much
        val instance = PronounCommand.INSTANCE!!
        instance.command!!.createSlashCommandUpdater().setSlashCommandOptions(
            listOf(
            SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "add", "Add pronouns",
                listOf(SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "name","The pronouns to add", true, instance.getPronouns()))),
            SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "remove", "Remove pronouns",
                listOf(SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "name","The pronouns to remove", true, instance.getPronouns())))))
            .updateForServer(getServer())
        simpleUserOnlyResponse(ctx, "Role ${chosenRole.mentionTag} ${if(add) "added to the list" else "removed from the list"}")
    }
}