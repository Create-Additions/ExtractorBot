package handy.base

import org.javacord.api.entity.permission.PermissionState
import org.javacord.api.entity.permission.PermissionType
import org.javacord.api.interaction.SlashCommandBuilder
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.interaction.SlashCommandPermissionType
import org.javacord.api.interaction.SlashCommandPermissions

abstract class AdminCommand(id: String) : HandyCommand(id) {
    override fun register(): SlashCommandBuilder {
        return createCommand().setDefaultPermission(false)
    }

    abstract fun createCommand(): SlashCommandBuilder

    override fun afterCommandRegistered() {
        val s = getServer()
        setCommandPermissions(s,
            *s.roles.map {
                return@map if(it.permissions.getState(PermissionType.ADMINISTRATOR).equals(PermissionState.ALLOWED))
                    SlashCommandPermissions.create(it.id, SlashCommandPermissionType.ROLE, true)
                else null
            }.filterNotNull().toTypedArray())
    }
}