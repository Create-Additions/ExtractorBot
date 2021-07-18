package handy.data

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Path

object HandyData {
    val DATA_PATH = Path.of("data/")

    fun get(path: String): File {
        val f = File("$DATA_PATH/$path")
        f.parentFile.mkdirs()
        if(f.createNewFile()) {
            f.writeText("{}")
        }
        return f
    }

    fun read(path: String): String = get(path).readText()
}