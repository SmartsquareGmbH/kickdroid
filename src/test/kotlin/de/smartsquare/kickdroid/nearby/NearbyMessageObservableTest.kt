package de.smartsquare.kickdroid.nearby

import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessageListener
import com.google.android.gms.nearby.messages.MessagesClient
import de.smartsquare.resourceText
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * @author Ruben Gees
 */
class NearbyMessageObservableTest {

    private val internalNearbyClientListener = slot<MessageListener>()

    private lateinit var nearbyClient: MessagesClient

    @BeforeEach
    fun setUp() {
        nearbyClient = mockk()

        every { nearbyClient.subscribe(capture(internalNearbyClientListener)) } returns mockk()
        every { nearbyClient.unsubscribe(any<MessageListener>()) } returns mockk()
    }

    @Test
    fun `subscribing and emitting`() {
        val observable = NearbyMessageObservable(nearbyClient).test()
        val message = Message(resourceText("idle.json").toByteArray(), "IDLE")

        internalNearbyClientListener.captured.onFound(message)
        internalNearbyClientListener.captured.onLost(message)

        observable
            .assertValueCount(2)
            .assertValueAt(0) { it is NearbyEvent.Found && it.message.type == message.type }
            .assertValueAt(1) { it is NearbyEvent.Lost && it.message.type == message.type }
            .assertNotComplete()

        verify(exactly = 1) { nearbyClient.subscribe(any<MessageListener>()) }
    }

    @Test
    fun disposing() {
        NearbyMessageObservable(nearbyClient).test(true)

        verify(exactly = 1) { nearbyClient.subscribe(any<MessageListener>()) }
        verify(exactly = 1) { nearbyClient.unsubscribe(any<MessageListener>()) }
    }
}
