package de.smartsquare.kickdroid.statistics

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.smartsquare.kickdroid.kickway.KickwayApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * @author Ruben Gees
 */
class PlayerViewModel(private val kickwayApi: KickwayApi) : ViewModel() {

    val statisticSuccess = MutableLiveData<PlayerStatistic>()
    val statisticError = MutableLiveData<Throwable>()
    val statisticLoading = MutableLiveData<Unit>()

    private var statisticDisposable: Disposable? = null

    override fun onCleared() {
        statisticDisposable?.dispose()

        super.onCleared()
    }

    fun loadStatistic(playerName: String) {
        statisticDisposable?.dispose()

        statisticDisposable = kickwayApi.playerStatistic(playerName)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { statisticLoading.value = Unit }
            .doAfterTerminate { statisticLoading.value = null }
            .subscribe({
                statisticSuccess.value = it
                statisticError.value = null
            }, {
                statisticSuccess.value = null
                statisticError.value = it
            })
    }
}
