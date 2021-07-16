package handy.data

import com.google.gson.annotations.SerializedName
import java.util.*

class HandyPronouns : HandyData.Data<HandyPronouns>(HandyData.get("pronouns.json"), HandyPronouns::class.java) {
    companion object {
        var INSTANCE: Optional<HandyPronouns> = Optional.empty();

        fun get(): HandyPronouns {
            return INSTANCE.orElseGet {
                INSTANCE = Optional.of(HandyData.deserialize("pronouns.json", HandyPronouns::class.java))
                return@orElseGet INSTANCE.get()
            }
        }
    }

    @SerializedName("pronouns")
    var pronouns: List<Pronoun> = emptyList()

    data class Pronoun(@SerializedName("id") val id: String)

    override fun getThis() = this
}