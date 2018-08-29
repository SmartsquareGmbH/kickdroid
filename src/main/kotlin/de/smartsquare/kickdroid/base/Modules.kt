package de.smartsquare.kickdroid.base

import android.app.Activity
import android.preference.PreferenceManager
import com.google.android.gms.nearby.Nearby
import com.squareup.moshi.Moshi
import de.smartsquare.kickdroid.BuildConfig.KICKWAY_URL
import de.smartsquare.kickdroid.kickway.KickwayApi
import de.smartsquare.kickdroid.nearby.NearbyManager
import de.smartsquare.kickdroid.statistics.StatisticsViewModel
import de.smartsquare.kickdroid.user.UserDialogViewModel
import de.smartsquare.kickdroid.user.UserManager
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

const val ACTIVITY_PARAMETER = "activity"

private val network = applicationContext {
    bean { Moshi.Builder().build() }

    bean {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .baseUrl(KICKWAY_URL)
            .build()
    }

    bean { get<Retrofit>().create(KickwayApi::class.java) }
}

private val nearby = applicationContext {
    factory { params -> Nearby.getConnectionsClient(params[ACTIVITY_PARAMETER]) }
    factory { params -> NearbyManager(get(parameters = { params.values }), get(), "de.smartsquare.kickdroid") }
}

private val user = applicationContext {
    bean { PreferenceManager.getDefaultSharedPreferences(androidApplication()) }
    bean { UserManager(get()) }
    viewModel { UserDialogViewModel(get(), get()) }
}

private val statistics = applicationContext {
    viewModel { StatisticsViewModel(get()) }
}

val modules = listOf(
    network,
    nearby,
    user,
    statistics
)

inline fun <reified T> Activity.activityInject(): Lazy<T> = inject(parameters = { mapOf(ACTIVITY_PARAMETER to this) })