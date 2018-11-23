package de.smartsquare.kickdroid.statistics

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.smartsquare.kickdroid.kickway.KickwayApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * @author Ruben Gees
 */
class StatisticsViewModel(private val kickwayApi: KickwayApi) : ViewModel() {

    val statisticsSuccess = MutableLiveData<List<Player>>()
    val statisticsError = MutableLiveData<Throwable>()
    val statisticsLoading = MutableLiveData<Unit>()

    private var statisticsDisposable: Disposable? = null

    override fun onCleared() {
        statisticsDisposable?.dispose()

        super.onCleared()
    }

    fun loadStatistics(type: StatisticsType) {
        statisticsDisposable?.dispose()

        statisticsDisposable = type.toApiSingle()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { statisticsLoading.value = Unit }
            .doAfterTerminate { statisticsLoading.value = null }
            .subscribe({
                statisticsSuccess.value = it
                statisticsError.value = null
            }, {
                statisticsSuccess.value = null
                statisticsError.value = it
            })
    }

    private fun StatisticsType.toApiSingle() = when (this) {
        StatisticsType.SOLO -> kickwayApi.soloQStatistics()
        StatisticsType.DUO -> kickwayApi.duoQStatistics()
        StatisticsType.FLEX -> kickwayApi.flexQStatistics()
    }
}
