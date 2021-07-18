package handy.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

@Serializable
class HandyPronouns {
    companion object {
        val file = HandyData.get("pronouns.json")
        var INSTANCE: Optional<HandyPronouns> = Optional.empty();

        fun get(): HandyPronouns {
            return INSTANCE.orElseGet {
                INSTANCE = Optional.of(Json.decodeFromString(file.readText()))
                return@orElseGet INSTANCE.get()
            }
        }
    }

    var pronouns: List<Pronoun> = emptyList()

    @Serializable
    data class Pronoun(val id: String)
}