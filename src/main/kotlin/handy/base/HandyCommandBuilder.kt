package handy.base

import handy.HandyDiscord.api
import handy.data.HandyConfig
import org.javacord.api.entity.permission.PermissionType
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import org.javacord.api.interaction.*
import org.javacord.api.interaction.SlashCommandOptionChoice.*
import org.javacord.api.interaction.SlashCommandPermissionType.*

open class HandyCommandBuilder(val name: String, val description: String) {
    open class OptionBuilder(val name: String, val description: String, val type: SlashCommandOptionType, var required: Boolean = true) {
        protected var choices = arrayListOf<SlashCommandOptionChoice>()

        fun choice(name: String, value: Int) {
            choices.add(create(name, value))
        }
        fun choice(name: String, value: String) {
            choices.add(create(name, value))
        }

        fun build(): SlashCommandOption {
            val b = SlashCommandOptionBuilder()
                .setName(name)
                .setDescription(description)
                .setType(type)
                .setRequired(required)
            for(choice in choices) b.addChoice(choice)
            return b.build()
        }
    }

    open class PermissionBuilder(var default: Boolean = true) {
        fun addRoles(vararg roles: String) {
            this.roles.addAll(roles)
        }

        protected var roles = arrayListOf<String>()
        protected var rolePredicate: Role.() -> Boolean = { roles.contains(idAsString) }

        fun rolePredicate(func: Role.() -> Boolean) {
            val oldP = rolePredicate
            rolePredicate = {
                oldP() && func()
            }
        }

        fun allowWithPermission(p: PermissionType) = rolePredicate {
            return@rolePredicate allowedPermissions.contains(p)
        }

        fun admin(overwriteDefault: Boolean = true, overwritePredicate: Boolean = true) {
            if(overwriteDefault) default = false
            if(overwritePredicate) rolePredicate = { true }
            allowWithPermission(PermissionType.ADMINISTRATOR)
        }

        protected var users = arrayListOf<String>()

        fun addUsers(vararg user: String) {
            users.addAll(user)
        }

        fun addRoles(vararg roles: Long) {
            addRoles(*roles.map { it.toString() }.toTypedArray())
        }

        fun apply(id: Long) {
            val server = api!!.getServerById(HandyConfig.get().mainServer).get()
            var u = SlashCommandPermissionsUpdater(server)
            for (it in server.roles) {
                if(rolePredicate(it)) {
                    u.addPermission(it.id, ROLE, true)
                }
            }
            u.addPermissions(users.map { SlashCommandPermissions.create(it.toLong(), USER, true) })
            u.update(id)
        }

    }
    protected var options: ArrayList<OptionBuilder> = arrayListOf()

    open class HandySlashCommand(protected val builder: HandyCommandBuilder) : SlashCommandBuilder() {
        fun getPermissions() = builder.permissionBuilder
        fun applyPermissions(id: Long) {
            val p = getPermissions()
            setDefaultPermission(p.default)
            p.apply(id)
        }
    }

    fun build(): SlashCommandBuilder {
        val b = HandySlashCommand(this).setName(name).setDescription(description)
        for(option in options) b.addOption(option.build())
        return b
    }

    fun option(name: String, description: String, type: SlashCommandOptionType, func: OptionBuilder.() -> Unit = {}) {
        val b = OptionBuilder(name, description, type)
        func(b)
        options.add(b)
    }

    protected var permissionBuilder = PermissionBuilder()

    fun permissions(func: PermissionBuilder.() -> Unit) {
        func(permissionBuilder)
    }
}