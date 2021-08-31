package handy.data

import handy.Handy
import handy.HandyDiscord.api
import handy.base.LazyValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.javacord.api.entity.channel.TextChannel
import java.util.*
import javax.swing.UIDefaults
import kotlin.collections.ArrayList

@Serializable
class HandyQuotes {
    val quotes = arrayListOf<Quote>()

    companion object{
        val file = HandyData.get("quotes.json")
        val INSTANCE: LazyValue<HandyQuotes> = LazyValue {
            return@LazyValue Json.decodeFromString(file.readText())
        };
    }

    fun addAndSave(element: Quote): HandyQuotes {
        quotes.add(element)
        return save()
    }

    fun save(): HandyQuotes {
        file.writeText(Handy.json.encodeToString(this))
        return this
    }

    @Serializable
    data class Quote(val messageId: String, val channelId: String) {
        override fun toString(): String {
            return "$channelId/$messageId"
        }
        fun getMessage() = api!!.getMessageById(messageId, api!!.getTextChannelById(channelId).get())
    }
}