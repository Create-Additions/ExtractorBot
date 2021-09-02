package handy.commands

import handy.base.LazyValue
import handy.base.RawCommand
import handy.base.SubscribeInitable
import handy.data.HandyConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.javacord.api.event.message.MessageCreateEvent
import java.io.File
import java.lang.Exception
import java.net.URL
import kotlin.text.RegexOption.DOT_MATCHES_ALL


@SubscribeInitable
class CompileCommand : RawCommand("compile") {
    override fun getTriggers(): MutableList<String> {
        return ArrayList(super.getTriggers() + listOf("${prefix}run", "${prefix}eval", "${prefix}do"))
    }
    companion object {
        val compilePath = "https://wandbox.org/api/compile.json"

        open class Language(val name: String, val version: String = "head") {
            var compiler: String = "$name-$version"
            open suspend fun run(event: MessageCreateEvent, code: String): WandboxResponse {
                val client = HttpClient(CIO) {
                    install(JsonFeature)
                }
                return client.post(compilePath) {
                    contentType(ContentType.Application.Json)
                    body = WandboxRequest(compiler, code)
                }
            }
        }

        @Serializable
        data class WandboxRequest(val compiler: String, val code: String, val save: Boolean = true)
        @Serializable
        data class WandboxResponse(
            var permalink: String?,
            var program_message: String?,
            var program_output: String?,
            var status: Int,
            var url: String?) {
            override fun toString(): String =
                program_output ?: "Error: $program_message"

            fun urlNoEmbed(): String? {
                if(url == null) return null
                return "<${url}>"
            }

            companion object {
                fun message(message: String) = WandboxResponse(null, message, message, 1, null)
            }
        }

        private val languages = mutableMapOf<String, Language>()

        fun addAlias(lang: Language, vararg alias: String): Language {
            for (it in alias) {
                languages[it] = lang
            }
            return lang
        }

        fun lang(name: String, vararg alias: String): Language {
            return addAlias(Language(name), *alias)
        }

        object JavaLanguage : Language("openjdk") {
            override suspend fun run(event: MessageCreateEvent, code: String): WandboxResponse {
                var codeV = code
                if(code.lines().none { it.contains(Regex("class (.)*\\{", DOT_MATCHES_ALL)) }) {
                    codeV = "class HandyIsVeryCoolAndEpic {\n" +
                            "  public static void main(String[] args) {\n" +
                            "      $code;\n" +
                            "  }\n" +
                            "}"
                }
                return super.run(event, codeV)
            }
        }

        object FloppaLanguage : Language("floppa") {
            val floppaTemplate = LazyValue {
                // npe my beloved
                return@LazyValue File(ClassLoader.getSystemResource("floppa.go").file).readText()
            }

            val goVersion = "1.14.2"

            fun floppaCode(code: String): String {
                return floppaTemplate.get().replace("%CODE%", code)
            }

            suspend fun floppaToGo(client: HttpClient, code: String): String? {
                val response: WandboxResponse = client.post(compilePath) {
                    contentType(ContentType.Application.Json)
                    body = WandboxRequest("go-$goVersion", floppaCode(code), save = false)
                }

                val responseOutput = response.toString()
                if(responseOutput.lines()[0] == "CODE: ") {
                    return response.toString().replaceFirst("CODE: \n", "")
                        .replaceFirst("byte(a %!)(MISSING)", "byte(a % 255)")
                        .replaceFirst("byte(256 - (a %!)(MISSING))", "byte(256 - (a % 255))")
                }
                return null
            }

            override suspend fun run(event: MessageCreateEvent, code: String): WandboxResponse {
                val client = HttpClient(CIO) {
                    install(JsonFeature)
                }
                var goCode = floppaToGo(client, code)
                if(goCode != null) {
                    return client.post(compilePath) {
                        contentType(ContentType.Application.Json)
                        body = WandboxRequest("go-$goVersion", goCode)
                    }
                }
                return WandboxResponse.message("Could not parse code!")
//                return WandboxResponse("floppers!","Floppers!", "Hello Floppa!", 1, "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
            }
        }

        val node = lang("nodejs", "node", "nodejs", "js", "javascript")
        val openjdk = addAlias(JavaLanguage, "java", "openjdk", "jdk", "jar", "jre")
        val openjdkRaw = lang("openjdk", "javaraw", "openjdkraw")
        val python = lang("cpython", "py", "py3", "python", "python3")
        val floppa = addAlias(FloppaLanguage, "floppa", "flop", "floppers")
//        val languages = mapOf(
//            "node" to node,
//            "nodejs" to node,
//            "js" to node,
//            "javascript" to node,
//            "java" to openjdk,
//            "openjdk" to openjdk,
//            "jdk" to openjdk,
//            "jar" to openjdk, // idk lol
//            "jre" to openjdk,
//            "py" to python,
//            "py3" to python,
//            "python" to python,
//            "python3" to python)
        val languageAlias = LazyValue {
            val map = mutableMapOf<Language, ArrayList<String>>()
            for((key, value) in languages) {
                if(!map.containsKey(value)) map[value] = arrayListOf()
                map[value]!!.add(key)
            }
            return@LazyValue map
        }
    }

    override fun checkForPermissions(event: MessageCreateEvent): Boolean = HandyConfig.get().compileEnabled
    override fun run(event: MessageCreateEvent) {
        val argsIncludingCmd = event.messageContent.split("\n", " ")
        val args = argsIncludingCmd.minus(argsIncludingCmd[0])
        if(args.isEmpty()) {
            event.message.reply("Language argument not provided")
            return
        }
        val alias = languageAlias.get()
        val langName = args[0]
        val lang = languages.getOrDefault(langName, null)
        if(lang == null) {
            event.message.reply("Could not find language")
            return
        }
        var code: String? = null
        var urlToFetch: String? = null
        val attachments = event.messageAttachments
        var url = try {
            URL(args[1].removePrefix("<").removeSuffix(">"))
        } catch (e: Exception) {
            null
        }
        if(attachments.isNotEmpty()) {
            urlToFetch = attachments[0].url.toString()
        } else if(url != null) {
            if(url.host == "github.com") {
                url = url.replaceHost("raw.githubusercontent.com").replaceFile(url.file.replace("/blob", ""))
            }
            urlToFetch = url.toString()
        } else {
            if(code == null) {
                code = args.minus(langName)
                    .joinToString(" ")
                    .removeSuffix("```")
                    .replaceFirst("```", "")
            }
            if(code.isBlank()) {
                event.message.reply("No code provided")
                return
            }
        }
        runBlocking {
            launch {
                if(urlToFetch != null && code == null) {
                    code = HttpClient(CIO).get(urlToFetch)
                }
                val out = lang.run(event, code ?: "")
                var outString = out.toString().replace("`", "\\`")
                if(outString.length > 330 || outString.split("\n").size > 7) outString = "Output too long, click link below"
                event.message.reply("```$outString``` ${out.urlNoEmbed() ?: "``Could not get link``"}")
            }
        }
//        runBlocking {
//            launch {
//                languages[args[0]].run(event, "console.log('tea')")
//            }
//        }
    }
}

fun URL.replaceHost(newHost: String): URL {
    return URL(protocol, newHost, file)
}

fun URL.replaceFile(newFile: String): URL {
    return URL(protocol, host, newFile)
}