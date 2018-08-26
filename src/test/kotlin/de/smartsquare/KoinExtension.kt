package de.smartsquare

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.Koin
import org.koin.standalone.StandAloneContext.closeKoin

/**
 * @author Ruben Gees
 */
class KoinExtension : AfterEachCallback {

    override fun afterEach(context: ExtensionContext) {
        Koin.logger.log("AutoClose Koin")
        closeKoin()
    }
}
