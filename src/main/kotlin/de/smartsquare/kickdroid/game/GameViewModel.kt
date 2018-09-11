package de.smartsquare.kickdroid.game

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.smartsquare.kickdroid.user.UserManager
import de.smartsquare.kickprotocol.ConnectionEvent
import de.smartsquare.kickprotocol.DiscoveryEvent
import de.smartsquare.kickprotocol.Kickprotocol
import de.smartsquare.kickprotocol.Lobby
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

/**
 * @author Ruben Gees
 */
class GameViewModel(private val kickprotocol: Kickprotocol, private val userManager: UserManager) : ViewModel() {

    val state = MutableLiveData<GameState>().apply { value = GameState.SEARCHING }
    val lobby = MutableLiveData<Lobby>()

    private val user
        get() = userManager.user ?: throw IllegalStateException("user cannot be null")

    private val disposables = CompositeDisposable()

    init {
        disposables += kickprotocol.discoveryEvents
            .filter { it is DiscoveryEvent.Found }
            .flatMapCompletable { kickprotocol.connect(user.name, it.endpointId) }
            .subscribe()

        disposables += kickprotocol.connectionEvents
            .filter { it is ConnectionEvent.Disconnected }
            .subscribe {
                state.value = GameState.SEARCHING
            }

        disposables += kickprotocol.idleMessageEvents
            .subscribe { state.value = GameState.IDLE }

        disposables += kickprotocol.matchmakingMessageEvents
            .subscribe {
                state.value = GameState.MATCHMAKING
                lobby.value = it.message.lobby
            }

        disposables += kickprotocol.playingMessageEvents
            .subscribe {
                state.value = GameState.PLAYING
                lobby.value = it.message.lobby
            }
    }

    override fun onCleared() {
        disposables.dispose()
        kickprotocol.stop()

        super.onCleared()
    }

    fun discover() {
        kickprotocol.discover().subscribe()
    }

    enum class GameState {
        SEARCHING, IDLE, MATCHMAKING, PLAYING
    }
}
