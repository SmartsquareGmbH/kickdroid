package de.smartsquare.kickdroid.nearby

import com.google.android.gms.nearby.messages.Message
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import de.smartsquare.kickdroid.nearby.NearbyMessage.IdleMessage.SearchingMessageContent

private val typeMap = mapOf("IDLE" to NearbyMessage.IdleMessage::class.java)

fun Message.toNearbyMessage(moshi: Moshi): NearbyMessage<*> {
    return typeMap.get(type)
        ?.let {
            try {
                moshi
                    .adapter(NearbyMessage.IdleMessage::class.java)
                    .fromJson(content.toString(Charsets.UTF_8))
            } catch (error: JsonDataException) {
                throw NearbyInvalidMessageException("Message does not conform to expected data structure", error)
            }
        }
        ?: throw NearbyUnknownMessageException("Unknown message type: ${type}")
}

sealed class NearbyMessage<T> {

    abstract val content: T

    fun toNativeMessage(moshi: Moshi): Message {
        val type = typeMap.entries.find { (_, clazz) -> clazz == this.javaClass }?.key
            ?: throw IllegalStateException("No type found for class: $javaClass")

        return Message(moshi.adapter(javaClass).toJson(this).toByteArray(), type)
    }

    @JsonClass(generateAdapter = true)
    data class IdleMessage(
        override val content: SearchingMessageContent
    ) : NearbyMessage<SearchingMessageContent>() {

        @JsonClass(generateAdapter = true)
        data class SearchingMessageContent(val raspberryId: String)
    }
}
