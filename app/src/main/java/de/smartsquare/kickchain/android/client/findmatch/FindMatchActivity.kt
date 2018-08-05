package de.smartsquare.kickchain.android.client.findmatch

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import de.smartsquare.kickchain.android.client.BaseActivity
import de.smartsquare.kickchain.android.client.R
import de.smartsquare.kickchain.android.client.nearby.NearbyManager
import de.smartsquare.kickchain.android.client.nearby.SearchingMessage
import de.smartsquare.kickchain.android.client.user.User
import de.smartsquare.kickchain.android.client.user.UserManager
import kotterknife.bindView
import kotlin.properties.Delegates

/**
 * @author Ruben Gees
 */
class FindMatchActivity : BaseActivity() {

    companion object {
        private const val LOADING_INDICATOR_ANIMATION_DURATION = 4000L
        private const val LOADING_INDICATOR_ROTATION = 720f
        private const val GOOGLE_API_ERROR_REQUEST = 42343

        fun navigateToForResult(activity: Activity, requestCode: Int) {
            activity.startActivityForResult(Intent(activity, FindMatchActivity::class.java), requestCode)
        }
    }

    private var nearbyManager: NearbyManager? = null

    private var players by Delegates.observable(emptyList<Player>()) { _, _, _ ->
        updateLists()
    }

    private val user: User?
        get() = UserManager.getUser(this)

    private val teamMateAdapter get() = teamMateList.adapter as PlayerAdapter
    private val opponentAdapter get() = opponentList.adapter as PlayerAdapter

    private val root by bindView<View>(android.R.id.content)

    private val searchingIndicator by bindView<View>(R.id.searchingIndicator)
    private val playerContainer by bindView<ViewGroup>(R.id.playerContainer)
    private val teamMateList by bindView<RecyclerView>(R.id.teamMateList)
    private val opponentList by bindView<RecyclerView>(R.id.opponentList)
    private val startMatchButton by bindView<Button>(R.id.startMatchButton)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_find_match)

        setupToolbar()
        setupLists()
        setupStartMatchButton()
        animateSearchingIndicator()
    }

    override fun onStart() {
        super.onStart()

        val availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

        if (availability == ConnectionResult.SUCCESS) {
            setupNearby()
        } else {
            GoogleApiAvailability.getInstance().showErrorDialogFragment(this, availability, GOOGLE_API_ERROR_REQUEST) {
                finish()
            }
        }
    }

    override fun onStop() {
        players = emptyList()

        nearbyManager?.disconnect()
        nearbyManager = null

        super.onStop()
    }

    override fun onDestroy() {
        ViewCompat.animate(searchingIndicator).cancel()

        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()

        return true
    }

    private fun setupNearby() {
        val safeUser = user ?: throw IllegalStateException("The user should not be null on this screen")

        nearbyManager = NearbyManager.connect(this).also {
            it.errorListener = {
                setResult(RESULT_OK, Intent().putExtra("error", it))

                finish()
            }

            it.searchingFoundListener = { foundMessage ->
                players = players.plus(foundMessage.toPlayer())
            }

            it.searchingLostListener = { lostMessage ->
                players = players.minus(lostMessage.toPlayer())
            }

            it.search(SearchingMessage(safeUser.name, 0, 0))
        }
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupLists() {
        teamMateList.layoutManager = LinearLayoutManager(this)
        opponentList.layoutManager = LinearLayoutManager(this)

        teamMateList.adapter = PlayerAdapter()
        opponentList.adapter = PlayerAdapter()

        teamMateList.isNestedScrollingEnabled = false
        opponentList.isNestedScrollingEnabled = false

        teamMateAdapter.playerSelectedCallback = { player ->
            players = players.map {
                when (it.name == player.name) {
                    true -> player.copy(isTeamMate = !player.isTeamMate, isOpponent = false)
                    else -> it.copy(isTeamMate = false)
                }
            }
        }

        opponentAdapter.playerSelectedCallback = { player ->
            var anotherOpponentPresent = false

            players = players.map {
                when (it.name == player.name) {
                    true -> player.copy(isTeamMate = false, isOpponent = !player.isOpponent)
                    else -> {
                        when {
                            anotherOpponentPresent -> it.copy(isOpponent = false)
                            else -> {
                                anotherOpponentPresent = it.isOpponent

                                it
                            }
                        }
                    }
                }
            }
        }

        teamMateAdapter.replaceData(players)
        opponentAdapter.replaceData(players)
    }

    private fun updateLists() {
        if (players.isEmpty()) {
            playerContainer.visibility = View.GONE
        } else {
            playerContainer.visibility = View.VISIBLE

            teamMateAdapter.replaceData(players.filter { !it.isOpponent })
            opponentAdapter.replaceData(players.filter { !it.isTeamMate })
        }

        updateStartMatchButton()
    }

    private fun setupStartMatchButton() {
        startMatchButton.setOnClickListener {
            if (players.any { it.isOpponent }) {
                // TODO
            } else {
                Snackbar.make(root, R.string.find_match_select_opponent, Snackbar.LENGTH_LONG).show()
            }
        }

        updateStartMatchButton()
    }

    private fun updateStartMatchButton() {
        if (players.any { it.isOpponent }) {
            startMatchButton.text = getString(R.string.find_match_start)
        } else {
            startMatchButton.text = getString(R.string.find_match_start_missing_opponent)
        }
    }

    private fun animateSearchingIndicator() {
        ViewCompat.animate(searchingIndicator)
            .x(Resources.getSystem().displayMetrics.widthPixels.toFloat())
            .rotationBy(LOADING_INDICATOR_ROTATION)
            .setDuration(LOADING_INDICATOR_ANIMATION_DURATION)
            .withEndAction {
                searchingIndicator.x = (-searchingIndicator.width).toFloat()

                animateSearchingIndicator()
            }
            .start()
    }
}
