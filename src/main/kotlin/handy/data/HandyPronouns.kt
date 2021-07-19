package handy.data

import handy.Handy.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.collections.ArrayList

@Serializable
class HandyPronouns(val pronouns: ArrayList<Pronoun>) {
    companion object {
        val file = HandyData.get("pronouns.json")
        var INSTANCE: Optional<HandyPronouns> = Optional.empty();

        fun get(): HandyPronouns {
            return INSTANCE.orElseGet {
                INSTANCE = Optional.of(Json.decodeFromString(file.readText()))
                return@orElseGet INSTANCE.get().save()
            }
        }
    }

    fun save(): HandyPronouns {
        file.writeText(json.encodeToString(this))
        return this
    }

    @Serializable
    data class Pronoun(val id: String)
}