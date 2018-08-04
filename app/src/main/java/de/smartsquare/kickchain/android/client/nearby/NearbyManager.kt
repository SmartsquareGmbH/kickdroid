package de.smartsquare.kickchain.android.client.nearby

import android.app.Activity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessageListener
import com.google.android.gms.nearby.messages.MessagesClient
import com.google.android.gms.nearby.messages.PublishCallback
import com.google.android.gms.nearby.messages.PublishOptions
import com.google.android.gms.nearby.messages.StatusCallback
import com.google.android.gms.nearby.messages.SubscribeCallback
import com.google.android.gms.nearby.messages.SubscribeOptions
import com.google.android.gms.tasks.OnFailureListener
import de.smartsquare.kickchain.android.client.nearby.NearbyException.NearbyExceptionType

/**
 * @author Ruben Gees
 */
class NearbyManager {

    var searchingFoundListener: ((SearchingMessage) -> Unit)? = null
    var searchingLostListener: ((SearchingMessage) -> Unit)? = null
    var errorListener: ((NearbyException) -> Unit)? = null

    private var client: MessagesClient? = null
    private var currentMessage: Message? = null

    private var messageListener: MessageListener? = null
    private var failureListener: OnFailureListener? = null
    private var statusCallback: StatusCallback? = null
    private var publishCallback: PublishCallback? = null
    private var subscribeCallback: SubscribeCallback? = null

    fun subscribe(activity: Activity) {
        unsubscribe()

        messageListener = object : MessageListener() {
            override fun onFound(newMessage: Message) {
                if (newMessage.type == MessageType.SEARCHING.name) {
                    searchingFoundListener?.invoke(SearchingMessage.fromNearbyMessage(newMessage))
                }
            }

            override fun onLost(oldMessage: Message) {
                if (oldMessage.type == MessageType.SEARCHING.name) {
                    searchingLostListener?.invoke(SearchingMessage.fromNearbyMessage(oldMessage))
                }
            }
        }

        failureListener = OnFailureListener {
            if (it is ApiException) {
                errorListener?.invoke(NearbyException(NearbyExceptionType.API, it.message, it))
            } else {
                errorListener?.invoke(NearbyException(NearbyExceptionType.UNKNOWN, it.message, it))
            }
        }

        statusCallback = object : StatusCallback() {
            override fun onPermissionChanged(granted: Boolean) {
                if (!granted) {
                    errorListener?.invoke(NearbyException(NearbyExceptionType.PERMISSION))
                }
            }
        }

        publishCallback = object : PublishCallback() {
            override fun onExpired() {
                errorListener?.invoke(NearbyException(NearbyExceptionType.PERMISSION))
            }
        }

        subscribeCallback = object : SubscribeCallback() {
            override fun onExpired() {
                errorListener?.invoke(NearbyException(NearbyExceptionType.EXPIRED))
            }
        }

        client = Nearby.getMessagesClient(activity).also {
            statusCallback?.let { statusCallback -> it.registerStatusCallback(statusCallback) }

            messageListener?.let { listener ->
                val options = SubscribeOptions.Builder().setCallback(subscribeCallback).build()

                it.subscribe(listener, options)
                    .also { task -> failureListener?.let { task.addOnFailureListener(it) } }
            }
        }
    }

    fun unsubscribe() {
        currentMessage?.let { client?.unpublish(it) }
        messageListener?.let { client?.unsubscribe(it) }
        statusCallback?.let { client?.unregisterStatusCallback(it) }

        client = null
        currentMessage = null
        messageListener = null
        failureListener = null
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
                    .also { task -> failureListener?.let { task.addOnFailureListener(it) } }
            }
        }
    }
}
