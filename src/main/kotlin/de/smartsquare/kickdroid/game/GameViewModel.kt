package de.smartsquare.kickdroid.game

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gojuno.koptional.None
import de.smartsquare.kickdroid.MainApplication.Companion.LOGGING_TAG
import de.smartsquare.kickdroid.user.UserManager
import de.smartsquare.kickprotocol.ConnectionEvent
import de.smartsquare.kickprotocol.DiscoveryEvent
import de.smartsquare.kickprotocol.Kickprotocol
import de.smartsquare.kickprotocol.Lobby
import de.smartsquare.kickprotocol.filterErrors
import de.smartsquare.kickprotocol.filterMessages
import de.smartsquare.kickprotocol.message.CreateGameMessage
import de.smartsquare.kickprotocol.message.JoinLobbyMessage
import de.smartsquare.kickprotocol.message.KickprotocolMessage
import de.smartsquare.kickprotocol.message.LeaveLobbyMessage
import de.smartsquare.kickprotocol.message.StartGameMessage
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * @author Ruben Gees
 */
class GameViewModel(private val kickprotocol: Kickprotocol, private val userManager: UserManager) : ViewModel() {

    private companion object {
        private val emptyLobby = Lobby("", "", emptyList(), emptyList(), 0, 0)
    }

    val state = MutableLiveData<GameState>().apply {
        value = if (userManager.user != null) GameState.SEARCHING else GameState.USER
    }

    val isLoading = MutableLiveData<Boolean>().apply { value = false }
    val error = MutableLiveData<Throwable>()
    val lobby = MutableLiveData<Lobby>().apply { value = emptyLobby }

    private val user
        get() = userManager.user ?: throw IllegalStateException("user cannot be null")

    private val disposables = CompositeDisposable()
    private var userDisposable: Disposable? = null
    private var discoveryDisposable: Disposable? = null

    init {
        disposables += userManager.userChanges()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it is None) {
                    state.value = GameState.USER

                    kickprotocol.stop()
                } else {
                    state.value = GameState.SEARCHING
                }
            }

        disposables += kickprotocol.discoveryEvents
            .filter { it is DiscoveryEvent.Found }
            .doOnNext { Log.d(LOGGING_TAG, "Found device: ${it.endpointId}") }
            .doOnNext { isLoading.value = true }
            .flatMapCompletable {
                if (kickprotocol.connectedEndpoints.isEmpty()) {
                    kickprotocol.connect(user.name, it.endpointId)
                        .doOnTerminate { isLoading.value = false }
                } else {
                    Completable.never()
                }
            }
            .subscribe({
                state.value = GameState.SEARCHING
                lobby.value = emptyLobby
            }, {
                error.value = it
            })

        disposables += kickprotocol.discoveryEvents
            .filter { it is DiscoveryEvent.Lost }
            .doOnNext { Log.d(LOGGING_TAG, "Lost device: ${it.endpointId}") }
            .subscribe()

        disposables += kickprotocol.connectionEvents
            .filter { it is ConnectionEvent.Connected }
            .doOnNext { Log.d(LOGGING_TAG, "Connected to device: ${it.endpointId}") }
            .subscribe()

        disposables += kickprotocol.connectionEvents
            .filter { it is ConnectionEvent.Disconnected }
            .doOnNext { Log.d(LOGGING_TAG, "Disconnected from device: ${it.endpointId}") }
            .subscribe {
                state.value = GameState.SEARCHING
                lobby.value = emptyLobby
            }

        disposables += kickprotocol.messageEvents
            .filterMessages()
            .subscribe { Log.d(LOGGING_TAG, "Received message: $it") }

        disposables += kickprotocol.messageEvents
            .filterErrors()
            .subscribe { isLoading.value = false }

        disposables += kickprotocol.idleMessageEvents
            .filterMessages()
            .doOnNext { isLoading.value = false }
            .subscribe { state.value = GameState.IDLE }

        disposables += kickprotocol.matchmakingMessageEvents
            .filterMessages()
            .doOnNext { isLoading.value = false }
            .subscribe {
                state.value = GameState.MATCHMAKING
                lobby.value = it.message.lobby
            }

        disposables += kickprotocol.playingMessageEvents
            .filterMessages()
            .doOnNext { isLoading.value = false }
            .subscribe {
                state.value = GameState.PLAYING
                lobby.value = it.message.lobby
            }
    }

    override fun onCleared() {
        disposables.dispose()
        discoveryDisposable?.dispose()
        kickprotocol.stop()

        discoveryDisposable = null
        userDisposable = null

        super.onCleared()
    }

    fun discover() {
        discoveryDisposable?.dispose()
        kickprotocol.stop()

        discoveryDisposable = kickprotocol.discover()
            .doOnSubscribe { isLoading.value = true }
            .doOnTerminate { isLoading.value = false }
            .subscribeBy(onError = { error.value = it })
    }

    fun createGame() {
        if (isLoading.value != true && kickprotocol.connectedEndpoints.isNotEmpty()) {
            disposables += kickprotocol
                .doSend(CreateGameMessage(user.id, user.name))
                .subscribeBy(onError = { error.value = it })
        }
    }

    fun joinTeam(position: JoinLobbyMessage.TeamPosition) {
        if (isLoading.value != true && kickprotocol.connectedEndpoints.isNotEmpty()) {
            disposables += kickprotocol
                .doSend(JoinLobbyMessage(user.id, user.name, position))
                .subscribeBy(onError = { error.value = it })
        }
    }

    fun leaveTeam() {
        if (isLoading.value != true && kickprotocol.connectedEndpoints.isNotEmpty()) {
            disposables += kickprotocol
                .doSend(LeaveLobbyMessage())
                .subscribeBy(onError = { error.value = it })
        }
    }

    fun startGame() {
        if (isLoading.value != true && kickprotocol.connectedEndpoints.isNotEmpty()) {
            disposables += kickprotocol
                .doSend(StartGameMessage())
                .subscribeBy(onError = { error.value = it })
        }
    }

    enum class GameState {
        USER, SEARCHING, IDLE, MATCHMAKING, PLAYING
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Kickprotocol.doSend(message: KickprotocolMessage): Completable {
        val endpoint = kickprotocol.connectedEndpoints.firstOrNull()

        if (endpoint == null) {
            throw IllegalArgumentException("No connected endpoints")
        } else {
            return this
                .sendAndAwait(endpoint, message)
                .doOnSubscribe { isLoading.value = true }
                .doOnError { isLoading.value = false }
        }
    }
}
