package de.smartsquare.kickdroid.nearby

import android.util.Log
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.squareup.moshi.Moshi
import io.reactivex.Observable
import io.reactivex.functions.Predicate
import io.reactivex.subjects.PublishSubject

/**
 * @author Ruben Gees
 */
class NearbyManager(
    private val nativeClient: ConnectionsClient,
    private val moshi: Moshi,
    private val serviceId: String
) {

    val foundEndpoints get() = internalFoundEndpoints
    val connectedEndpoints get() = internalConnectedEndpoints

    val discovered: Observable<DiscoveryEvent> get() = internalDiscoverySubject.hide()
    val connected: Observable<Unit> get() = internalConnectionSubject.hide()
    val messages: Observable<NearbyMessage> get() = internalMessageSubject.hide()

    private val internalFoundEndpoints = mutableListOf<String>()
    private val internalConnectedEndpoints = mutableListOf<String>()

    private val internalDiscoverySubject = PublishSubject.create<DiscoveryEvent>()
    private val internalConnectionSubject = PublishSubject.create<Unit>()
    private val internalMessageSubject = PublishSubject.create<NearbyMessage>()

    fun discover(discoveryOptions: DiscoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()) {
        nativeClient.startDiscovery(serviceId, object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, discoveredEndpointInfo: DiscoveredEndpointInfo) {
                internalFoundEndpoints += endpointId

                internalDiscoverySubject.onNext(DiscoveryEvent.Found(endpointId))
            }

            override fun onEndpointLost(endpointId: String) {
                internalConnectedEndpoints -= endpointId
                internalFoundEndpoints -= endpointId

                internalDiscoverySubject.onNext(DiscoveryEvent.Lost(endpointId))
            }
        }, discoveryOptions).addOnFailureListener {
            Log.e("kickdroid", "Could not start discovery: $it")
        }
    }

    fun connect(nickname: String, endpointId: String, accept: Predicate<String> = Predicate { true }) {
        nativeClient.requestConnection(nickname, endpointId, object : ConnectionLifecycleCallback() {
            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        internalConnectedEndpoints += endpointId
                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                        // TODO
                    }
                    ConnectionsStatusCodes.STATUS_ERROR -> {
                        // TODO
                    }
                }
            }

            override fun onDisconnected(endpointId: String) {
                internalConnectedEndpoints -= endpointId
            }

            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                if (accept.test(endpointId)) {
                    nativeClient.acceptConnection(endpointId, object : PayloadCallback() {
                        override fun onPayloadReceived(endpointId: String, payload: Payload) {
                            try {
                                internalMessageSubject.onNext(payload.toNearbyMessage(moshi))
                            } catch (exception: NearbyException) {
                                internalMessageSubject.onError(exception)
                            }
                        }

                        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
                    })
                }
            }
        }).addOnFailureListener {
            Log.e("kickdroid", "Could not start connecting: $it")
        }
    }

    fun destroy() {
        nativeClient.stopDiscovery()
        nativeClient.stopAdvertising()
        nativeClient.stopAllEndpoints()

        internalFoundEndpoints.clear()
        internalConnectedEndpoints.clear()
    }

    sealed class DiscoveryEvent(val endpointId: String) {
        class Found(endpointId: String) : DiscoveryEvent(endpointId)
        class Lost(endpointId: String) : DiscoveryEvent(endpointId)
    }
}
