package handy.commands

import handy.base.HandyCommand
import handy.HandyDiscord.api
import handy.base.Subscribe
import handy.data.HandyPronouns
import org.javacord.api.entity.permission.Role
import org.javacord.api.interaction.*

@Subscribe
class PronounCommand : HandyCommand("pronouns") {
    override fun register(): SlashCommandBuilder {
        return SlashCommandBuilder().setName(id).setDescription("Add or remove pronouns")
            .addOption(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "add", "Add pronouns",
                listOf(SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "name","The pronouns to add", true, getPronouns()))))
            .addOption(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "remove", "Remove pronouns",
                listOf(SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "name","The pronouns to remove", true, getPronouns()))))
    }

    fun getPronouns(): List<SlashCommandOptionChoice> {
        return getPronounRoles().map {
            return@map SlashCommandOptionChoiceBuilder().setName(it.name).setValue(it.id.toString()).build()
        }
    }

    fun getPronounRoles(): List<Role> {
        return HandyPronouns.get().pronouns.map { api!!.getRoleById(it.id).get() }
    }

    override fun onCalled(ctx: SlashCommandInteraction) {
        val p = getPronounRoles()
        val chosenRole = ctx.firstOption.get().firstOption.get().stringValue.get()
        val role = p.find { it.id.toString() == chosenRole }!!
        val add = ctx.firstOption.get().name.equals("add")
        if(add) {
            ctx.user.addRole(role)
        } else {
            ctx.user.removeRole(role)
        }
        simpleUserOnlyResponse(ctx, "Role ${role.mentionTag} ${if(add) "given" else "removed"}")
    }
}