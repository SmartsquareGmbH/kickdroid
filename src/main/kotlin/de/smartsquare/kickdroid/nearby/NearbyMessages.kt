package de.smartsquare.kickdroid.nearby

import com.google.android.gms.nearby.messages.Message
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi

private val typeMap = mapOf("IDLE" to NearbyMessage.Idle::class.java)

fun Message.toNearbyMessage(moshi: Moshi): NearbyMessage {
    return typeMap.get(type)
        ?.let {
            try {
                moshi
                    .adapter(it)
                    .fromJson(content.toString(Charsets.UTF_8))
            } catch (error: JsonDataException) {
                throw NearbyInvalidMessageException("Message does not conform to expected data structure", error)
            }
        }
        ?: throw NearbyUnknownMessageException("Unknown message type: $type")
}

sealed class NearbyMessage {

    fun toNativeMessage(moshi: Moshi): Message {
        val type = typeMap.entries.find { (_, clazz) -> clazz == this.javaClass }?.key
            ?: throw IllegalStateException("No type found for class: $javaClass")

        return Message(moshi.adapter(javaClass).toJson(this).toByteArray(), type)
    }

    @JsonClass(generateAdapter = true)
    data class Idle(
        val raspberryId: Long
    ) : NearbyMessage()
}
