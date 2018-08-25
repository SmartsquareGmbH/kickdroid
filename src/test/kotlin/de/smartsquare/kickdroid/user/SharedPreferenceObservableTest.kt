package de.smartsquare.kickdroid.user

import android.content.SharedPreferences
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * @author Ruben Gees
 */
class SharedPreferenceObservableTest {

    private val preferenceListener = slot<SharedPreferences.OnSharedPreferenceChangeListener>()
    private val preferences = mockk<SharedPreferences>()

    @BeforeEach
    fun setUp() {
        every { preferences.registerOnSharedPreferenceChangeListener(capture(preferenceListener)) } just Runs
        every { preferences.unregisterOnSharedPreferenceChangeListener(any()) } just Runs
    }

    @Test
    fun `subscribing and emitting`() {
        val observable = SharedPreferenceObservable(preferences).test()

        preferenceListener.captured.onSharedPreferenceChanged(preferences, "firstKey")
        preferenceListener.captured.onSharedPreferenceChanged(preferences, "secondKey")

        observable.assertValueSequenceOnly(listOf("firstKey", "secondKey"))

        verify(exactly = 1) { preferences.registerOnSharedPreferenceChangeListener(any()) }
    }

    @Test
    fun disposing() {
        SharedPreferenceObservable(preferences).test(true)

        verify(exactly = 1) { preferences.registerOnSharedPreferenceChangeListener(any()) }
        verify(exactly = 1) { preferences.unregisterOnSharedPreferenceChangeListener(any()) }
    }
}
