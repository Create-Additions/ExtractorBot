package data

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken


class HandyConfig : HandyData.Data<HandyConfig> {
    companion object {
        var INSTANCE: HandyConfig? = null;

        fun get(): HandyConfig {
            if(INSTANCE != null) return INSTANCE as HandyConfig;
            return HandyData.deserialize("config.json", HandyConfig::class.java)
        }
    }

    @SerializedName("token")
    var discordToken = ""
    @SerializedName("dev")
    var dev = true
    @SerializedName("mainServer")
    var mainServer = ""
    @SerializedName("suggestionChannel")
    var suggestionChannel = ""
    @SerializedName("applyChannel")
    var applyChannel = ""

    fun getToken() = discordToken
    fun isDev() = dev
    fun getServer() = mainServer

    constructor() : super(HandyData.get("config.json"), HandyConfig::class.java) {
        INSTANCE = this
    }

    override fun getThis(): HandyConfig {
        return this
    }

}