package de.smartsquare.kickdroid.nearby

import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessageListener
import com.google.android.gms.nearby.messages.MessagesClient
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Ruben Gees
 */
class NearbyMessageObservable(private val internalClient: MessagesClient) :
    Observable<NearbyEvent>() {

    override fun subscribeActual(observer: Observer<in NearbyEvent>) {
        val listener = Listener(internalClient, observer)

        observer.onSubscribe(listener)
        internalClient.subscribe(listener)
    }

    private class Listener(
        private val internalClient: MessagesClient,
        private val observer: Observer<in NearbyEvent>
    ) : MessageListener(), Disposable {

        private val unsubscribed = AtomicBoolean()

        override fun onFound(message: Message) {
            observer.onNext(NearbyEvent.Found(message))
        }

        override fun onLost(message: Message) {
            observer.onNext(NearbyEvent.Lost(message))
        }

        override fun isDisposed(): Boolean {
            return unsubscribed.get()
        }

        override fun dispose() {
            if (unsubscribed.compareAndSet(false, true)) {
                internalClient.unsubscribe(this)
            }
        }
    }
}
