package de.smartsquare.kickdroid.base

import android.content.SharedPreferences
import com.google.android.gms.nearby.connection.ConnectionsClient
import de.smartsquare.KoinExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.android.ext.koin.with
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.test.KoinTest
import org.koin.test.checkModules

/**
 * @author Ruben Gees
 */
@ExtendWith(KoinExtension::class)
class ModulesKtTest : KoinTest {

    private val testModules = modules + module {
        single(override = true) { mockk<ConnectionsClient>() }
        single(override = true) { mockk<SharedPreferences>() }
    }

    @Test
    fun `koin check modules`() {
        startKoin(testModules) with mockk()
        checkModules(testModules)
    }
}
