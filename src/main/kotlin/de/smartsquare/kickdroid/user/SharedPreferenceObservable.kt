package de.smartsquare.kickdroid.user

import android.content.SharedPreferences
import de.smartsquare.kickdroid.checkMainThread
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

/**
 * @author Ruben Gees
 */
class SharedPreferenceObservable(private val preferences: SharedPreferences) :
    Observable<String>() {

    override fun subscribeActual(observer: Observer<in String>) {
        if (!observer.checkMainThread()) {
            return
        }

        val listener =
            Listener(preferences, observer)

        observer.onSubscribe(listener)
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    private class Listener(
        private val preferences: SharedPreferences,
        private val observer: Observer<in String>
    ) : MainThreadDisposable(), SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
            if (!isDisposed) {
                observer.onNext(key)
            }
        }

        override fun onDispose() {
            preferences.unregisterOnSharedPreferenceChangeListener(this)
        }
    }
}
