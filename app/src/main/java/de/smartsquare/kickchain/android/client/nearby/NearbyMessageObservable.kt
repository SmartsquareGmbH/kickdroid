package de.smartsquare.kickchain.android.client.nearby

import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessageListener
import com.google.android.gms.nearby.messages.MessagesClient
import de.smartsquare.kickchain.android.client.checkMainThread
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import java.util.concurrent.atomic.AtomicReference

/**
 * @author Ruben Gees
 */
class NearbyMessageObservable(private val internalClient: MessagesClient) :
    Observable<NearbyEvent>() {

    override fun subscribeActual(observer: Observer<in NearbyEvent>) {
        if (!observer.checkMainThread()) {
            return
        }

        val listener = Listener(internalClient, observer)

        observer.onSubscribe(listener)
        listener.subscribeInternal(internalClient)
    }

    private class Listener(
        private val internalClient: MessagesClient,
        private val observer: Observer<in NearbyEvent>
    ) : MainThreadDisposable() {

        private val messageListener = AtomicReference<MessageListener>(object : MessageListener() {
            override fun onFound(message: Message) {
                observer.onNext(NearbyEvent.Found(message))
            }

            override fun onLost(message: Message) {
                observer.onNext(NearbyEvent.Lost(message))
            }
        })

        fun subscribeInternal(internalClient: MessagesClient) {
            internalClient.subscribe(messageListener.get())
        }

        override fun onDispose() {
            internalClient.unsubscribe(messageListener.get())
            messageListener.set(null)
        }
    }
}
