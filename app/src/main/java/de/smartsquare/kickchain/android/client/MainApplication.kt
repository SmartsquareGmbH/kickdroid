package de.smartsquare.kickchain.android.client

import android.app.Application
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.Iconics
import com.squareup.leakcanary.LeakCanary
import org.koin.android.ext.android.startKoin

/**
 * @author Ruben Gees
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }

        LeakCanary.install(this)
        Iconics.registerFont(CommunityMaterial())

        startKoin(this, listOf(moshi, nearby, user))
    }
}
