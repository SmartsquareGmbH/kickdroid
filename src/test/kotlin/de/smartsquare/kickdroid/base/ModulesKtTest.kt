package de.smartsquare.kickdroid.base

import android.app.Activity
import android.content.SharedPreferences
import com.google.android.gms.nearby.messages.MessagesClient
import de.smartsquare.KoinExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.android.ext.koin.with
import org.koin.dsl.module.applicationContext
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.test.KoinTest
import org.koin.test.dryRun

/**
 * @author Ruben Gees
 */
@ExtendWith(KoinExtension::class)
class ModulesKtTest : KoinTest {

    private val testModules = modules + applicationContext {
        factory { mockk<MessagesClient>() }
        bean { mockk<SharedPreferences>() }
    }

    @Test
    fun `koin dry run`() {
        startKoin(testModules) with mockk()
        dryRun { mapOf(ACTIVITY_PARAMETER to mockk<Activity>()) }
    }
}
