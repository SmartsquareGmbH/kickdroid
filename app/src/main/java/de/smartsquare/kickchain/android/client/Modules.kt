package de.smartsquare.kickchain.android.client

import android.app.Activity
import android.preference.PreferenceManager
import com.google.android.gms.nearby.Nearby
import com.squareup.moshi.Moshi
import de.smartsquare.kickchain.android.client.nearby.NearbyWrapper
import de.smartsquare.kickchain.android.client.user.UserManager
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext

const val ACTIVITY_PARAMETER = "activity"

val moshi = applicationContext {
    bean { Moshi.Builder().build() }
}

val nearby = applicationContext {
    factory { params -> Nearby.getMessagesClient(params[ACTIVITY_PARAMETER]) }
    factory { params -> NearbyWrapper(get(parameters = { params.values }), get()) }
}

val user = applicationContext {
    bean { PreferenceManager.getDefaultSharedPreferences(androidApplication()) }
    bean { UserManager(get()) }
}

inline fun <reified T> Activity.activityInject(): Lazy<T> =
    inject(parameters = { mapOf(ACTIVITY_PARAMETER to this) })
