package handy.stUFFFFF

import com.therandomlabs.curseapi.file.CurseFile
import handy.HandyDiscord.api
import handy.base.Subscribable
import handy.base.Subscribe
import handy.data.HandyConfig
import handy.data.HandyMods
import org.eclipse.egit.github.core.Gist
import org.eclipse.egit.github.core.GistFile
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.GistService
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.entity.message.embed.EmbedBuilder
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
        changelog = when {
            changelog.isEmpty() -> {
                "No changelog provided"
            }
            changelog.length > 330 -> {
                val client = GitHubClient().setOAuth2Token(HandyConfig.get().ghToken)
                var gist = Gist().setDescription("Changelog for ${file.nameOnDisk()}")
                val gistFile = GistFile().setContent(changelog)
                gist.files = mapOf("changelog-${file.id()}.md" to gistFile)
                gist = GistService(client).createGist(gist)
                "Changelog too long, press [here](${gist.htmlUrl}) to see it"
            }
            else -> changelog
        }
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
//            .addActionRow(
//                Button.link(file.url().toString(), "File"),
//                Button.link(file.downloadURL().toString(), "Download"),
//                Button.link(file.project().url().toString(), file.project().name())
//            )
            .send(webhook.asIncomingWebhook().get()).get().crossPost()
    }
}