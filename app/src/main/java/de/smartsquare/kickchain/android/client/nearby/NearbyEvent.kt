package de.smartsquare.kickchain.android.client.nearby

import com.google.android.gms.nearby.messages.Message

/**
 * @author Ruben Gees
 */
sealed class NearbyEvent {
    class Found(val message: Message) : NearbyEvent()
    class Lost(val message: Message) : NearbyEvent()
}
