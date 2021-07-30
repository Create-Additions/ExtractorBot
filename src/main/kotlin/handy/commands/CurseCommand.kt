package handy.commands

import com.therandomlabs.curseapi.CurseAPI
import com.therandomlabs.curseapi.project.CurseProject
import handy.base.AdminCommand
import handy.base.Subscribe
import handy.data.HandyMods
import okhttp3.HttpUrl
import org.javacord.api.interaction.*
import org.javacord.api.interaction.SlashCommandOption.*
import org.javacord.api.interaction.SlashCommandOptionType.*
import java.util.*

@Subscribe
class CurseCommand : AdminCommand("curse") {
    override fun createCommand(): SlashCommandBuilder {
        return SlashCommandBuilder().setName(id).setDescription("Add or remove notifications from CurseForge project")
            .addOption(createSubCommand("remove", createProjectRemoveOption()))
            .addOption(createSubCommand("add", createProjectAddOption()))
            .addOption(create(SUB_COMMAND, "invalidate_types", "Too hard to explain, ask kota lol"))
    }

    fun createSubCommand(name: String, option: SlashCommandOption) = createWithOptions(SUB_COMMAND, name, "${name.capitalize()} notifications for CurseForge project", listOf(option))
    fun getAllProjects() = HandyMods.get().mods.map { CurseAPI.project(it.curseforgeId) }
    fun getAllPresentProjects() = getAllProjects().mapNotNull { it.orElseGet(null) }
    fun createProjectRemoveOption() = createWithChoices(STRING, "project", "The project to remove", true, getAllPresentProjects().map {
        SlashCommandOptionChoice.create(it.name(), it.id().toString())
    })
    fun getProject(string: String): Optional<CurseProject> {
        return if(string.toIntOrNull() != null) {
            CurseAPI.project(string.toInt())
        } else (
            try {
                CurseAPI.project(HttpUrl.get(string))
            } catch (e: Exception) {
                return Optional.empty()
            }
        )
    }
    fun createProjectAddOption() = create(STRING, "project", "The project to add. Can be the project's ID, or URL", true)

    override fun onCalled(ctx: SlashCommandInteraction) {
        when (val name = ctx.firstOption.get().name) {
            "add", "remove" -> {
                val add = name.equals("add")
                val value = ctx.firstOption.get().firstOption.get().stringValue.get()
                val list = HandyMods.get().mods
                val p = getProject(value).orElse(null)
                if(p == null) {
                    simpleUserOnlyResponse(ctx, "Could not find project ``${value}``")
                    return
                }
                val existing = list.find { it.curseforgeId == p.id() }
                if(add) {
                    if(existing == null) {
                        list.add(HandyMods.Mod(p.id(), p.files().first().id()))
                    }
                } else {
                    list.remove(existing)
                }
                simpleUserOnlyResponse(ctx, "Project ``${p.name()}`` (``${value}``) ${if(add) "added" else "removed"}")
                command!!.createSlashCommandUpdater()
                    .setSlashCommandOptions(listOf(createSubCommand("remove", createProjectRemoveOption()), createSubCommand("add", createProjectAddOption())))
                    .setDefaultPermission(false)
                    .updateForServer(getServer())
                afterCommandRegistered()
                HandyMods.get().save()
            }
            "invalidate_types" -> {
                var i = 0
                HandyMods.get().mods.forEach {
                    if(it.type != HandyMods.ModType.UNFETCHED) {
                        it.type = HandyMods.ModType.UNFETCHED
                        i++
                    }
                }
                HandyMods.get().save()
                simpleUserOnlyResponse(ctx, "Invalided $i mod types")
            }
            else -> {
                simpleUserOnlyResponse(ctx, "Couldn't find command")
            }
        }
    }
}