package de.smartsquare.kickchain.android.client.nearby

import android.app.Activity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessageListener
import com.google.android.gms.nearby.messages.PublishCallback
import com.google.android.gms.nearby.messages.PublishOptions
import com.google.android.gms.nearby.messages.StatusCallback
import com.google.android.gms.nearby.messages.SubscribeCallback
import com.google.android.gms.nearby.messages.SubscribeOptions
import com.google.android.gms.tasks.OnFailureListener
import de.smartsquare.kickchain.android.client.nearby.NearbyException.NearbyExceptionType
import java.lang.ref.WeakReference

/**
 * @author Ruben Gees
 */
class NearbyManager private constructor(activity: Activity) {

    companion object {
        fun connect(activity: Activity): NearbyManager {
            return NearbyManager(activity)
        }
    }

    var searchingFoundListener: ((SearchingMessage) -> Unit)? = null
    var searchingLostListener: ((SearchingMessage) -> Unit)? = null
    var errorListener: ((NearbyException) -> Unit)? = null

    private val currentActivity = WeakReference(activity)

    private var isConnected = false

    private var currentMessage: Message? = null
    private var enqueuedMessage: Message? = null

    private var messageListener: MessageListener? = null
    private var failureListener: OnFailureListener? = null
    private var statusCallback: StatusCallback? = null
    private var publishCallback: PublishCallback? = null
    private var subscribeCallback: SubscribeCallback? = null

    init {
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
                // 17 Means the user has cancelled the permission dialog.
                // I have not found a Constants class for this yet.

                if (it.statusCode == 17) {
                    errorListener?.invoke(NearbyException(NearbyExceptionType.PERMISSION))
                } else {
                    errorListener?.invoke(NearbyException(NearbyExceptionType.API))
                }
            } else {
                errorListener?.invoke(NearbyException(NearbyExceptionType.UNKNOWN))
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
                currentMessage?.also { publish(it) }
            }
        }

        subscribeCallback = object : SubscribeCallback() {
            override fun onExpired() {
                subscribe()
            }
        }

        subscribe()
    }

    fun disconnect() {
        val client = currentActivity.get()?.let { Nearby.getMessagesClient(it) }

        currentMessage?.let { client?.unpublish(it) }
        messageListener?.let { client?.unsubscribe(it) }
        statusCallback?.let { client?.unregisterStatusCallback(it) }

        isConnected = false

        // Android is pain.
        searchingFoundListener = null
        searchingLostListener = null
        errorListener = null

        currentMessage = null
        enqueuedMessage = null
        messageListener = null
        failureListener = null
        statusCallback = null
        publishCallback = null
        subscribeCallback = null
    }

    fun search(newMessage: SearchingMessage) {
        publish(newMessage.toNearbyMessage())
    }

    private fun subscribe() {
        val safeActivity = currentActivity.get()
        val safeMessageListener = messageListener

        if (safeActivity != null && safeMessageListener != null) {
            Nearby.getMessagesClient(safeActivity).also { newClient ->
                val options = SubscribeOptions.Builder()
                    .apply { subscribeCallback?.let { setCallback(subscribeCallback) } }
                    .build()

                newClient.subscribe(safeMessageListener, options).also { task ->
                    failureListener?.let { task.addOnFailureListener(it) }

                    task.addOnSuccessListener {
                        isConnected = true

                        statusCallback?.let { statusCallback -> newClient.registerStatusCallback(statusCallback) }
                        enqueuedMessage?.let { publish(it) }
                    }
                }
            }
        }
    }

    private fun publish(message: Message) {
        val client = currentActivity.get()?.let { Nearby.getMessagesClient(it) }

        if (isConnected && client != null) {
            currentMessage?.let { client.unpublish(it) }

            currentMessage = message
            enqueuedMessage = null

            val options = PublishOptions.Builder()
                .apply { publishCallback?.let { setCallback(publishCallback) } }
                .build()

            client.publish(message, options).also { task ->
                failureListener?.let {
                    currentMessage = null

                    task.addOnFailureListener(it)
                }
            }
        } else {
            enqueuedMessage = message
        }
    }
}
