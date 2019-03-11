package de.smartsquare.kickdroid

import android.Manifest
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.clicks
import com.tbruyelle.rxpermissions2.RxPermissions
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import de.smartsquare.kickdroid.base.BaseActivity
import de.smartsquare.kickdroid.base.DefaultErrorHandler
import de.smartsquare.kickdroid.game.GameViewModel
import de.smartsquare.kickdroid.game.GameViewModel.GameState
import de.smartsquare.kickdroid.game.IdleFragment
import de.smartsquare.kickdroid.game.MatchmakingFragment
import de.smartsquare.kickdroid.game.PlayingFragment
import de.smartsquare.kickdroid.game.SearchingFragment
import de.smartsquare.kickdroid.statistics.StatisticsActivity
import de.smartsquare.kickdroid.user.User
import de.smartsquare.kickdroid.user.UserFragment
import de.smartsquare.kickdroid.user.UserManager
import kotterknife.bindView
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * @author Ruben Gees
 */
class MainActivity : BaseActivity() {

    private val root by bindView<ViewGroup>(android.R.id.content)
    private val toolbar by bindView<Toolbar>(R.id.toolbar)

    private val headline by bindView<TextView>(R.id.headline)
    private val bestPlayersButton by bindView<View>(R.id.bestPlayersButton)

    private val userManager by inject<UserManager>()
    private val viewModel by viewModel<GameViewModel>()

    private val rxPermissions = RxPermissions(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setupUI(userManager.user)

        ensurePermission()
        initListeners()
        initViewModel()
    }

    private fun setupUI(user: User?) {
        if (user == null) {
            headline.text = getString(R.string.main_welcome)
        } else {
            headline.text = getString(R.string.main_welcome_back, user.name)
        }
    }

    private fun ensurePermission() {
        rxPermissions
            .requestEachCombined(Manifest.permission.ACCESS_COARSE_LOCATION)
            .autoDisposable(this.scope())
            .subscribe {
                if (it.shouldShowRequestPermissionRationale) {
                    MaterialDialog(this)
                        .title(R.string.main_permission_title)
                        .message(R.string.main_permission_message)
                        .positiveButton(R.string.main_permission_positive) { _ -> ensurePermission() }
                        .negativeButton(R.string.main_permission_negative) { _ -> finish() }
                        .onCancel { _ -> finish() }
                        .show()
                } else if (it.granted.not()) {
                    finish()
                }
            }
    }

    private fun initListeners() {
        bestPlayersButton.clicks()
            .autoDisposable(this.scope())
            .subscribe { StatisticsActivity.navigateTo(this) }

        userManager.userChanges()
            .autoDisposable(this.scope())
            .subscribe { setupUI(it.toNullable()) }
    }

    private fun initViewModel() {
        viewModel.state.observe(this, Observer {
            if (it == null) throw IllegalStateException("state cannot be null")

            val newFragment = when (it) {
                GameState.USER -> UserFragment.newInstance()
                GameState.SEARCHING -> SearchingFragment.newInstance()
                GameState.IDLE -> IdleFragment.newInstance()
                GameState.MATCHMAKING -> MatchmakingFragment.newInstance()
                GameState.PLAYING -> PlayingFragment.newInstance()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.gameContainer, newFragment)
                .commitNow()
        })

        viewModel.error.observe(this, Observer {
            Snackbar.make(root, DefaultErrorHandler.handle(it), Snackbar.LENGTH_LONG).show()
        })
    }
}
