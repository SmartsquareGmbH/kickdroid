package de.smartsquare.kickdroid.user

import android.content.SharedPreferences
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Ruben Gees
 */
class SharedPreferenceObservable(private val preferences: SharedPreferences) :
    Observable<String>() {

    override fun subscribeActual(observer: Observer<in String>) {
        val listener = Listener(preferences, observer)

        observer.onSubscribe(listener)
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    private class Listener(
        private val preferences: SharedPreferences,
        private val observer: Observer<in String>
    ) : SharedPreferences.OnSharedPreferenceChangeListener, Disposable {

        private val unsubscribed = AtomicBoolean()

        override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
            if (!isDisposed) {
                observer.onNext(key)
            }
        }

        override fun isDisposed(): Boolean {
            return unsubscribed.get()
        }

        override fun dispose() {
            if (unsubscribed.compareAndSet(false, true)) {
                preferences.unregisterOnSharedPreferenceChangeListener(this)
            }
        }
    }
}
