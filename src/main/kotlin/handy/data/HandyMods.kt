package handy.data

import com.therandomlabs.curseapi.CurseAPI
import com.therandomlabs.curseapi.file.CurseFile
import handy.Handy
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

@Serializable
class HandyMods(val mods: List<Mod> = emptyList()) {
    companion object{
        val file = HandyData.get("mods.json")
        var INSTANCE: Optional<HandyMods> = Optional.empty();

        fun get(): HandyMods {
            return INSTANCE.orElseGet {
                INSTANCE = Optional.of(Json.decodeFromString(file.readText()))
                return@orElseGet INSTANCE.get().save()
            }
        }
    }

    @Serializable
    data class Mod(val curseforgeId: Int, var lastId: Int) {
        fun getFiles() =
            CurseAPI.files(curseforgeId)
    }

    fun save(): HandyMods {
        file.writeText(Handy.json.encodeToString(this))
        return this
    }
}