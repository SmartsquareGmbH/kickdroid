package de.smartsquare.kickchain.android.client.nearby

import android.app.Activity
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessageListener
import com.google.android.gms.nearby.messages.MessagesClient
import com.google.android.gms.nearby.messages.PublishCallback
import com.google.android.gms.nearby.messages.PublishOptions
import com.google.android.gms.nearby.messages.StatusCallback
import com.google.android.gms.nearby.messages.SubscribeCallback
import com.google.android.gms.nearby.messages.SubscribeOptions

/**
 * @author Ruben Gees
 */
class NearbyManager {

    var searchingFoundListener: ((SearchingMessage) -> Unit)? = null
    var searchingLostListener: ((SearchingMessage) -> Unit)? = null
    var expiredListener: (() -> Unit)? = null

    private var client: MessagesClient? = null
    private var currentMessage: Message? = null

    private var listener: MessageListener? = null
    private var statusCallback: StatusCallback? = null
    private var publishCallback: PublishCallback? = null
    private var subscribeCallback: SubscribeCallback? = null

    fun subscribe(activity: Activity) {
        unsubscribe()

        listener = object : MessageListener() {
            override fun onFound(newMessage: Message) {
                if (newMessage.type == MessageType.SEARCHING.name) {
                    searchingFoundListener?.invoke(
                        SearchingMessage.fromNearbyMessage(
                            newMessage
                        )
                    )
                }
            }

            override fun onLost(oldMessage: Message) {
                if (oldMessage.type == MessageType.SEARCHING.name) {
                    searchingLostListener?.invoke(
                        SearchingMessage.fromNearbyMessage(
                            oldMessage
                        )
                    )
                }
            }
        }

        statusCallback = object : StatusCallback() {
            override fun onPermissionChanged(granted: Boolean) {
                if (!granted) {
                    expiredListener?.invoke()
                }
            }
        }

        publishCallback = object : PublishCallback() {
            override fun onExpired() {
                expiredListener?.invoke()
            }
        }

        subscribeCallback = object : SubscribeCallback() {
            override fun onExpired() {
                expiredListener?.invoke()
            }
        }

        client = Nearby.getMessagesClient(activity).also {
            statusCallback?.let { statusCallback -> it.registerStatusCallback(statusCallback) }

            listener?.let { listener ->
                val options = SubscribeOptions.Builder().setCallback(subscribeCallback).build()

                it.subscribe(listener, options)
            }
        }
    }

    fun unsubscribe() {
        currentMessage?.let { client?.unpublish(it) }
        listener?.let { client?.unsubscribe(it) }
        statusCallback?.let { client?.unregisterStatusCallback(it) }

        client = null
        currentMessage = null
        listener = null
        statusCallback = null
    }

    fun search(newMessage: SearchingMessage) {
        val safeClient = client

        if (safeClient == null) {
            throw IllegalStateException("Only call this method after subscribing")
        } else {
            currentMessage?.let { client?.unpublish(it) }
            currentMessage = newMessage.toNearbyMessage().also {
                val options = PublishOptions.Builder().setCallback(publishCallback).build()

                safeClient.publish(it, options)
            }
        }
    }
}
