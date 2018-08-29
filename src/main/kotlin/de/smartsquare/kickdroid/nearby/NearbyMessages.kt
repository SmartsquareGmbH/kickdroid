package de.smartsquare.kickdroid.nearby

import com.google.android.gms.nearby.connection.Payload
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import de.smartsquare.kickdroid.domain.Lobby

private val typeMap = mapOf(
    "GET_STATUS" to NearbyMessage.GetStatusMessage::class.java,
    "IDLE" to NearbyMessage.IdleMessage::class.java,
    "PLAYING" to NearbyMessage.PlayingMessage::class.java
)

sealed class NearbyMessage(val type: String) {

    class GetStatusMessage : NearbyMessage("GET_STATUS")
    class IdleMessage : NearbyMessage("IDLE")

    @JsonClass(generateAdapter = true)
    class PlayingMessage(val lobby: Lobby) : NearbyMessage("PLAYING")
}

fun Payload.toNearbyMessage(moshi: Moshi): NearbyMessage {
    val content = this.asBytes()?.toString(Charsets.UTF_8) ?: ""
    val head = content.substringBefore('\n', "")

    if (head.isBlank()) {
        throw NearbyInvalidMessageException("Message without head could not be parsed: $content")
    }

    val messageType = typeMap[head]
        ?: throw NearbyInvalidMessageException("Message with unknown type $type could not be parsed: $content")

    return if (messageType.isAnnotationPresent(JsonClass::class.java)) {
        // This message requires a body.
        val body = content.substringAfter('\n', "")

        if (body.isBlank()) {
            throw NearbyInvalidMessageException("Message without body could not be parsed: $body")
        }

        try {
            moshi.adapter(messageType).fromJson(body)
                ?: throw NearbyInvalidMessageException("Message could not be parsed: $content")
        } catch (exception: JsonDataException) {
            throw NearbyInvalidMessageException(
                "Message with invalid structure could not be parsed: $content",
                exception
            )
        } catch (exception: JsonEncodingException) {
            throw NearbyInvalidMessageException(
                "Message with invalid encoding could not be parsed: $content",
                exception
            )
        }
    } else {
        // This messages does not require a body.
        messageType.newInstance()
    }
}
