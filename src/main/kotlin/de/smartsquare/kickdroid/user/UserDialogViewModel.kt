package de.smartsquare.kickdroid.user

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.smartsquare.kickdroid.kickway.KickwayApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * @author Ruben Gees
 */
class UserDialogViewModel(private val userManager: UserManager, private val kickwayApi: KickwayApi) : ViewModel() {

    val authorizationSuccess = MutableLiveData<Unit>()
    val authorizationError = MutableLiveData<Throwable>()
    val authorizationLoading = MutableLiveData<Unit>()

    private var authorizationDisposable: Disposable? = null

    override fun onCleared() {
        authorizationDisposable?.dispose()

        super.onCleared()
    }

    fun authorize(user: User) {
        authorizationDisposable?.dispose()

        authorizationDisposable = kickwayApi.authorize(user)
            .doOnComplete { userManager.user = user }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { authorizationLoading.value = Unit }
            .doOnTerminate { authorizationLoading.value = null }
            .subscribe({
                authorizationSuccess.value = Unit
            }, {
                authorizationError.value = it
            })
    }
}
