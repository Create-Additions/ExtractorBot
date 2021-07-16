package handy.data

import com.google.gson.annotations.SerializedName


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
    @SerializedName("ideaRole")
    var ideaRole = "865251596409503774"

    @SerializedName("coderRole")
    var coderRole = ""
    @SerializedName("artRole")
    var artRole = ""
    @SerializedName("packRole")
    var packRole = ""
    @SerializedName("devRole")
    var devRole = ""

    @SerializedName("funnyButton")
    var funnyButton = true

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