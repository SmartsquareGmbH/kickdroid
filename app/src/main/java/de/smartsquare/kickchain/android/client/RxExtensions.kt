package de.smartsquare.kickchain.android.client

import android.os.Looper
import io.reactivex.Observer
import io.reactivex.disposables.Disposables

fun Observer<*>.checkMainThread() = if (Looper.myLooper() != Looper.getMainLooper()) {
    onSubscribe(Disposables.empty())
    onError(IllegalStateException("Expected to be called on the main thread but was ${Thread.currentThread().name}"))

    false
} else {
    true
}
