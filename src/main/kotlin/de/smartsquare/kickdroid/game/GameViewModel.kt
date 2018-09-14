package de.smartsquare.kickdroid.game

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy

/**
 * @author Ruben Gees
 */
class GameViewModel(private val kickprotocol: Kickprotocol, private val userManager: UserManager) :
    ViewModel() {

    private companion object {
        private val EMPTY_LOBBY = Lobby("", "", emptyList(), emptyList(), 0, 0)
    }

    val state = MutableLiveData<GameState>().apply { value = GameState.SEARCHING }
    val isLoading = MutableLiveData<Boolean>().apply { value = false }
    val error = MutableLiveData<Throwable>()
    val lobby = MutableLiveData<Lobby>().apply { value = EMPTY_LOBBY }

    private val user
        get() = userManager.user ?: throw IllegalStateException("user cannot be null")

    private val disposables = CompositeDisposable()
    private var discoveryDisposable: Disposable? = null

    init {
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
                lobby.value = EMPTY_LOBBY
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
                lobby.value = EMPTY_LOBBY
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
        discoveryDisposable?.dispose()
        disposables.dispose()
        kickprotocol.stop()

        discoveryDisposable = null

        super.onCleared()
    }

    fun discover() {
        if (isLoading.value != true && discoveryDisposable == null) {
            discoveryDisposable = kickprotocol.discover()
                .doOnSubscribe { isLoading.value = true }
                .doOnTerminate { isLoading.value = false }
                .subscribeBy(onError = { error.value = it })
        }
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
        SEARCHING, IDLE, MATCHMAKING, PLAYING
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
