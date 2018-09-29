package de.smartsquare.kickdroid

import android.os.Looper
import androidx.multidex.MultiDexApplication
import com.kirillr.strictmodehelper.StrictModeCompat
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.Iconics
import com.squareup.leakcanary.LeakCanary
import de.smartsquare.kickdroid.base.modules
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import org.koin.android.ext.android.startKoin

/**
 * @author Ruben Gees
 */
class MainApplication : MultiDexApplication() {

    companion object {
        const val LOGGING_TAG = "kickdroid"
    }

    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }

        if (BuildConfig.DEBUG) {
            val threadPolicy = StrictModeCompat.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()

            val vmPolicy = StrictModeCompat.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()

            StrictModeCompat.setPolicies(threadPolicy, vmPolicy)
        }

        LeakCanary.install(this)
        Iconics.registerFont(CommunityMaterial())

        RxAndroidPlugins.setInitMainThreadSchedulerHandler { AndroidSchedulers.from(Looper.getMainLooper(), true) }
        RxAndroidPlugins.setMainThreadSchedulerHandler { AndroidSchedulers.from(Looper.getMainLooper(), true) }

        startKoin(this, modules)
    }
}
