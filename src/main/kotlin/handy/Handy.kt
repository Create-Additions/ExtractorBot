package handy

import kotlinx.serialization.json.Json

object Handy {
    val json = Json { ignoreUnknownKeys = true }

    @JvmStatic
        fun main(args: Array<String>) {
            HandyDiscord.create()
    }
}
