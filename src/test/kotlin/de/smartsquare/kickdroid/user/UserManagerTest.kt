package de.smartsquare.kickdroid.user

import android.content.SharedPreferences
import com.gojuno.koptional.None
import com.gojuno.koptional.toOptional
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * @author Ruben Gees
 */
class UserManagerTest {

    private val preferenceListener = slot<SharedPreferences.OnSharedPreferenceChangeListener>()

    private val preferences = mockk<SharedPreferences>()
    private val preferencesEditor = mockk<SharedPreferences.Editor>()
    private val userManager = UserManager(preferences)

    @BeforeEach
    fun setUp() {
        every { preferences.edit() } returns preferencesEditor
        every { preferences.getString("user_id", null) } returns "123"
        every { preferences.getString("user_name", null) } returns "test"

        every { preferences.registerOnSharedPreferenceChangeListener(capture(preferenceListener)) } just Runs
        every { preferences.unregisterOnSharedPreferenceChangeListener(any()) } just Runs

        every { preferencesEditor.putString(any(), any()) } returns preferencesEditor
        every { preferencesEditor.remove(any()) } returns preferencesEditor
        every { preferencesEditor.apply() } just Runs
    }

    @Test
    fun `getting user`() {
        userManager.user shouldEqual User("123", "test")
    }

    @Test
    fun `getting incomplete user`() {
        every { preferences.getString("user_name", null) } returns null

        userManager.user shouldEqual null
    }

    @Test
    fun `setting user`() {
        userManager.user = User("321", "abc")

        verify(exactly = 1) { preferencesEditor.putString("user_id", "321") }
        verify(exactly = 1) { preferencesEditor.putString("user_name", "abc") }
        verify(exactly = 1) { preferencesEditor.apply() }
    }

    @Test
    fun `setting user to null`() {
        userManager.user = null

        verify(exactly = 1) { preferencesEditor.remove("user_id") }
        verify(exactly = 1) { preferencesEditor.remove("user_name") }
        verify(exactly = 1) { preferencesEditor.apply() }
    }

    @Test
    fun `receiving user changes`() {
        val observable = userManager.userChanges().test()
        val expectedUser = User("123", "test").toOptional()

        preferenceListener.captured.onSharedPreferenceChanged(preferences, "user_id")
        preferenceListener.captured.onSharedPreferenceChanged(preferences, "user_name")

        observable.assertValue(expectedUser)
    }

    @Test
    fun `receiving user changes with null user`() {
        every { preferences.getString("user_name", null) } returns null

        val observable = userManager.userChanges().test()

        preferenceListener.captured.onSharedPreferenceChanged(preferences, "user_id")

        observable
            .assertValue(None)
            .assertNotComplete()
    }
}
