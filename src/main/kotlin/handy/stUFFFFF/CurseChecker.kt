package handy.stUFFFFF

import com.therandomlabs.curseapi.file.CurseFile
import handy.HandyDiscord.api
import handy.base.Initable
import handy.base.SubscribeInitable
import handy.data.HandyConfig
import handy.data.HandyMods
import org.eclipse.egit.github.core.Gist
import org.eclipse.egit.github.core.GistFile
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.GistService
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.entity.message.component.Button
import org.javacord.api.entity.message.component.LowLevelComponent
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.webhook.Webhook
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate


@SubscribeInitable
class CurseChecker : Initable {
    override fun init() {
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

//    fun getOrCreateWebhook(): Webhook {
//        val id = HandyConfig.get().releaseWebhook
//        // if id contains non numerical characters
//        if(!id.contains(Regex("\\d"))) {
//            val w = api!!.getServerById(HandyConfig.get().mainServer).get()
//                .getTextChannelById(HandyConfig.get().modReleasesChannel).get().createWebhookBuilder()
//                .setName("Release webhook").create().get()
//            HandyConfig.get().releaseWebhook = w.idAsString
//            HandyConfig.get().save()
//            return w
//        }
//        return api!!.getWebhookById(id.toLong()).get()
//    }

    fun createWebhook(mod: HandyMods.Mod, file: CurseFile): Webhook {
        val project = file.project();
        return api!!.getServerTextChannelById(mod.getProjectType(project).getChannel()).get().createWebhookBuilder()
            .setAvatar(project.logo().url().url())
            .setName(project.name())
//            .setChannel(api!!.getChannelById(mod.getProjectType(project).getChannel()).get().asServerTextChannel().get())
            .create().get()
    }

    fun sendNewFileMessage(mod: HandyMods.Mod, file: CurseFile) {
        val webhook = createWebhook(mod, file)
        var changelog = file.changelogPlainText()
        val components = LinkedList(arrayListOf(
            Button.link(file.url().toString(), "File"),
            Button.link(file.downloadURL().toString(), "Download"),
            Button.link(file.project().url().toString(), file.project().name()))
        )
        var changelogButton = Button.link(file.url().toString(), "Changelog")
        changelog = when {
            changelog.isEmpty() -> {
                "No changelog provided"
            }
            changelog.length > 330 || changelog.split("\n").size > 7 -> {
                val client = GitHubClient().setOAuth2Token(HandyConfig.get().ghToken)
                var gist = Gist().setDescription("Changelog for ${file.nameOnDisk()}")
                val gistFile = GistFile().setContent(changelog)
                gist.files = mapOf("changelog-${file.id()}.md" to gistFile)
                gist = GistService(client).createGist(gist)
                changelogButton = Button.link(gist.htmlUrl, "Changelog")
                "Changelog too long, press [here](${gist.htmlUrl}) to see it"
            }
            else -> changelog
        }
        components.push(changelogButton)
        MessageBuilder()
            .addEmbed(EmbedBuilder()
                .setTitle("${file.project().name()} released a new file: ${file.displayName()}")
                .setUrl(file.url().toString())
                .addField("Changelog", changelog)
                .setColor(mod.type.color)
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
            .addActionRow(*components.toTypedArray())
            .send(webhook.asIncomingWebhook().get()).get().crossPost().get()
        webhook.delete("Update message sent.")
    }
}