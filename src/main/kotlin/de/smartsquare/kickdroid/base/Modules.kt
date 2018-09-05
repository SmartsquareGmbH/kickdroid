package de.smartsquare.kickdroid.base

import android.app.Activity
import android.preference.PreferenceManager
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.squareup.moshi.Moshi
import de.smartsquare.kickdroid.BuildConfig.KICKWAY_URL
import de.smartsquare.kickdroid.kickway.KickwayApi
import de.smartsquare.kickdroid.statistics.StatisticsViewModel
import de.smartsquare.kickdroid.user.UserDialogViewModel
import de.smartsquare.kickdroid.user.UserManager
import de.smartsquare.kickprotocol.Kickprotocol
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

private val network = module {
    single { Moshi.Builder().build() }

    single {
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .baseUrl(KICKWAY_URL)
            .build()
    }

    single { get<Retrofit>().create(KickwayApi::class.java) }
}

private val kickprotocol = module {
    factory { (activity: Activity) -> Nearby.getConnectionsClient(activity) }
    factory { Kickprotocol(get<ConnectionsClient>(), get()) }
}

private val user = module {
    single { PreferenceManager.getDefaultSharedPreferences(androidContext()) }
    single { UserManager(get()) }
    viewModel { UserDialogViewModel(get(), get()) }
}

private val statistics = module {
    viewModel { StatisticsViewModel(get()) }
}

val modules = listOf(
    network,
    kickprotocol,
    user,
    statistics
)
