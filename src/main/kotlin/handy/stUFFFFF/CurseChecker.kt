package handy.stUFFFFF

import com.therandomlabs.curseapi.CurseAPI
import com.therandomlabs.curseapi.CurseException
import com.therandomlabs.curseapi.file.CurseFile
import com.therandomlabs.curseapi.forgesvc.ForgeSvcProvider
import handy.HandyDiscord.api
import handy.base.Subscribable
import handy.base.Subscribe
import handy.data.HandyConfig
import handy.data.HandyMods
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.entity.message.component.ActionRow
import org.javacord.api.entity.message.component.Button
import org.javacord.api.entity.message.embed.EmbedBuilder
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

@Subscribe
class CurseChecker : Subscribable {
    override fun subscribe() {
        Timer().scheduleAtFixedRate(10, HandyConfig.get().curseCheckInterval * 1000L) {
            HandyMods.get().mods.forEach { mod ->
                val files = mod.getFiles().get()
                if(files.first().newerThan(mod.lastId)) {
                    files.toList().take(HandyConfig.get().modReleasesCheckCount).reversed().forEach { file ->
                        if(file.newerThan(mod.lastId)) {
                            sendNewFileMessage(mod, file)
                            mod.lastId = file.id()
                        }
                    }
                    HandyMods.get().save()
                }
            }
        }
    }

    fun sendNewFileMessage(mod: HandyMods.Mod, file: CurseFile) {
        var webhook = api!!.getWebhookById(HandyConfig.get().releaseWebhook.toLong()).get()
        val project = file.project();
        webhook = webhook.createUpdater()
            .setAvatar(project.logo().url().url())
            .setName(project.name())
            .setChannel(api!!.getChannelById(mod.getProjectType(project).getChannel()).get().asServerTextChannel().get())
            .update().get()
        var changelog = file.changelogPlainText()
        if(changelog.isEmpty()) {
            changelog = "No changelog provided"
        }
        MessageBuilder()
            .addEmbed(EmbedBuilder()
                .setTitle("${file.project().name()} released a new file: ${file.displayName()}")
                .setUrl(file.url().toString())
                .addField("Changelog", changelog)
            )
//            .appendNewLine()
//            .append("```")
//            .append {
//                val text = file.changelogPlainText()
//                if(text.isEmpty()) {
//                    return@append "No changelog provided"
//                }
//                return@append text
//            }
//            .append("```")
//            .addActionRow(
//                Button.link(file.url().toString(), "File"),
//                Button.link(file.downloadURL().toString(), "Download"),
//                Button.link(file.project().url().toString(), file.project().name())
//            )
            .send(webhook.asIncomingWebhook().get()).get().crossPost()
    }
}