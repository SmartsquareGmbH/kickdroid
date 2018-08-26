package de.smartsquare.kickdroid.user

import android.content.SharedPreferences
import androidx.lifecycle.Observer
import de.smartsquare.InstantTaskExecutorExtension
import de.smartsquare.KoinExtension
import de.smartsquare.RxJavaExtension
import de.smartsquare.kickdroid.base.modules
import de.smartsquare.kickdroid.kickway.KickwayApi
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifySequence
import io.reactivex.Completable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.android.ext.koin.with
import org.koin.dsl.module.applicationContext
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.inject
import org.koin.test.KoinTest
import java.io.IOException

/**
 * @author Ruben Gees
 */
@ExtendWith(InstantTaskExecutorExtension::class, RxJavaExtension::class, KoinExtension::class)
class UserDialogViewModelTest : KoinTest {

    private val api = mockk<KickwayApi>()
    private val viewModel by inject<UserDialogViewModel>()
    private val userManager by inject<UserManager>()

    private val testModules = modules + applicationContext {
        bean { api }
        bean { mockk<SharedPreferences>() }
        bean { mockk<UserManager>() }
    }

    @BeforeEach
    fun setUp() {
        startKoin(testModules) with mockk()

        every { userManager.user = any() } just Runs
    }

    @Test
    fun authorization() {
        val successObserver = spyk<Observer<Unit>>()
        val loadingObserver = spyk<Observer<Unit>>()
        val errorObserver = spyk<Observer<Throwable>>()
        val user = User("123", "Ruby")

        every { api.authorize(user) } returns Completable.complete()

        viewModel.authorizationSuccess.observeForever(successObserver)
        viewModel.authorizationLoading.observeForever(loadingObserver)
        viewModel.authorizationError.observeForever(errorObserver)

        viewModel.authorize(user)

        verifySequence {
            loadingObserver.onChanged(Unit)
            loadingObserver.onChanged(null)
        }

        verify(exactly = 1) { successObserver.onChanged(Unit) }
        verify(exactly = 0) { errorObserver.onChanged(any()) }
    }

    @Test
    fun `authorization error`() {
        val successObserver = spyk<Observer<Unit>>()
        val loadingObserver = spyk<Observer<Unit>>()
        val errorObserver = spyk<Observer<Throwable>>()
        val user = User("123", "Ruby")
        val error = IOException()

        every { api.authorize(user) } returns Completable.error(error)

        viewModel.authorizationSuccess.observeForever(successObserver)
        viewModel.authorizationLoading.observeForever(loadingObserver)
        viewModel.authorizationError.observeForever(errorObserver)

        viewModel.authorize(user)

        verifySequence {
            loadingObserver.onChanged(Unit)
            loadingObserver.onChanged(null)
        }

        verify(exactly = 0) { successObserver.onChanged(any()) }
        verify(exactly = 1) { errorObserver.onChanged(error) }
    }
}
