package de.smartsquare.kickdroid.user

import android.content.SharedPreferences
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import io.reactivex.Observable

/**
 * @author Ruben Gees
 */
class UserManager(private val preferences: SharedPreferences) {

    private companion object {
        private const val ID_KEY = "user_id"
        private const val NAME_KEY = "user_name"
    }

    var user: User?
        get() {
            val id = preferences.getString(ID_KEY, null)
            val name = preferences.getString(NAME_KEY, null)

            return if (id == null || name == null) {
                null
            } else {
                User(id, name)
            }
        }
        set(value) {
            if (value == null) {
                preferences.edit()
                    .remove(ID_KEY)
                    .remove(NAME_KEY)
                    .apply()
            } else {
                preferences.edit()
                    .putString(ID_KEY, value.id)
                    .putString(NAME_KEY, value.name)
                    .apply()
            }
        }

    fun userChanges(): Observable<Optional<User>> {
        return SharedPreferenceObservable(preferences)
            .filter { it == ID_KEY }
            .map { key -> user.toOptional() }
    }
}
