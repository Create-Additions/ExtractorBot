package handy.data

import com.therandomlabs.curseapi.CurseAPI
import com.therandomlabs.curseapi.file.CurseFile
import com.therandomlabs.curseapi.file.CurseFiles
import com.therandomlabs.curseapi.project.CurseProject
import handy.Handy
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.collections.ArrayList

@Serializable
class HandyMods(val mods: ArrayList<Mod> = ArrayList()) {
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
    data class Mod(val curseforgeId: Int, var lastId: Int, var type: ModType = ModType.UNFETCHED) {
        fun getFiles(): Optional<CurseFiles<CurseFile>> {
            return CurseAPI.files(curseforgeId)
        }

        fun getProjectType(project: CurseProject): ModType {
            if(type != ModType.UNFETCHED) return type
            type = if(HandyConfig.get().officialProjects.contains(project.id())) ModType.OFFICIAL
            else {
                if(project.categorySection().id() == 6) ModType.MOD else ModType.PACK
            }
            return type
        }
    }

    fun save(): HandyMods {
        file.writeText(Handy.json.encodeToString(this))
        return this
    }

    enum class ModType(val getChannel: () -> String?) {
        OFFICIAL ({
            HandyConfig.get().officialReleasesChannel
        }),
        MOD ({
            HandyConfig.get().modReleasesChannel
        }),
        PACK ({
            HandyConfig.get().packReleasesChannel
        }),
        UNFETCHED ({ null });
    }
}