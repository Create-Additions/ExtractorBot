package handy.data

import handy.Handy.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class HandyConfig(val discordToken: String, var isDev: Boolean = true, var suggestionChannel: String = "",
                       var mainServer: String = "", var applyChannel: String = "", var ideaRole: String = "",
                       var coderRole: String = "", var artRole: String = "", var packRole: String = "",
                       var devRole: String = "", var funnyButton: Boolean = true, var curseCheckInterval: Int = 120,
                       var modReleasesChannel: String, var modReleasesCheckCount:Int = 10
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