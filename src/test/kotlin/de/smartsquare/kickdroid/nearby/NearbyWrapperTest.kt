package de.smartsquare.kickdroid.nearby

import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessageListener
import com.google.android.gms.nearby.messages.MessagesClient
import com.squareup.moshi.Moshi
import de.smartsquare.resourceText
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * @author Ruben Gees
 */
class NearbyWrapperTest {

    private val moshi = Moshi.Builder().build()
    private val internalNearbyClientListener = slot<MessageListener>()

    private val nearbyClient = mockk<MessagesClient>()
    private val nearbyWrapper = NearbyWrapper(nearbyClient, moshi)

    @BeforeEach
    fun setUp() {
        every { nearbyClient.publish(any()) } returns mockk()
        every { nearbyClient.unpublish(any()) } returns mockk()
        every { nearbyClient.subscribe(capture(internalNearbyClientListener)) } returns mockk()
    }

    @Test
    fun `sending a message`() {
        val expectedJson = "{\"raspberryId\":123}"

        nearbyWrapper.send(NearbyMessage.Idle(123))

        verify(exactly = 1) {
            nearbyClient.publish(match {
                it.type == "IDLE" && it.content.toString(Charsets.UTF_8) == expectedJson
            })
        }
    }

    @Test
    fun `sending two messages unpublishes the first`() {
        val expectedJsonFirst = "{\"raspberryId\":123}"
        val expectedJsonSecond = "{\"raspberryId\":321}"

        nearbyWrapper.send(NearbyMessage.Idle(123))
        nearbyWrapper.send(NearbyMessage.Idle(321))

        verifySequence {
            nearbyClient.publish(match {
                it.type == "IDLE" && it.content.toString(Charsets.UTF_8) == expectedJsonFirst
            })

            nearbyClient.unpublish(match {
                it.type == "IDLE" && it.content.toString(Charsets.UTF_8) == expectedJsonFirst
            })

            nearbyClient.publish(match {
                it.type == "IDLE" && it.content.toString(Charsets.UTF_8) == expectedJsonSecond
            })
        }
    }

    @Test
    fun `receiving found messages`() {
        val observable = nearbyWrapper.foundMessages().test()

        internalNearbyClientListener.captured.onFound(
            Message(resourceText("idle.json").toByteArray(), "IDLE")
        )

        internalNearbyClientListener.captured.onLost(
            Message(resourceText("idle.json").toByteArray(), "IDLE")
        )

        observable
            .assertValue { it is NearbyMessage.Idle && it.raspberryId == 123L }
            .assertNotComplete()
    }

    @Test
    fun `receiving lost messages`() {
        val observable = nearbyWrapper.lostMessages().test()

        internalNearbyClientListener.captured.onFound(
            Message(resourceText("idle.json").toByteArray(), "IDLE")
        )

        internalNearbyClientListener.captured.onLost(
            Message(resourceText("idle.json").toByteArray(), "IDLE")
        )

        observable
            .assertValue { it is NearbyMessage.Idle && it.raspberryId == 123L }
            .assertNotComplete()
    }
}
