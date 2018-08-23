package de.smartsquare.kickchain.android.client.nearby

import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessagesClient
import com.squareup.moshi.Moshi
import io.reactivex.Observable

/**
 * @author Ruben Gees
 */
class NearbyWrapper(private val internalClient: MessagesClient, private val moshi: Moshi) {

    private var currentMessage: Message? = null

    fun send(message: NearbyMessage<*>) {
        currentMessage?.also { internalClient.unpublish(it) }

        currentMessage = message.toNativeMessage(moshi)

        currentMessage?.also { internalClient.publish(it) }
    }

    fun foundMessages(): Observable<NearbyMessage<*>> {
        return NearbyMessageObservable(internalClient)
            .filter { it is NearbyEvent.Found }
            .map { it as NearbyEvent.Found }
            .map { it.message.toNearbyMessage(moshi) }
    }

    fun lostMessages(): Observable<NearbyMessage<*>> {
        return NearbyMessageObservable(internalClient)
            .filter { it is NearbyEvent.Lost }
            .map { it as NearbyEvent.Lost }
            .map { it.message.toNearbyMessage(moshi) }
    }
}
