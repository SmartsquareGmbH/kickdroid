package de.smartsquare

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.standalone.StandAloneContext.stopKoin

/**
 * @author Ruben Gees
 */
class KoinExtension : AfterEachCallback {

    override fun afterEach(context: ExtensionContext) {
        stopKoin()
    }
}
