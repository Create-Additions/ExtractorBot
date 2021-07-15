package commands

import HandyDiscord.api
import base.HandyCommand
import base.Subscribe
import data.HandyPronouns
import org.javacord.api.entity.permission.Role
import org.javacord.api.interaction.*

@Subscribe
class PronounCommand : HandyCommand() {
    override fun register(): SlashCommandBuilder {
        return SlashCommandBuilder().setName("pronouns").setDescription("Add pronouns")
            .addOption(SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "pronouns", "What pronoun do you want to add?", true,
            getPronouns()))
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
        val chosenRole = ctx.firstOption.get().stringValue.get()
        val role = p.find { it.id.toString() == chosenRole }!!
        ctx.user.addRole(role)
        simpleUserOnlyResponse(ctx, "Role ${role.mentionTag} given")
    }
}