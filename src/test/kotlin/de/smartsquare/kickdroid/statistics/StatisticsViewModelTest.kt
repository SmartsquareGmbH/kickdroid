package de.smartsquare.kickdroid.statistics

import androidx.lifecycle.Observer
import de.smartsquare.InstantTaskExecutorExtension
import de.smartsquare.KoinExtension
import de.smartsquare.RxJavaExtension
import de.smartsquare.kickdroid.base.modules
import de.smartsquare.kickdroid.kickway.KickwayApi
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifySequence
import io.reactivex.Single
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.koin.android.ext.koin.with
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext
import org.koin.standalone.inject
import org.koin.test.KoinTest
import java.io.IOException

/**
 * @author Ruben Gees
 */
@ExtendWith(InstantTaskExecutorExtension::class, RxJavaExtension::class, KoinExtension::class)
class StatisticsViewModelTest : KoinTest {

    private val api = mockk<KickwayApi>()
    private val viewModel by inject<StatisticsViewModel>()

    private val testModules = modules + module {
        single(override = true) { api }
    }

    @BeforeEach
    fun setUp() {
        StandAloneContext.startKoin(testModules) with mockk()
    }

    @ParameterizedTest(name = "with type {arguments}")
    @EnumSource(StatisticsType::class)
    fun `loading statistics`(type: StatisticsType) {
        val successObserver = spyk<Observer<List<Player>>>()
        val loadingObserver = spyk<Observer<Unit>>()
        val errorObserver = spyk<Observer<Throwable>>()

        val expectedPlayers = listOf(
            Player("Ruby", 2, 23),
            Player("Deen", 3, 43)
        )

        every { api.soloQStatistics() } returns Single.just(expectedPlayers)
        every { api.duoQStatistics() } returns Single.just(expectedPlayers)
        every { api.flexQStatistics() } returns Single.just(expectedPlayers)

        viewModel.statisticsSuccess.observeForever(successObserver)
        viewModel.statisticsLoading.observeForever(loadingObserver)
        viewModel.statisticsError.observeForever(errorObserver)

        viewModel.loadStatistics(type)

        verifySequence {
            loadingObserver.onChanged(Unit)
            loadingObserver.onChanged(null)
        }

        verify(exactly = 1) { successObserver.onChanged(expectedPlayers) }
        verify(exactly = 1) { errorObserver.onChanged(isNull()) }
    }

    @ParameterizedTest(name = "with type {arguments}")
    @EnumSource(StatisticsType::class)
    fun `loading statistics error`(type: StatisticsType) {
        val successObserver = spyk<Observer<List<Player>>>()
        val loadingObserver = spyk<Observer<Unit>>()
        val errorObserver = spyk<Observer<Throwable>>()
        val error = IOException()

        every { api.soloQStatistics() } returns Single.error(error)
        every { api.duoQStatistics() } returns Single.error(error)
        every { api.flexQStatistics() } returns Single.error(error)

        viewModel.statisticsSuccess.observeForever(successObserver)
        viewModel.statisticsLoading.observeForever(loadingObserver)
        viewModel.statisticsError.observeForever(errorObserver)

        viewModel.loadStatistics(type)

        verifySequence {
            loadingObserver.onChanged(Unit)
            loadingObserver.onChanged(null)
        }

        verify(exactly = 1) { successObserver.onChanged(isNull()) }
        verify(exactly = 1) { errorObserver.onChanged(error) }
    }
}
