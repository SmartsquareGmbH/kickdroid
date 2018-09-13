package de.smartsquare.kickdroid.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding2.view.clicks
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import de.smartsquare.kickdroid.R
import de.smartsquare.kickdroid.user.UserManager
import de.smartsquare.kickprotocol.Lobby
import de.smartsquare.kickprotocol.message.JoinLobbyMessage
import io.reactivex.Observable
import kotterknife.bindView
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

/**
 * @author Ruben Gees
 */
class MatchmakingFragment : Fragment() {

    companion object {
        fun newInstance() = MatchmakingFragment()
    }

    private val viewModel by sharedViewModel<GameViewModel>()
    private val userManager by inject<UserManager>()

    private val user
        get() = userManager.user ?: throw IllegalStateException("user is null")

    private val lobby
        get() = viewModel.lobby.value ?: throw IllegalStateException("lobby cannot be null")

    private val isLoading
        get() = viewModel.isLoading.value ?: throw IllegalStateException("isLoading cannot be null")

    private val playerLeft1 by bindView<TextView>(R.id.playerLeft1)
    private val playerLeft2 by bindView<TextView>(R.id.playerLeft2)
    private val playerRight1 by bindView<TextView>(R.id.playerRight1)
    private val playerRight2 by bindView<TextView>(R.id.playerRight2)

    private val joinLeft by bindView<Button>(R.id.joinLeft)
    private val joinRight by bindView<Button>(R.id.joinRight)
    private val startMatch by bindView<Button>(R.id.startMatch)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_matchmaking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.lobby.observe(this, Observer {
            if (it != null) {
                updateUI(it)
            }
        })

        viewModel.isLoading.observe(this, Observer {
            if (it == null) throw IllegalStateException("isLoading cannot be null")

            updateLoading(it)
        })

        Observable
            .merge(
                joinLeft.clicks().map { JoinLobbyMessage.TeamPosition.LEFT to !lobby.leftTeam.contains(user.name) },
                joinRight.clicks().map { JoinLobbyMessage.TeamPosition.RIGHT to !lobby.rightTeam.contains(user.name) }
            )
            .autoDisposable(this.scope())
            .subscribe { (position, shouldJoin) ->
                if (shouldJoin) {
                    viewModel.joinTeam(position)
                } else {
                    viewModel.leaveTeam()
                }
            }

        startMatch.clicks()
            .autoDisposable(this.scope())
            .subscribe { viewModel.startGame() }
    }

    private fun updateUI(lobby: Lobby) {
        playerLeft1.text = lobby.leftTeam.getOrNull(0) ?: getString(R.string.matchmaking_waiting)
        playerLeft2.text = lobby.leftTeam.getOrNull(1) ?: getString(R.string.matchmaking_waiting)
        playerRight1.text = lobby.rightTeam.getOrNull(0) ?: getString(R.string.matchmaking_waiting)
        playerRight2.text = lobby.rightTeam.getOrNull(1) ?: getString(R.string.matchmaking_waiting)

        joinLeft.text = when (lobby.leftTeam.contains(user.name)) {
            true -> getString(R.string.matchmaking_leave)
            false -> getString(R.string.matchmaking_join)
        }

        joinRight.text = when (lobby.rightTeam.contains(user.name)) {
            true -> getString(R.string.matchmaking_leave)
            false -> getString(R.string.matchmaking_join)
        }

        joinLeft.isEnabled = !isLoading && lobby.rightTeam.contains(user.name).not()
        joinRight.isEnabled = !isLoading && lobby.leftTeam.contains(user.name).not()

        if (lobby.owner == user.name) {
            startMatch.visibility = View.VISIBLE
            startMatch.isEnabled = !isLoading && lobby.leftTeam.isNotEmpty() && lobby.rightTeam.isNotEmpty()
        } else {
            startMatch.visibility = View.GONE
        }
    }

    private fun updateLoading(isLoading: Boolean) {
        startMatch.isEnabled = !isLoading && lobby.leftTeam.isNotEmpty() && lobby.rightTeam.isNotEmpty()
        joinLeft.isEnabled = !isLoading && lobby.rightTeam.contains(user.name).not()
        joinRight.isEnabled = !isLoading && lobby.leftTeam.contains(user.name).not()
    }
}
