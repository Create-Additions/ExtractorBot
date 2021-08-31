package handy.data

import handy.Handy.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class HandyConfig(val discordToken: String = "TODO", var isDev: Boolean = true, var suggestionChannel: String = "TODO",
                       var mainServer: String = "TODO", var applyChannel: String = "TODO", var ideaRole: String = "TODO",
                       var coderRole: String = "TODO", var artRole: String = "TODO", var packRole: String = "TODO",
                       var devRole: String = "TODO", var funnyButton: Boolean = true, var curseCheckInterval: Int = 60 * 60 * 60,
                       var modReleasesChannel: String = "TODO", var modReleasesCheckCount:Int = 4, var officialProjects: List<Int> = ArrayList(),
                       var packReleasesChannel: String = "TODO", var officialReleasesChannel: String = "TODO", var releaseWebhook: String = "TODO",
                       var ghToken: String = "TODO", var prefix: String = "!", var quoteChannel: String = "TODO"
) {
    companion object {
        val file = HandyData.get("config.json")
        var INSTANCE: HandyConfig? = null

        fun get(): HandyConfig {
            if(INSTANCE != null) return INSTANCE as HandyConfig;
            INSTANCE = json.decodeFromString(file.readText())
            return INSTANCE!!.save()
        }
    }

    init {
        INSTANCE = this
    }

    fun save(): HandyConfig {
        file.writeText(json.encodeToString(this))
        return this
    }
}