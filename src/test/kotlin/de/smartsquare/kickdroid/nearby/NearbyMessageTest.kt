package de.smartsquare.kickdroid.nearby

import com.google.android.gms.nearby.messages.Message
import com.gregwoodfill.assert.shouldStrictlyEqualJson
import com.squareup.moshi.Moshi
import de.smartsquare.resourceText
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

/**
 * @author Ruben Gees
 */
internal class NearbyMessageTest {

    internal val moshi = Moshi.Builder().build()

    @Test
    internal fun `converting idle native message to nearby message`() {
        val nativeMessage = Message(resourceText("idle.json").toByteArray(), "IDLE")

        val nearbyMessage = nativeMessage.toNearbyMessage(moshi) as NearbyMessage.Idle

        nearbyMessage.raspberryId shouldBe 123
    }

    @Test
    internal fun `converting invalid idle native message to nearby message`() {
        val nativeMessage = Message(resourceText("invalid.json").toByteArray(), "IDLE")

        val nearbyMessageFunction = { nativeMessage.toNearbyMessage(moshi) }

        nearbyMessageFunction shouldThrow NearbyInvalidMessageException::class
    }

    @Test
    internal fun `converting unknown native message to nearby message`() {
        val nativeMessage = Message("".toByteArray(), "UNKNOWN")

        val nearbyMessageFunction = { nativeMessage.toNearbyMessage(moshi) }

        nearbyMessageFunction shouldThrow NearbyUnknownMessageException::class
    }

    @Test
    internal fun `converting idle nearby message to native message`() {
        val nearbyMessage = NearbyMessage.Idle(123)

        val nativeMessage = nearbyMessage.toNativeMessage(moshi)

        nativeMessage.type shouldBeEqualTo "IDLE"
        nativeMessage.content.toString(Charsets.UTF_8) shouldStrictlyEqualJson resourceText("idle.json")
    }
}
