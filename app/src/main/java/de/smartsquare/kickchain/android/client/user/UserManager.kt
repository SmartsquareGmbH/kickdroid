package de.smartsquare.kickchain.android.client.user

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * @author Ruben Gees
 */
object UserManager {

    private const val NAME_KEY = "user_name"
    private const val ID_KEY = "user_id"

    private var listeners = listOf<ListenerWrapper>()

    fun getUser(context: Context): User? {
        return getUser(getPreferences(context))
    }

    fun setUser(context: Context, user: User?) {
        getPreferences(context).also {
            if (user == null) {
                it.edit()
                    .remove(ID_KEY)
                    .remove(NAME_KEY)
                    .apply()
            } else {
                it.edit()
                    .putString(ID_KEY, user.id)
                    .putString(NAME_KEY, user.name)
                    .apply()
            }
        }
    }

    fun registerUserChangeListener(context: Context, listener: (User?) -> Unit) {
        val listenerWrapper = ListenerWrapper(listener).also { listeners = listeners.plus(it) }

        getPreferences(context).registerOnSharedPreferenceChangeListener(listenerWrapper)
    }

    fun unregisterUserChangeListener(context: Context, listener: (User?) -> Unit) {
        val listenerWrapper = listeners.find { it.isWrapping(listener) }

        if (listenerWrapper != null) {
            getPreferences(context).unregisterOnSharedPreferenceChangeListener(listenerWrapper)

            listeners = listeners.minus(listenerWrapper)
        }
    }

    private fun getUser(preferences: SharedPreferences): User? {
        return preferences.let {
            val id = it.getString(ID_KEY, null)
            val name = it.getString(NAME_KEY, null)

            if (id == null || name == null) {
                null
            } else {
                User(id, name)
            }
        }
    }

    private fun getPreferences(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

    class ListenerWrapper(
        private val internalListener: (User?) -> Unit
    ) : SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
            if (key == NAME_KEY) {
                internalListener.invoke(getUser(preferences))
            }
        }

        fun isWrapping(listener: (User?) -> Unit) = internalListener == listener
    }
}
