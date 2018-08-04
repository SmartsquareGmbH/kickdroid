package de.smartsquare.kickchain.android.client.nearby

import com.google.android.gms.nearby.messages.Message
import com.squareup.moshi.JsonClass
import de.smartsquare.kickchain.android.client.MainApplication.Companion.moshi
import de.smartsquare.kickchain.android.client.findmatch.Player

/**
 * @author Ruben Gees
 */
@JsonClass(generateAdapter = true)
data class SearchingMessage(val name: String, val wins: Int?, val losses: Int?) {

    companion object {
        fun fromNearbyMessage(message: Message): SearchingMessage {
            return moshi.adapter(SearchingMessage::class.java)
                .fromJson(message.content.toString(Charsets.UTF_8))
                ?: throw IllegalArgumentException("Invalid message: $message")
        }
    }

    fun toNearbyMessage(): Message {
        val thisAsJson = moshi.adapter(this.javaClass).toJson(this)

        return Message(thisAsJson.toByteArray(), MessageType.SEARCHING.name)
    }

    fun toPlayer(): Player {
        return Player(name, wins, losses)
    }
}
