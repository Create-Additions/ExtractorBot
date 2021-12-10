package handy

import kotlinx.serialization.json.Json

object Handy {
    val json = Json {  prettyPrint = true; encodeDefaults = true }

    @JvmStatic
    fun main(args: Array<String>) {
            System.setProperty("log4j2.formatMsgNoLookups", "true");
            HandyDiscord.create()
    }
}
