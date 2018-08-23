package de.smartsquare.kickdroid.nearby

import com.google.android.gms.nearby.messages.Message

/**
 * @author Ruben Gees
 */
sealed class NearbyEvent {
    class Found(val message: Message) : NearbyEvent()
    class Lost(val message: Message) : NearbyEvent()
}
