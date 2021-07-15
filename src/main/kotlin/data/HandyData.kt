package data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
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

    abstract class Data<T>(val file: File, val clazz: Class<T>) {
        abstract fun getThis(): T

        fun file() = file
        fun read(): T = Gson().fromJson(file().readText(), clazz)
        open fun save(): T {
//            file().writeText(GsonBuilder().setPrettyPrinting().create().toJson(getThis(), TypeToken.get(clazz).type))
            return getThis()
        }
    }

    fun <T : Data<T>> deserialize(path: String, t: Class<T>): T {
        return Gson().fromJson(get(path).readText(), t).save()
    }

    fun read(path: String): String = get(path).readText()
}